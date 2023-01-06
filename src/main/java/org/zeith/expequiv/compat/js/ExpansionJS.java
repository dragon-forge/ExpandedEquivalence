package org.zeith.expequiv.compat.js;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.expequiv.ExpandedEquivalence;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.expequiv.compat.js.wrappers.*;
import org.zeith.expequiv.utils.BreakpointOnException;
import org.zeith.hammerlib.util.configured.ConfigFile;
import org.zeith.hammerlib.util.java.functions.Function2;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
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
		
		Consumer<String> info = log::info, warn = log::warn, error = log::error;
		
		var ingr = new JSIngredients(context);
		
		this.engine = js
				.addInstancePointer(new JSLists(), "List")
				.addInstancePointer(new JSStack(context), "ItemStack")
				.addInstancePointer(new JSReflection(), "Reflection")
				.addInstancePointer(ingr, "Ingredient")
				.addInstancePointer(new JSRecipes(this, context, resources, ingr), "Recipe")
				.addInstancePointer(context.data(), "Data")
				.addInstancePointer(getItem, "getItem")
				.addInstancePointer(info, "info")
				.addInstancePointer(warn, "warn")
				.addInstancePointer(error, "error")
				.addInstancePointer(BreakpointOnException.BREAKPOINT, "breakpoint")
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
			log.error("Failed to perform script step 'setupData' for script " + id, e);
			throw new RuntimeException(e);
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
			log.error("Failed to perform script step 'tweakData' for script " + id, e);
			throw new RuntimeException(e);
		}
	}
	
	public void registerEMC()
	{
		try
		{
			engine.callFunction("registerEMC", new JSConfigs(config, context, this));
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing registerEMC method.");
		} catch(Throwable e)
		{
			log.error("Failed to perform script step 'registerEMC' for script " + id, e);
			throw new RuntimeException(e);
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
			log.error("Failed to perform script step 'addMappers' for script " + id, e);
			throw new RuntimeException(e);
		}
	}
	
	public void complete()
	{
		config.save();
	}
	
	public static CompletableFuture<ExpansionJS> createExpansion(Resource.IoSupplier<BufferedReader> input, ResourceLocation fid, IContextEMC ctx, ReloadableServerResources resources)
	{
		return CompletableFuture.supplyAsync(() ->
		{
			try(var br = input.get(); var lns = br.lines())
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
		);
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
			reloads.put(fid, createExpansion(e.getValue()::openAsReader, fid, ctx, resources));
		}
		
		if(!FMLEnvironment.production)
		{
			var devscripts = ExpandedEquivalence.getModCfgPath()
					.resolve("Dev Scipts");
			devscripts.toFile().mkdirs();
			try
			{
				for(final var p : Files.walk(devscripts)
						.filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".js"))
						.toList())
				{
					var id = new ResourceLocation(ExpandedEquivalence.MOD_ID, "dev/" + devscripts.relativize(p));
					final var fid = new ResourceLocation(id.getNamespace(), id.getPath().substring(0, id.getPath().length() - 3));
					ExpandedEquivalence.LOG.info("Found Dev EE script: " + fid + " (" + id + ")");
					var dev = createExpansion(() -> Files.newBufferedReader(p), fid, ctx, resources);
					dev.thenAcceptAsync(exp ->
					{
						try
						{
							Files.writeString(p.getParent().resolve(p.getFileName().toString() + "_dump"), exp.engine.source.parsedJS());
						} catch(IOException e)
						{
							BreakpointOnException.breakpoint(e);
						}
					});
					reloads.put(fid, dev);
				}
			} catch(IOException e)
			{
				BreakpointOnException.breakpoint(e);
			}
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
				BreakpointOnException.breakpoint(e);
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
				BreakpointOnException.breakpoint(err);
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
				BreakpointOnException.breakpoint(err);
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
				BreakpointOnException.breakpoint(err);
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
				BreakpointOnException.breakpoint(err);
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
				BreakpointOnException.breakpoint(err);
			}
			return true;
		});
		
		ExpandedEquivalence.LOG.info("Built-in expansions reloaded in " + (System.currentTimeMillis() - now) + " ms.");
	}
}