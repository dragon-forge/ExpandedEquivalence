package org.zeith.expequiv.js.wrappers;

import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class JSFStack
{
	public final FluidStack EMPTY = FluidStack.EMPTY;
	
	public boolean isEmpty(FluidStack stack)
	{
		return stack == null || stack.isEmpty();
	}
	
	public Fluid getFluid(FluidStack stack)
	{
		return stack.getFluid();
	}
	
	public int getCount(FluidStack stack)
	{
		return stack.getAmount();
	}
	
	public FluidStack create(Fluid fluid, int amt)
	{
		return new FluidStack(fluid, amt);
	}
}