package org.zeith.expequiv.js;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.zeith.expequiv.ExpandedEquivalence;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.expequiv.api.emc.MutableEMCContext;
import org.zeith.expequiv.utils.*;

import javax.script.ScriptException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ScriptSystem
{
	protected final MutableEMCContext context = new MutableEMCContext();
	
	protected final CompletableFuture<Map<ResourceLocation, ExpansionJS>> expansions;
	
	public ScriptSystem(ReloadableServerResources resources, ResourceManager manager)
	{
		this.expansions = compileExpansions(gatherExpansions(context, resources, manager))
				// Perform common setup
				.thenApply(prepared ->
				{
					prepared.entrySet().removeIf(e ->
					{
						try
						{
							e.getValue().setupData();
							return false;
						} catch(Exception err)
						{
							ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to setup its data. Skipping", err);
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
						} catch(Exception err)
						{
							ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to tweak its data. Skipping", err);
							BreakpointOnException.breakpoint(err);
						}
						return true;
					});
					
					return prepared;
				});
	}
	
	protected List<ISubTagFilter> subTagFilters;
	protected Map<TagKey<Item>, Set<Item>> addedToTags;
	
	public void preload()
	{
		subTagFilters = null;
	}
	
	public Map<TagKey<Item>, Set<Item>> setupTags()
	{
		if(addedToTags != null) return addedToTags;
		
		addedToTags = new HashMap<>();
		
		ExpandedEquivalence.LOG.info("Gathering extra item tag content.");
		long now = System.currentTimeMillis();
		
		expansions.join().entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().populateTags(addedToTags);
				return false;
			} catch(Exception err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to gather its extra item tags. Skipping", err);
				BreakpointOnException.breakpoint(err);
			}
			return true;
		});
		
		ExpandedEquivalence.LOG.info("Built-in extra item tags gathered in " + (System.currentTimeMillis() - now) + " ms.");
		return addedToTags;
	}
	
	public List<ISubTagFilter> gatherBlockers()
	{
		if(this.subTagFilters != null) return this.subTagFilters;
		
		ExpandedEquivalence.LOG.info("Gathering sub-tag blockers.");
		long now = System.currentTimeMillis();
		
		var prepared = expansions.join();
		
		List<ISubTagFilter> subTagFilters = new ArrayList<>();
		ISubTagFilterCollector collector = subTagFilters::add;
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().gatherBlockers(collector);
				return false;
			} catch(Exception err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to gather its sub-tag blockers. Skipping", err);
				BreakpointOnException.breakpoint(err);
			}
			return true;
		});
		
		ExpandedEquivalence.LOG.info("Built-in sub-tag blockers (" + subTagFilters.size() + ") gathered in " + (System.currentTimeMillis() - now) + " ms.");
		
		return this.subTagFilters = subTagFilters;
	}
	
	public void applyContextAndExecute(IContextEMC ctx)
	{
		this.context.update(ctx);
		
		ExpandedEquivalence.LOG.info("Reloading built-in expansions.");
		long now = System.currentTimeMillis();
		var prepared = expansions.join();
		
		prepared.entrySet().removeIf(e ->
		{
			try
			{
				e.getValue().registerEMC();
				return false;
			} catch(Exception err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to register its EMC. Skipping", err);
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
			} catch(Exception err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to add its mappers. Skipping", err);
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
			} catch(Exception err)
			{
				ExpandedEquivalence.LOG.fatal("Expansion " + e.getKey() + " has failed to complete. Skipping", err);
				BreakpointOnException.breakpoint(err);
			}
			return true;
		});
		
		ExpandedEquivalence.LOG.info("Built-in expansions reloaded in " + (System.currentTimeMillis() - now) + " ms.");
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
	
	
	public static Map<ResourceLocation, CompletableFuture<ExpansionJS>> gatherExpansions(IContextEMC ctx, ReloadableServerResources resources, ResourceManager manager)
	{
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
		return reloads;
	}
	
	public static CompletableFuture<Map<ResourceLocation, ExpansionJS>> compileExpansions(Map<ResourceLocation, CompletableFuture<ExpansionJS>> reloads)
	{
		return CompletableFuture.supplyAsync(() ->
		{
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
			return prepared;
		});
	}
}