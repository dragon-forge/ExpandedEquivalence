package org.zeith.expequiv.api.emc;

import org.zeith.expequiv.api.CountedIngredient;

import java.util.Collection;

public class EMCIngredientUtils
{
	public static FakeItem merge(IContextEMC emc, Collection<CountedIngredient> ingredients)
	{
		return merge(emc, ingredients.toArray(new CountedIngredient[0]));
	}
	
	public static FakeItem merge(IContextEMC emc, CountedIngredient... ingredients)
	{
		FakeItem fake = emc.registrar().fake();
		CountedIngredient fakeX1 = fake.stack(1);
		for(CountedIngredient i : ingredients) emc.registrar().map(fakeX1, i);
		return fake;
	}
}