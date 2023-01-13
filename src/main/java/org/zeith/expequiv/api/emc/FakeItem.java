package org.zeith.expequiv.api.emc;

import moze_intel.projecte.api.nss.NSSFake;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.zeith.expequiv.ExpandedEquivalence;
import org.zeith.expequiv.api.CountedIngredient;

import java.util.Arrays;
import java.util.Collection;

public class FakeItem
{
	static long fakeID = 0L;
	
	final NormalizedSimpleStack holder;
	
	public FakeItem()
	{
		this(NSSFake.create(ExpandedEquivalence.MOD_ID + "_fake_n" + Long.toUnsignedString(++fakeID)));
	}
	
	public FakeItem(NormalizedSimpleStack holder)
	{
		this.holder = holder;
	}
	
	public static FakeItem ofExisting(NormalizedSimpleStack nss)
	{
		return new FakeItem(nss);
	}
	
	public NormalizedSimpleStack getHolder()
	{
		return holder;
	}
	
	public CountedIngredient stack(int amount)
	{
		return CountedIngredient.create(this, amount);
	}
	
	public static FakeItem merge(IContextEMC emc, CountedIngredient... ingredients)
	{
		return EMCIngredientUtils.merge(emc, ingredients);
	}
	
	public static FakeItem merge(IContextEMC emc, Collection<CountedIngredient> ingredients)
	{
		return EMCIngredientUtils.merge(emc, ingredients);
	}
	
	public static CountedIngredient merge(IContextEMC emc, int count, CountedIngredient... ingredients)
	{
		return CountedIngredient.create(merge(emc, ingredients), count);
	}
	
	public static CountedIngredient merge(IContextEMC emc, int count, Collection<CountedIngredient> ingredients)
	{
		return CountedIngredient.create(merge(emc, ingredients), count);
	}
	
	public static FakeItem create(IContextEMC emc, Ingredient ingredient)
	{
		return merge(emc, Arrays.stream(ingredient.getItems())
				.map(i -> CountedIngredient.create(emc, i))
				.toList()
		);
	}
	
	public static CountedIngredient create(IContextEMC emc, int count, Ingredient ingredient)
	{
		return CountedIngredient.create(create(emc, ingredient), count);
	}
	
	public static CountedIngredient create(IContextEMC emc, Ingredient ingredient, int count)
	{
		return CountedIngredient.create(create(emc, ingredient), count);
	}
}