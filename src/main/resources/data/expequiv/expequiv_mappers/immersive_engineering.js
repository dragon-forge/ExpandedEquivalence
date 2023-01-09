//#define ${mod} immersiveengineering
//#require isModLoaded("${mod}");

function registerEMC(configs) {
    var mod = "${mod}";
    configs.addEMC(getItem(mod, "hemp_fiber"), "IndustrialHempFiber", 24);
    configs.addEMC(getItem(mod, "slag"), "Slag", 16);
    configs.addEMC(getItem(mod, "dust_coke"), "CokeDust", 128);
    configs.addEMC(getItem(mod, "dust_salpeter"), "Nitrate", 32);
    configs.addEMC(getItem(mod, "dust_wood"), "Sawdust", 4);
    configs.addEMC(getItem(mod, "treated_wood_horizontal"), "TreatedWoodPlanks", 10);
}

//#import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;

function decodeIngredientWithSize(ingr) {
    if(ingr instanceof IngredientWithSize)
        return Ingredient.decode(ingr.getBaseIngredient()).stack(ingr.getCount());
    return Ingredient.decode(ingr);
}

function decodeFluidTagInput(FluidTagInput) {
    if(!FluidTagInput) return null;
    return Ingredient.decode1(FluidTagInput.getMatchingFluidStacks()).stack(FluidTagInput.getAmount());
}

function mapMultiblockRecipe(mappers) {
    return function(recipe, output, inputs) {
        var outputs = List.arrayList();
        var inputs = List.arrayList();

        var tmp = recipe.getItemInputs();
        for(var i = 0; tmp && i < tmp.size(); ++i) {
            inputs.add(decodeIngredientWithSize(tmp.get(i)));
        }

        tmp = recipe.getFluidInputs();
        for(var i = 0; tmp && i < tmp.size(); ++i) {
            inputs.add(decodeFluidTagInput(tmp.get(i)));
        }

        tmp = recipe.getFluidOutputs();
        for(var i = 0; tmp && i < tmp.size(); ++i) {
            outputs.add(Ingredient.decode(tmp.get(i)));
        }

        tmp = recipe.getItemOutputs();
        for(var i = 0; tmp && i < tmp.size(); ++i) {
            outputs.add(Ingredient.decode(tmp.get(i)));
        }

        mappers.multiMap(outputs, inputs);
    }
}

function input2OutputMapper(mappers) {
    return function(recipe, output, inputs) {
        var inputs = List.arrayList();
        inputs.add(decodeIngredientWithSize(recipe.input));
        mappers.map(Ingredient.decode(output), inputs);
    };
}

function addMappers(mappers) {
    var defRecipeMapper = Recipe.mapItems();

    var creosote = getFluid("${mod}", "creosote");

    mappers.map(Ingredient.decode(FluidStack.create(creosote, 1)), Ingredient.decode(FluidStack.create(getFluid1("water"), 1)));

    Recipe.mapRecipeType("${mod}:alloy", function (recipe, output, inputs) {
        var inputs = List.arrayList();
        inputs.add(decodeIngredientWithSize(recipe.input0));
        inputs.add(decodeIngredientWithSize(recipe.input1));
        mappers.map(Ingredient.decode(output), inputs);
    });

    Recipe.mapRecipeType("${mod}:blast_furnace", input2OutputMapper(mappers));

    Recipe.mapRecipeType("${mod}:coke_oven", function (recipe, output, inputs) {
        var outputs = List.arrayList();
        var inputs = List.arrayList();
        inputs.add(decodeIngredientWithSize(recipe.input));
        outputs.add(Ingredient.decode(output));
        outputs.add(Ingredient.decode(FluidStack.create(creosote, recipe.creosoteOutput)));
        mappers.multiMap(outputs, inputs);
    });

    Recipe.mapRecipeType("${mod}:blueprint", mapMultiblockRecipe(mappers));
    Recipe.mapRecipeType("${mod}:metal_press", input2OutputMapper(mappers));
    // TODO: ArcFurnaceRecipe

    Recipe.mapRecipeType("${mod}:bottling_machine", function (recipe, output, inputs) {
        var outputs = List.arrayList();
        var inputs = List.arrayList();

        if(!recipe.getItemInputs) return;

        var tmp = recipe.getItemInputs();
        for(var i = 0; tmp && i < tmp.size(); ++i) {
            inputs.add(decodeIngredientWithSize(tmp.get(i)));
        }
        inputs.add(decodeFluidTagInput(recipe.fluidInput));

        outputs.addAll(Ingredient.decode(recipe.output.get()));
        mappers.multiMap(outputs, inputs);
    });

    Recipe.mapRecipeType("${mod}:crusher", input2OutputMapper(mappers));

    // TODO: SawmillRecipe

    var fermenterAndSqueezer = function (recipe, output, inputs) {
        var outputs = List.arrayList();
        var inputs = List.arrayList();
        inputs.add(decodeIngredientWithSize(recipe.input));
        outputs.add(Ingredient.decode(output));
        if(!FluidStack.isEmpty(recipe.fluidOutput)) outputs.add(Ingredient.decode(recipe.fluidOutput));
        mappers.multiMap(outputs, inputs);
    };

    Recipe.mapRecipeType("${mod}:fermenter", fermenterAndSqueezer);
    Recipe.mapRecipeType("${mod}:squeezer", fermenterAndSqueezer);

    Recipe.mapRecipeType("${mod}:refinery", function (recipe, output, inputs) {
        var outputs = List.arrayList();
        var inputs = List.arrayList();
        inputs.add(decodeFluidTagInput(recipe.input0));
        inputs.add(decodeFluidTagInput(recipe.input1));
        // inputs.add(Ingredient.decode(recipe.catalyst));
        mappers.map(Ingredient.decode(recipe.output).stack(FluidStack.getCount(recipe.output)), inputs);
    });
}