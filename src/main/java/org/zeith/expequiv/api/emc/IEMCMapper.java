package org.zeith.expequiv.api.emc;

import org.zeith.expequiv.api.event.GatherEMCEvent;
import org.zeith.hammerlib.util.configured.ConfigFile;

public interface IEMCMapper
{
	void configure(ConfigFile cfg);
	
	void register(GatherEMCEvent event);
	
	default boolean doLogRegistration()
	{
		return true;
	}
}