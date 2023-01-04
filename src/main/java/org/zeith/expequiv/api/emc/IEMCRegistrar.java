package org.zeith.expequiv.api.emc;

import moze_intel.projecte.api.imc.CustomEMCRegistration;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.emc.mappers.APICustomEMCMapper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.zeith.expequiv.ExpandedEquivalence;
import org.zeith.expequiv.api.CountedIngredient;

import java.util.Collection;
import java.util.List;

public interface IEMCRegistrar
{
	IContextEMC context();
	
	void map(CountedIngredient output, CountedIngredient... ingredients);
	
	// Default methods:
	
	default CountedIngredient sum(Collection<CountedIngredient> inputs)
	{
		CountedIngredient out = fake().stack(1);
		map(out, inputs);
		return out;
	}
	
	default CountedIngredient min(Collection<CountedIngredient> input)
	{
		return EMCIngredientUtils.merge(context(), input).stack(1);
	}
	
	default FakeItem fake(long emc)
	{
		FakeItem fake = new FakeItem();
		register(fake, emc);
		return fake;
	}
	
	default FakeItem fake()
	{
		return new FakeItem();
	}
	
	default void map(ItemStack output, int outCount, Collection<CountedIngredient> ingredients)
	{
		map(output, outCount, ingredients.toArray(new CountedIngredient[0]));
	}
	
	default void map(CountedIngredient ingr, Collection<CountedIngredient> ingredients)
	{
		map(ingr, ingredients.toArray(new CountedIngredient[0]));
	}
	
	default void map(ItemStack output, int outCount, CountedIngredient... ingredients)
	{
		map(CountedIngredient.create(context(), output, outCount), ingredients);
	}
	
	default void map(ItemStack output, Collection<CountedIngredient> ingredients)
	{
		map(output, output.getCount(), ingredients);
	}
	
	default void map(ItemStack output, CountedIngredient... ingredients)
	{
		map(output, output.getCount(), ingredients);
	}
	
	//
	
	default void map(FluidStack output, int outCount, Collection<CountedIngredient> ingredients)
	{
		map(output, outCount, ingredients.toArray(new CountedIngredient[ingredients.size()]));
	}
	
	default void map(FluidStack output, int outCount, CountedIngredient... ingredients)
	{
		map(CountedIngredient.create(output, outCount), ingredients);
	}
	
	default void map(FluidStack output, Collection<CountedIngredient> ingredients)
	{
		map(output, output.getAmount(), ingredients);
	}
	
	default void map(FluidStack output, CountedIngredient... ingredients)
	{
		map(output, output.getAmount(), ingredients);
	}
	
	//
	
	default void multiMap(List<CountedIngredient> output, Collection<CountedIngredient> ingredients)
	{
		if(output.isEmpty()) return;
		if(output.size() == 1)
		{
			CountedIngredient out = output.get(0);
			map(out, ingredients);
			return;
		}
		
		int total = 0;
		for(CountedIngredient c : ingredients) total += c.getCount();
		
		FakeItem obj = new FakeItem();
		map(obj, total, ingredients);
		
		for(CountedIngredient o : output) map(o, obj.stack(o.getCount()));
	}
	
	//
	
	default void map(FakeItem output, int outCount, Collection<CountedIngredient> ingredients)
	{
		map(output, outCount, ingredients.toArray(new CountedIngredient[ingredients.size()]));
	}
	
	default void map(FakeItem output, int outCount, CountedIngredient... ingredients)
	{
		map(CountedIngredient.create(output, outCount), ingredients);
	}
	
	//
	
	default void register(FakeItem item, long emc)
	{
		APICustomEMCMapper.INSTANCE.registerCustomEMC(ExpandedEquivalence.MOD_ID,
				new CustomEMCRegistration(item.getHolder(), emc)
		);
	}
	
	default void register(ItemStack item, long emc)
	{
		APICustomEMCMapper.INSTANCE.registerCustomEMC(ExpandedEquivalence.MOD_ID,
				new CustomEMCRegistration(NSSItem.createItem(item), emc)
		);
	}
}