//#define ${mod} farmersdelight
//#require isModLoaded("${mod}");

function registerEMC(emc) {
    emc.addTagEMC("#forge:crops/cabbage", "Cabbage", 16);
    emc.addEMC(getItem1("${mod}:brown_mushroom_colony"), "BrownMushroomColony", 32);
    emc.addEMC(getItem1("${mod}:red_mushroom_colony"), "RedMushroomColony", 32);
    emc.addEMC(getItem1("${mod}:straw"), "Straw", 1);
    emc.addEMC(getItem1("${mod}:ham"), "Ham", 64);
}

function handleCuttingRecipe(mappers, CuttingBoardRecipe, inputItems) {
    var inputs = List.arrayList();

    // Add items that do not remain after recipe.
    for (var i = 0; i < inputItems.size(); ++i) {
        var it = ItemStack.toIngredientIf(inputItems.get(i), function (stack) {
            return !ItemStack.doesRemain(stack);
        });
        if (it && !it.isEmpty()) inputs.add(it);
    }

    var outputs = List.arrayList();

    var recipeOutputItems = CuttingBoardRecipe.getRollableResults();

    var minIterations = 1;
    for (var i = 0; i < recipeOutputItems.size(); ++i) {
        var cr = recipeOutputItems.get(i);
        var chance = Math.pow(cr.getChance(), ItemStack.getCount(cr.getStack()));
        minIterations = Math.max(minIterations, parseInt(Math.ceil(1 / chance)));
        outputs.add(Ingredient.decode(cr.getStack()));
    }

    // This part multiplies all things to the minimum amount of iterations (for example, 0.25 (or 25%) will make minIterations = 4)
    // In other words, we get only one of the rarest item, and (1/chance)x of other items
    for (var i = 0; i < inputs.size(); ++i) inputs.set(i, inputs.get(i).stack(minIterations));
    for (var i = 0; i < outputs.size(); ++i) outputs.set(i, outputs.get(i).stack(minIterations));

    mappers.multiMap(outputs, inputs);
}

function addMappers(mappers) {
    mappers.map(Ingredient.decode(getItem1("${mod}:rice_panicle")), /* <== */ Ingredient.decode(getItem1("${mod}:rice")));
    mappers.map(Ingredient.decode(getItem1("${mod}:rich_soil")), /* <== */ Ingredient.decode(getItem1("${mod}:organic_compost")));
    mappers.map(Ingredient.decode(getItem1("${mod}:rotten_tomato")), /* <== */ Ingredient.decode(getItem1("${mod}:tomato")));

    Recipe.mapRecipeType("${mod}:cutting", function (recipe, output, inputs) {
        handleCuttingRecipe(mappers, recipe, inputs);
    });

    Recipe.mapRecipeType("${mod}:cooking", function (recipe, output, ingrs) {
        var inputs = List.arrayList();
        var ctr = recipe.getOutputContainer();
        if (!ItemStack.isEmpty(ctr))
            inputs.add(Ingredient.decode(ctr));
        for (var i = 0; i < ingrs.size(); ++i) {
            inputs.add(Ingredient.decode(ingrs.get(i)));
        }
        mappers.map(output, inputs);
    });
}