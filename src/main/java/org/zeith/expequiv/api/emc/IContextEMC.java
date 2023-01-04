package org.zeith.expequiv.api.emc;

import org.zeith.expequiv.api.EMCData;

public interface IContextEMC
{
	EMCData data();
	
	FakeItem forEMC(long emc);
	
	IEMCRegistrar registrar();
}