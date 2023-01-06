package org.zeith.expequiv.api.emc;

import org.zeith.expequiv.api.EMCData;

public class MutableEMCContext
		implements IContextEMC
{
	IContextEMC parent;
	EMCData tmp = new EMCData();
	
	public void update(IContextEMC parent)
	{
		this.parent = parent;
	}
	
	@Override
	public EMCData data()
	{
		if(parent == null) return tmp;
		else if(tmp != null)
		{
			// This will carry over all the data for when it will be needed, later
			tmp.mergeTo(parent.data());
			tmp = null;
		}
		
		return parent.data();
	}
	
	@Override
	public FakeItem forEMC(long emc)
	{
		if(parent == null) throw new IllegalStateException("EMC Context has not yet been set.");
		return parent.forEMC(emc);
	}
	
	@Override
	public IEMCRegistrar registrar()
	{
		if(parent == null) throw new IllegalStateException("EMC Context has not yet been set.");
		return parent.registrar();
	}
}