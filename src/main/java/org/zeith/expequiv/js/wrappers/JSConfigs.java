package org.zeith.expequiv.js.wrappers;

import moze_intel.projecte.api.nss.NSSItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;
import org.zeith.expequiv.api.emc.FakeItem;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.expequiv.js.ExpansionJS;
import org.zeith.hammerlib.util.configured.ConfigFile;
import org.zeith.hammerlib.util.configured.ConfiguredLib;
import org.zeith.hammerlib.util.configured.data.IntValueRange;

public class JSConfigs
{
	protected final ConfigFile config;
	protected final IContextEMC context;
	protected final ExpansionJS exp;
	
	public JSConfigs(ConfigFile config, IContextEMC context, ExpansionJS exp)
	{
		this.config = config;
		this.context = context;
		this.exp = exp;
	}
	
	public void addTagEMC(String tag, String configKey, long EMC)
	{
		if(tag.startsWith("#")) tag = tag.substring(1);
		context.registrar().register(FakeItem.ofExisting(NSSItem.createTag(new ResourceLocation(tag))), getCfgEMC(EMC, configKey));
	}
	
	public void addEMC(ItemLike item, String configKey, long EMC)
	{
		ItemStack res;
		if(item == null || item == Items.AIR || (res = new ItemStack(item)).isEmpty())
		{
			exp.log.warn("Tried to map EMC to non-existent item: " + configKey);
			return;
		}
		context.registrar().register(res, getCfgEMC(EMC, configKey));
	}
	
	public void addEMC(FluidStack fluid, String configKey, long EMC)
	{
		if(fluid == null || fluid.isEmpty())
		{
			exp.log.warn("Tried to map EMC to non-existent FluidStack: " + configKey);
			return;
		}
		context.registrar().register(fluid, getCfgEMC(EMC, configKey));
	}
	
	public long getCfgEMC(long base, String id)
	{
		return getCfgEMC(base, id, splitName(id));
	}
	
	public long getCfgEMC(long base, String id, String name)
	{
		return config.setupCategory("EMC")
				.getElement(ConfiguredLib.INT, id)
				.withRange(IntValueRange.range(0L, Long.MAX_VALUE))
				.withDefault(base)
				.withComment("Base cost for " + name + ". Set to 0 to disable.")
				.getValue()
				.longValue();
	}
	
	private static String splitName(String str)
	{
		StringBuilder sb = new StringBuilder();
		for(char c : str.toCharArray())
		{
			if(Character.toUpperCase(c) == c && Character.toLowerCase(c) != c && sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ')
				sb.append(' ');
			sb.append(c);
		}
		return sb.toString();
	}
}