package org.zeith.expequiv.api.event;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import net.minecraftforge.eventbus.api.Event;
import org.zeith.expequiv.api.emc.*;
import org.zeith.expequiv.api.EMCData;

public class GatherEMCEvent
		extends Event
		implements IContextEMC
{
	public final EMCData data = CreateJSDataEvent.create();
	public final IEMCRegistrar registrar;
	public final Long2ObjectArrayMap<FakeItem> emcMappings = new Long2ObjectArrayMap<>();
	
	public GatherEMCEvent(IEMCRegistrar registrar)
	{
		this.registrar = registrar;
	}
	
	@Override
	public EMCData data()
	{
		return data;
	}
	
	@Override
	public FakeItem forEMC(long emc)
	{
		return emcMappings.computeIfAbsent(emc, registrar::fake);
	}
	
	@Override
	public IEMCRegistrar registrar()
	{
		return registrar;
	}
}