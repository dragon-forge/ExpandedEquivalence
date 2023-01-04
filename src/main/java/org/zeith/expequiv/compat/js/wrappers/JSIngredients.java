package org.zeith.expequiv.compat.js.wrappers;

import org.zeith.expequiv.api.CountedIngredient;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.hammerlib.api.energy.EnergyUnit;

import java.util.*;
import java.util.stream.StreamSupport;

public class JSIngredients
{
	public final IContextEMC context;
	
	public JSIngredients(IContextEMC context)
	{
		this.context = context;
	}
	
	public CountedIngredient decode(Object i)
	{
		return CountedIngredient.tryCreate(context, i);
	}
	
	public CountedIngredient[] decode(Object... i)
	{
		return Arrays.stream(i).map(this::decode).toArray(CountedIngredient[]::new);
	}
	
	public Collection<CountedIngredient> decode(Iterable<?> itr)
	{
		return StreamSupport.stream(itr.spliterator(), false)
				.map(this::decode)
				.filter(Objects::nonNull)
				.toList();
	}
	
	public CountedIngredient forgeEnergy(long fe)
	{
		return context.forEMC(Math.round(EnergyUnit.FE.convertTo(fe, EnergyUnit.EMC))).stack(1);
	}
}