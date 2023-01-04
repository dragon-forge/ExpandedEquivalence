package org.zeith.expequiv.compat.js;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.expequiv.ExpandedEquivalence;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.expequiv.compat.js.wrappers.*;
import org.zeith.hammerlib.util.configured.ConfigFile;
import org.zeith.hammerlib.util.java.functions.Function2;

import javax.script.ScriptException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class ExpansionJS
{
	public final ResourceLocation id;
	public final JSEngine engine;
	public final IContextEMC context;
	public final ConfigFile config;
	public final Logger log;
	public final ReloadableServerResources resources;
	
	public ExpansionJS(ResourceLocation id, JSSource js, ConfigFile config, IContextEMC context, ReloadableServerResources resources) throws ScriptException
	{
		this.id = id;
		this.context = context;
		this.config = config;
		this.log = LogManager.getLogger("ExpandedEquivalence/" + js.path());
		this.resources = resources;
		
		Function2<String, String, Item> getItem = (namespace, path) ->
		{
			ResourceLocation key;
			if(StringUtil.isNullOrEmpty(path)) key = new ResourceLocation(namespace);
			else key = new ResourceLocation(namespace, path);
			return ForgeRegistries.ITEMS.getValue(key);
		};
		
		Consumer<String> warn = log::warn, error = log::error;
		
		var ingr = new JSIngredients(context);
		
		this.engine = js
				.addClassPointer(JSLists.class, "Lists")
				.addInstancePointer(new JSStack(), "ItemStack")
				.addClassPointer(JSReflection.class, "Reflection")
				.addInstancePointer(ingr, "Ingredients")
				.addInstancePointer(new JSRecipes(this, context, resources, ingr), "Recipes")
				.addInstancePointer(context.data(), "Data")
				.addInstancePointer(getItem, "getItem")
				.addInstancePointer(warn, "warn")
				.addInstancePointer(error, "error")
				.execute(ExtraJSContext.create())
		;
	}
	
	public void setupData()
	{
		try
		{
			engine.callFunction("setupData");
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing setupData method.");
		} catch(Throwable e)
		{
			log.error("Failed to perform script step 'setupData'", e);
		}
	}
	
	public void tweakData()
	{
		try
		{
			engine.callFunction("tweakData");
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing tweakData method.");
		} catch(Throwable e)
		{
			log.error("Failed to perform script step 'tweakData'", e);
		}
	}
	
	public void registerEMC()
	{
		try
		{
			engine.callFunction("registerEMC", new JSConfigs(config, context));
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing registerEMC method.");
		} catch(Throwable e)
		{
			log.error("Failed to perform script step 'registerEMC'", e);
		}
	}
	
	public void addMappers()
	{
		try
		{
			engine.callFunction("addMappers", context.registrar());
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing addMappers method.");
		} catch(Throwable e)
		{
			log.error("Failed to perform script step 'addMappers'", e);
		}
	}
	
	public void complete()
	{
		config.save();
	}
	
	public static void loadFromResources(IContextEMC ctx, ReloadableServerResources resources, ResourceManager manager)
	{
		ExpandedEquivalence.LOG.info("Reloading built-in expansions.");
		long now = System.currentTimeMillis();
		
		final Map<ResourceLocation, CompletableFuture<ExpansionJS>> reloads = new HashMap<>();
		for(var e : manager.listResources("expequiv_mappers", p -> p.getPath().toLowerCase(Locale.ROOT).endsWith(".js")).entrySet())
		{
			var id = e.getKey();
			final var fid = new ResourceLocation(id.getNamespace(), id.getPath().substring(17, id.getPath().length() - 3));
			ExpandedEquivalence.LOG.info("Found EE script: " + fid + " (" + id + ")");
			
			reloads.put(fid, CompletableFuture.supplyAsync(() ->
					{
						try(var br = e.getValue().openAsReader(); var lns = br.lines())
						{
							return JSSource.create(fid, lns);
						} catch(IOException | ScriptException ex)
						{
							throw new CompletionException(ex);
						}
					}).thenCompose(src -> CompletableFuture.supplyAsync(() ->
							{
								try
								{
									return ExpandedEquivalence.openExpansionConfig(fid);
								} catch(IOException ex)
								{
									throw new CompletionException(ex);
								}
							}).thenApply(cfg ->
									{
										try
										{
											return new ExpansionJS(fid, src, cfg, ctx, resources);
										} catch(ScriptException ex)
										{
											throw new CompletionException(ex);
										}
									}
							)
					)
			);
		}
		
		// Perform the common gather:
		final Map<ResourceLocation, ExpansionJS> prepared = new HashMap<>();
		for(var s : reloads.entrySet())
		{
			try
			{
				var exp = s.getValue().join();
				if(!exp.engine.isEnabled)
				{
					ExpandedEquivalence.LOG.info("Expansion " + s.getKey() + " has not passed all required checks (Specifically, " + exp.engine.failedCheck + ") and is going to be disabled.");
					continue;
				}
				
				prepared.put(s.getKey(), exp);
			} catch(CompletionException e)
			{
				var realE = e.getCause();
				ExpandedEquivalence.LOG.fatal("Expansion " + s.getKey() + " has failed to prepare. Skipping", realE);
			}
		}
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().setupData();
				return false;
			} catch(Throwable err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to setup it's data. Skipping", err);
			}
			return true;
		});
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().tweakData();
				return false;
			} catch(Throwable err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to tweak it's data. Skipping", err);
			}
			return true;
		});
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().registerEMC();
				return false;
			} catch(Throwable err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to register it's EMC. Skipping", err);
			}
			return true;
		});
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().addMappers();
				return false;
			} catch(Throwable err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to add it's mappers. Skipping", err);
			}
			return true;
		});
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().complete();
				return false;
			} catch(Throwable err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to complete. Skipping", err);
			}
			return true;
		});
		
		ExpandedEquivalence.LOG.info("Built-in expansions reloaded in " + (System.currentTimeMillis() - now) + " ms.");
	}
}