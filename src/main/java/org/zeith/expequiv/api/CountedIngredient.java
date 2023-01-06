package org.zeith.expequiv.api;

import moze_intel.projecte.api.nss.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidStack;
import org.zeith.expequiv.api.emc.FakeItem;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.hammerlib.api.crafting.*;
import org.zeith.hammerlib.api.energy.EnergyUnit;
import org.zeith.hammerlib.util.charging.fe.FECharge;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CountedIngredient
{
	private final int count;
	private final NormalizedSimpleStack ingredient;
	
	private CountedIngredient(int count, NormalizedSimpleStack ingredient)
	{
		this.count = count;
		this.ingredient = ingredient;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public NormalizedSimpleStack getIngredient()
	{
		return ingredient;
	}
	
	public CountedIngredient withCount(int count)
	{
		return new CountedIngredient(count, ingredient);
	}
	
	public CountedIngredient grow(int amount)
	{
		return new CountedIngredient(this.count + amount, ingredient);
	}
	
	public CountedIngredient stack(int amount)
	{
		return new CountedIngredient(this.count * amount, ingredient);
	}
	
	public static CountedIngredient create(IContextEMC ctx, ItemStack stack, int count)
	{
		if(stack.isEmpty() || count < 1)
			return null;
		
		if(stack.getItem() instanceof PotionItem)
		{
			Potion type = PotionUtils.getPotion(stack);
			Map<Potion, Long> costs = (Map<Potion, Long>) ctx.data().get("minecraft:potion_type_costs");
			if(costs != null && costs.containsKey(type))
			{
				List<CountedIngredient> ci = new ArrayList<>();
				
				long potCost = costs.getOrDefault(type, 0L);
				if(potCost > 0) ci.add(ctx.forEMC(potCost).stack(1));
				
				ci.add(create(Items.GLASS_BOTTLE));
				
				if(stack.getItem() == Items.SPLASH_POTION)
					ci.add(create(Items.GUNPOWDER));
				if(stack.getItem() == Items.LINGERING_POTION)
					ci.add(create(Items.DRAGON_BREATH));
				
				FakeItem out = ctx.registrar().fake();
				ctx.registrar().map(out.stack(1), ci);
				return out.stack(count);
			}
		}
		
		return new CountedIngredient(count, NSSItem.createItem(stack.copy().split(1)));
	}
	
	public static CountedIngredient create(IContextEMC ctx, ItemStack stack)
	{
		return create(ctx, stack, stack.getCount());
	}
	
	public static CountedIngredient create(ItemLike item, int count)
	{
		return new CountedIngredient(count, NSSItem.createItem(item));
	}
	
	public static CountedIngredient create(ItemLike item)
	{
		return create(item, 1);
	}
	
	public static CountedIngredient create(FluidStack fluid, int count)
	{
		return new CountedIngredient(count, NSSFluid.createFluid(fluid));
	}
	
	public static CountedIngredient create(FluidStack fluid)
	{
		if(fluid == null)
			return null;
		return new CountedIngredient(fluid.getAmount(), NSSFluid.createFluid(fluid));
	}
	
	public static CountedIngredient create(TagKey<Item> tag, int count)
	{
		return new CountedIngredient(count, NSSItem.createTag(tag));
	}
	
	public static CountedIngredient create(FakeItem item, int count)
	{
		return new CountedIngredient(count, item.getHolder());
	}
	
	public static CountedIngredient tryCreate(IContextEMC ctx, Object x)
	{
		if(x instanceof CountedIngredient ci) return ci;
		
		if(x instanceof ItemStack s)
			return create(ctx, s);
		
		if(x instanceof Item i)
			return create(i, 1);
		
		if(x instanceof Block b)
		{
			Item blk = b.asItem();
			if(blk != Items.AIR) return create(blk, 1);
			else return null;
		}
		
		if(x instanceof Ingredient i)
		{
			if(i.isEmpty()) return null;
			return FakeItem.create(ctx, i, 1);
		}
		
		if(x instanceof Iterable<?> unawareList)
		{
			List<CountedIngredient> inputs = StreamSupport.stream(unawareList.spliterator(), false)
					.map(o -> tryCreate(ctx, o))
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			return ctx.registrar().sum(inputs);
		}
		
		if(x instanceof TagKey<?> tag)
			return create((TagKey<Item>) tag, 1);
		
		if(x instanceof FluidStack)
			return create((FluidStack) x);
		
		if(x instanceof FECharge charge)
		{
			long emcv = (long) Math.ceil(EnergyUnit.EMC.convertFrom(charge.FE, EnergyUnit.FE));
			return ctx.forEMC(emcv).stack(1);
		}
		
		// Add native HammerLib support
		if(x instanceof IBaseIngredient base)
		{
			if(base instanceof IItemIngredient<?> item)
			{
				return tryCreate(ctx, item.asIngredient());
			}
			
			if(base instanceof IFluidIngredient fluid)
			{
				List<FluidStack> matching = fluid.asIngredient(new IngredientStack<>(fluid, 1));
				List<CountedIngredient> inputs = matching.stream().map(CountedIngredient::create).collect(Collectors.toList());
				FakeItem all = ctx.registrar().fake();
				ctx.registrar().map(all.stack(1), inputs);
				return all.stack(1);
			}
			
			if(base instanceof IEnergyIngredient<?> energy)
			{
				long emcv = (long) Math.ceil(EnergyUnit.EMC.convertFrom(energy.getAmount().doubleValue(), energy.getUnit()));
				return ctx.forEMC(emcv).stack(1);
			}
		}
		
		return null;
	}
	
	@Override
	public String toString()
	{
		return "CountedIngredient{" +
				"count=" + count +
				", ingredient=" + ingredient +
				'}';
	}
	
	public boolean isEmpty()
	{
		return ingredient == null || count < 1;
	}
}