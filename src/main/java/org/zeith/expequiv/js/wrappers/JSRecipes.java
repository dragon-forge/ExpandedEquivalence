package org.zeith.expequiv.js.wrappers;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.zeith.expequiv.api.emc.IContextEMC;
import org.zeith.expequiv.js.ExpansionJS;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.hammerlib.util.java.consumers.Consumer3;

import java.util.Objects;

public class JSRecipes
{
	protected final ExpansionJS exp;
	protected final IContextEMC context;
	protected final ReloadableServerResources resources;
	protected final JSIngredients ingredients;
	
	public JSRecipes(ExpansionJS exp, IContextEMC context, ReloadableServerResources resources, JSIngredients ingredients)
	{
		this.exp = exp;
		this.context = context;
		this.resources = resources;
		this.ingredients = ingredients;
	}
	
	public Consumer3<Recipe<?>, ItemStack, NonNullList<Ingredient>> mapItems()
	{
		return (recipe, result, ingredients) ->
				context.registrar().map(result, ingredients.stream()
						.map(this.ingredients::decode)
						.filter(Objects::nonNull)
						.toList()
				);
	}
	
	public void mapRecipeType(String id, Consumer3<Recipe<?>, ItemStack, NonNullList<Ingredient>> handler)
	{
		RecipeType<?> type = ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation(id));
		if(type != null)
		{
			exp.log.info("Mapping recipe type " + new ResourceLocation(id));
			resources.getRecipeManager().getAllRecipesFor(Cast.cast(type))
					.forEach(r ->
					{
						var list = NonNullList.<Ingredient> create();
						list.addAll(r.getIngredients());
						handler.accept(r, r.getResultItem().copy(), list);
					});
		} else
			exp.log.warn("Tried to map an unknown recipe type: " + new ResourceLocation(id));
	}
}