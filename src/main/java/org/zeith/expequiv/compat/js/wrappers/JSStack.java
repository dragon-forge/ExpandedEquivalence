package org.zeith.expequiv.compat.js.wrappers;

import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class JSStack
{
	public final ItemStack EMPTY = ItemStack.EMPTY;
	
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
	
	public ItemStack fromState(BlockState state)
	{
		Block blk = state.getBlock();
		Item item = blk.asItem();
		if(item != Items.AIR) return new ItemStack(item);
		return EMPTY;
	}
}