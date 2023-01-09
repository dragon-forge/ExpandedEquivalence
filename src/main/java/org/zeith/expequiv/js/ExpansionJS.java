package org.zeith.expequiv.js;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.expequiv.js.wrappers.*;
import org.zeith.expequiv.utils.BreakpointOnException;
import org.zeith.expequiv.utils.ISubTagFilterCollector;
import org.zeith.hammerlib.util.configured.ConfigFile;
import org.zeith.hammerlib.util.java.functions.Function2;

import javax.script.ScriptException;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

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
		
		Function<String, Item> getItem1 = (path) -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(path));
		
		Function2<String, String, Fluid> getFluid = (namespace, path) ->
		{
			ResourceLocation key;
			if(StringUtil.isNullOrEmpty(path)) key = new ResourceLocation(namespace);
			else key = new ResourceLocation(namespace, path);
			return ForgeRegistries.FLUIDS.getValue(key);
		};
		
		Function<String, Fluid> getFluid1 = (path) -> ForgeRegistries.FLUIDS.getValue(new ResourceLocation(path));
		
		
		Consumer<String> info = log::info, warn = log::warn, error = log::error;
		
		var ingr = new JSIngredients(context);
		
		this.engine = js
				.addInstancePointer(new JSLists(), "List")
				.addInstancePointer(new JSStack(context), "ItemStack")
				.addInstancePointer(new JSFStack(), "FluidStack")
				.addInstancePointer(new JSReflection(), "Reflection")
				.addInstancePointer(new JSClass(), "Class")
				.addInstancePointer(ingr, "Ingredient")
				.addInstancePointer(new JSRecipes(this, context, resources, ingr), "Recipe")
				.addInstancePointer(context.data(), "Data")
				.addInstancePointer(getItem, "getItem").addInstancePointer(getItem1, "getItem1")
				.addInstancePointer(getFluid, "getFluid").addInstancePointer(getFluid1, "getFluid1")
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
			if(e instanceof ScriptException se)
				e = se.getCause();
			log.error("Failed to perform script step 'setupData' for script " + id, e);
			throw new RuntimeException(e);
		}
	}
	
	public void populateTags(Map<TagKey<Item>, Set<Item>> tags)
	{
		try
		{
			engine.callFunction("fakePopulateEMCTags", new JSTagRegistry(tags));
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing populateTags method.");
		} catch(Throwable e)
		{
			if(e instanceof ScriptException se)
				e = se.getCause();
			log.error("Failed to perform script step 'populateTags' for script " + id, e);
			throw new RuntimeException(e);
		}
	}
	
	public void gatherBlockers(ISubTagFilterCollector collector)
	{
		try
		{
			engine.callFunction("gatherBlockers", collector);
		} catch(NoSuchMethodException ignored)
		{
			log.trace("Script " + id + " is missing gatherBlockers method.");
		} catch(Throwable e)
		{
			if(e instanceof ScriptException se)
				e = se.getCause();
			log.error("Failed to perform script step 'gatherBlockers' for script " + id, e);
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
			if(e instanceof ScriptException se)
				e = se.getCause();
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
			if(e instanceof ScriptException se)
				e = se.getCause();
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
			if(e instanceof ScriptException se)
				e = se.getCause();
			log.error("Failed to perform script step 'addMappers' for script " + id, e);
			throw new RuntimeException(e);
		}
	}
	
	public void complete()
	{
		config.save();
	}
}