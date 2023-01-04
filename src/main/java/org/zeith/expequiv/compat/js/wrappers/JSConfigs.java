package org.zeith.expequiv.compat.js.wrappers;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.hammerlib.util.configured.ConfigFile;
import org.zeith.hammerlib.util.configured.ConfiguredLib;
import org.zeith.hammerlib.util.configured.data.IntValueRange;

public class JSConfigs
{
	protected final ConfigFile config;
	protected final IContextEMC context;
	
	public JSConfigs(ConfigFile config, IContextEMC context)
	{
		this.config = config;
		this.context = context;
	}
	
	public void addEMC(ItemLike item, String configKey, long EMC)
	{
		context.registrar().register(new ItemStack(item), getCfgEMC(EMC, configKey));
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