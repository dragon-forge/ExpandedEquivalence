package org.zeith.expequiv;

import net.minecraft.Util;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.zeith.expequiv.api.event.CreateJSDataEvent;

import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class AllForgeBusEvents
{
	@SubscribeEvent
	public static void populateJSData(CreateJSDataEvent e)
	{
		e.getData().setup("minecraft:potion_type_costs", Util.make(new ConcurrentHashMap<Potion, Long>(), map ->
		{
			for(Potion potion : ForgeRegistries.POTIONS)
			{
				map.put(potion, 100L);
			}
		}));
	}
}