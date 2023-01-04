package org.zeith.expequiv;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.zeith.expequiv.api.CountedIngredient;
import org.zeith.expequiv.api.emc.*;
import org.zeith.expequiv.api.event.GatherEMCEvent;
import org.zeith.expequiv.compat.js.ExpansionJS;

import java.util.concurrent.atomic.AtomicReference;

@EMCMapper(requiredMods = "expequiv")
public class PEMapper
		implements IEMCMapper<NormalizedSimpleStack, Long>
{
	@Override
	public String getName()
	{
		return "ExpandedEquivalenceMapper";
	}
	
	@Override
	public String getDescription()
	{
		return "Helps ExpandedEquivalence map EVERYTHING.";
	}
	
	@Override
	public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> collector, CommentedFileConfig config, ReloadableServerResources resources, ResourceManager manager)
	{
		AtomicReference<GatherEMCEvent> ref = new AtomicReference<>();
		IEMCRegistrar registrar = new IEMCRegistrar()
		{
			@Override
			public IContextEMC context()
			{
				return ref.get();
			}
			
			@Override
			public void map(CountedIngredient out, CountedIngredient... ings)
			{
				if(out == null) return;
				
				Object2IntArrayMap<NormalizedSimpleStack> ingredients = new Object2IntArrayMap<>();
				
				for(CountedIngredient ci : ings)
					if(ci != null && ci.getIngredient() != null && ci.getCount() > 0)
					{
						var nss = ci.getIngredient();
						
						if(nss == null)
						{
							ExpandedEquivalence.LOG.error("Found a NULL ingredient while adding " + out + ": " + ci);
						}
						
						if(ingredients.containsKey(nss))
							ingredients.put(nss, ingredients.getInt(nss) + ci.getCount());
						else
							ingredients.put(nss, ci.getCount());
					}
				
				if(out.getIngredient() != null && out.getCount() > 0 && !ingredients.isEmpty())
					collector.addConversion(out.getCount(), out.getIngredient(), ingredients);
			}
			
			@Override
			public void register(FakeItem item, long emc)
			{
				collector.setValueBefore(item.getHolder(), emc);
			}
			
			@Override
			public void register(ItemStack item, long emc)
			{
				collector.setValueBefore(NSSItem.createItem(item), emc);
			}
		};
		ref.set(new GatherEMCEvent(registrar));
		MinecraftForge.EVENT_BUS.post(ref.get());
		
		ExpansionJS.loadFromResources(ref.get(), resources, manager);
	}
}