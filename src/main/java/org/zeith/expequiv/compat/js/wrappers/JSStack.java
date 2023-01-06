package org.zeith.expequiv.compat.js.wrappers;

import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.zeith.expequiv.api.CountedIngredient;
import org.zeith.expequiv.api.emc.IContextEMC;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

public class JSStack
{
	public final IContextEMC ctx;
	public final ItemStack EMPTY = ItemStack.EMPTY;
	
	public JSStack(IContextEMC ctx)
	{
		this.ctx = ctx;
	}
	
	public boolean isEmpty(ItemStack stack)
	{
		return stack == null || stack.isEmpty();
	}
	
	public int getMetadata(ItemStack stack)
	{
		return stack.getDamageValue();
	}
	
	public Item getItem(ItemStack stack)
	{
		return stack.getItem();
	}
	
	public int getCount(ItemStack stack)
	{
		return stack.getCount();
	}
	
	public ItemStack getRemains(ItemStack stack)
	{
		return stack.getItem().getCraftingRemainingItem(stack);
	}
	
	public boolean doesRemain(ItemStack stack)
	{
		return getRemains(stack).equals(stack, true);
	}
	
	public boolean doesNotRemain(ItemStack stack)
	{
		return !doesRemain(stack);
	}
	
	public CountedIngredient toIngredientIf(Collection<ItemStack> stacks, Predicate<ItemStack> stack)
	{
		return CountedIngredient.tryCreate(ctx, Ingredient.of(stacks.stream().filter(stack)));
	}
	
	public CountedIngredient toIngredientIf(Ingredient stacks, Predicate<ItemStack> stack)
	{
		return CountedIngredient.tryCreate(ctx, Ingredient.of(Arrays.stream(stacks.getItems()).filter(stack)));
	}
	
	public CountedIngredient toIngredientIf(ItemStack[] stacks, Predicate<ItemStack> stack)
	{
		return CountedIngredient.tryCreate(ctx, Ingredient.of(Arrays.stream(stacks).filter(stack)));
	}
	
	public ItemStack create(ItemLike item)
	{
		return new ItemStack(item);
	}
	
	public ItemStack create(ItemLike item, int amt)
	{
		return new ItemStack(item, amt);
	}
	
	public ItemStack create(ItemLike item, int amt, int dmg)
	{
		var st = new ItemStack(item, amt);
		st.setDamageValue(dmg);
		return st;
	}
	
	public ItemStack copy(ItemStack stack)
	{
		return stack.copy();
	}
	
	public ItemStack copyWithCount(ItemStack stack, int count)
	{
		var is = copy(stack);
		is.setCount(count);
		return is;
	}
	
	public ItemStack fromState(BlockState state)
	{
		Block blk = state.getBlock();
		Item item = blk.asItem();
		if(item != Items.AIR) return new ItemStack(item);
		return EMPTY;
	}
}