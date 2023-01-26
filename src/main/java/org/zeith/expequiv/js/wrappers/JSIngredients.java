package org.zeith.expequiv.js.wrappers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
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
	
	public CountedIngredient decode1(Object i)
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
	
	public CountedIngredient tag(String tag)
	{
		if(tag.startsWith("#")) tag = tag.substring(1);
		return CountedIngredient.create(ItemTags.create(new ResourceLocation(tag)), 1);
	}
	
	public CountedIngredient forgeEnergy(long fe)
	{
		return context.forEMC(Math.round(EnergyUnit.FE.convertTo(fe, EnergyUnit.EMC))).stack(1);
	}
	
	public boolean testById(Ingredient ingr, ItemStack item)
	{
		if(ingr.isEmpty()) return item.isEmpty();
		if(item.isEmpty()) return false;
		return testById(ingr, item.getItem());
	}
	
	public boolean testById(Ingredient ingr, ItemLike item)
	{
		return Arrays.stream(ingr.getItems()).map(ItemStack::getItem).anyMatch(item.asItem()::equals);
	}
}