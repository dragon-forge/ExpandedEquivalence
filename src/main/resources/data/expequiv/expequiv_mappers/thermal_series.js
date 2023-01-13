//#define ${mod} thermal
//#require isModLoaded("${mod}");

function registerEMC(configs) {
    var material = function (id) {
        return getItem("${mod}", id);
    };

    configs.addEMC(material("rich_slag"), "RichSlag", 128);
    configs.addEMC(material("apatite"), "Apatite", 192);
    configs.addEMC(material("niter"), "Niter", 320);
    configs.addEMC(material("sulfur"), "Sulfur", 96);
    configs.addEMC(material("sawdust"), "Sawdust", 4);
    configs.addEMC(material("cinnabar"), "Cinnabar", 512);
    configs.addEMC(material("coal_coke"), "Coke", 192);
    configs.addEMC(material("tar"), "Tar", 32);
    configs.addEMC(material("rosin"), "Rosin", 32);
    configs.addEMC(material("bitumen"), "Bitumen", 256);
    configs.addEMC(material("blizz_rod"), "BlizzCube", 1536);
    configs.addEMC(material("blizz_powder"), "BlizzPowder", 1536 / 4);
    configs.addEMC(material("blitz_rod"), "BlitzMote", 1536);
    configs.addEMC(material("blitz_powder"), "BlitzPowder", 1536 / 4);
    configs.addEMC(material("basalz_rod"), "BasalzShard", 1536);
    configs.addEMC(material("basalz_powder"), "BasalzPowder", 1536 / 4);
}

function tweakData() {
    Data.set("minecraft:max_blaze_powders_from_rod", Math.max(3, Data.get("minecraft:max_blaze_powders_from_rod")));
}

function handleThermalRecipe(mappers, ThermalRecipe, inputItemFilter) {
    var inputs = List.arrayList();

    // Add items that do not remain after recipe.
    var inputItems = ThermalRecipe.getInputItems();
    for (var i = 0; i < inputItems.size(); ++i) {
        var currentIng = i;
        var it = ItemStack.toIngredientIf(inputItems.get(i), function (stack) {
            if (inputItemFilter) return inputItemFilter(stack, currentIng);
            return !ItemStack.doesRemain(stack);
        });
        if (it && !it.isEmpty()) inputs.add(it);
    }

    inputs.addAll(Ingredient.decode(ThermalRecipe.getInputFluids()));

    var outputs = List.arrayList();
    outputs.addAll(Ingredient.decode(ThermalRecipe.getOutputFluids()));

    var recipeOutputItems = ThermalRecipe.getOutputItems();
    var recipeOutputChances = ThermalRecipe.getOutputItemChances();

    var minIterations = 1;
    for (var i = 0; i < recipeOutputChances.size(); ++i) {
        var chance = Math.abs(recipeOutputChances.get(i));
        chance = chance % 1;
        if (chance > 0) minIterations = Math.max(minIterations, parseInt(Math.ceil(1 / chance)));
    }

    // This part multiplies all things to the minimum amount of iterations (for example, 0.25 (or 25%) will make minIterations = 4)
    // In other words, we get only one of the rarest item, and (1/chance)x of other items
    for (var i = 0; i < inputs.size(); ++i) inputs.set(i, inputs.get(i).stack(minIterations));
    for (var i = 0; i < outputs.size(); ++i) outputs.set(i, outputs.get(i).stack(minIterations));

    for (var i = 0; i < recipeOutputItems.size(); ++i) {
        var recipeOutput = recipeOutputItems.get(i);
        var chance = Math.abs(recipeOutputChances.get(i));
        var recipeCount = ItemStack.getCount(recipeOutput);
        var outputCount = chance <= 1 ? recipeCount : (chance * recipeCount); // guaranteed to get, every time
        outputCount *= minIterations; // Multiply by how many times we copy over our recipe
        var oneOf = Ingredient.decode(ItemStack.copyWithCount(recipeOutput, 1));
        var extra = minIterations * (chance % 1);
        outputs.add(oneOf.withCount(Math.ceil(outputCount + extra)));
    }

    mappers.multiMap(outputs, inputs);
}

function addMappers(mappers) {
    var defaultHandler = function (recipe, output, inputs) { handleThermalRecipe(mappers, recipe); };

    Recipe.mapRecipeType("${mod}:centrifuge", defaultHandler);
    Recipe.mapRecipeType("${mod}:crucible", defaultHandler);
    Recipe.mapRecipeType("${mod}:crystallizer", defaultHandler);
    Recipe.mapRecipeType("${mod}:press", function (recipe, output, inputs) {
        handleThermalRecipe(mappers, recipe, function (stack, i) {
            return i < 1;
        });
    });
    Recipe.mapRecipeType("${mod}:pulverizer", defaultHandler);
    Recipe.mapRecipeType("${mod}:pulverizer_recycle", defaultHandler);
    Recipe.mapRecipeType("${mod}:refinery", defaultHandler);
    Recipe.mapRecipeType("${mod}:sawmill", defaultHandler);
    Recipe.mapRecipeType("${mod}:smelter", defaultHandler);
    Recipe.mapRecipeType("${mod}:smelter_recycle", defaultHandler);
}