package org.zeith.expequiv.utils;

import moze_intel.projecte.api.ProjectEAPI;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import org.zeith.hammerlib.core.RecipeHelper;

import java.lang.reflect.Array;
import java.util.List;

public class EMCUtils
{
	public static long getEMC(Object obj)
	{
		if(obj == null) return 0L;
		
		var proxy = ProjectEAPI.getEMCProxy();
		
		if(obj instanceof Ingredient i)
		{
			if(i.isEmpty()) return 0L;
			return getEMC(i.getItems());
		} else if(obj instanceof ItemStack s) return proxy.getValue(s);
		else if(obj instanceof Item i) return proxy.getValue(i);
		else if(obj instanceof Block b) return proxy.getValue(b);
		else if(obj instanceof List && !((List) obj).isEmpty())
		{
			long least = 0;
			long cemc;
			for(Object o : (List) obj)
				if((cemc = getEMC(o)) > 0 && (cemc < least || least == 0))
					least = cemc;
			return least;
		} else if(obj.getClass().isArray())
		{
			int l = Array.getLength(obj);
			
			long least = 0;
			long cemc;
			for(int i = 0; i < l; ++i)
				if((cemc = getEMC(Array.get(obj, i))) > 0 && (cemc < least || least == 0))
					least = cemc;
			return least;
		} else
		{
			return getEMC(RecipeHelper.fromComponent(obj));
		}
	}
}