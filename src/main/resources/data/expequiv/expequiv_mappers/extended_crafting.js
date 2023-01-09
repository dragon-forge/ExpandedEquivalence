//#define ${mod} extendedcrafting
//#require isModLoaded("${mod}");

function addMappers(mappers) {
    var defRecipeMapper = Recipe.mapItems();

    Recipe.mapRecipeType("${mod}:combination", defRecipeMapper);
    Recipe.mapRecipeType("${mod}:table", defRecipeMapper);

    Recipe.mapRecipeType("${mod}:compressor", function (recipe, output, inputs) {
        var items = List.arrayList();
        items.add(Ingredient.decode(recipe.getCatalyst()));

        for(var i = 0; i < inputs.size(); ++i) {
            items.add(Ingredient.decode(inputs.get(i)).stack(recipe.getInputCount()));
        }

        mappers.map(output, items);
    });

    Recipe.mapRecipeType("${mod}:ender_crafter", defRecipeMapper);
    Recipe.mapRecipeType("${mod}:flux_crafter", defRecipeMapper);
}