package org.zeith.expequiv.api.event;

import net.minecraft.Util;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.zeith.expequiv.api.EMCData;

public class CreateJSDataEvent
		extends Event
{
	protected final EMCData data;
	
	public CreateJSDataEvent(EMCData data)
	{
		this.data = data;
	}
	
	public EMCData getData()
	{
		return data;
	}
	
	public static EMCData create()
	{
		return Util.make(new EMCData(), data -> MinecraftForge.EVENT_BUS.post(new CreateJSDataEvent(data)));
	}
}