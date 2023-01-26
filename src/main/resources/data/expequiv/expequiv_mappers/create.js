//#define ${mod} create
//#require isModLoaded("${mod}");

//#define getItem( getItem("${mod}",

function setupData() {
}

function registerEMC(configs) {
    configs.addEMC(getItem("cinder_flour"), "CinderFlour", 1);
    configs.addEMC(getItem("limestone"), "Limestone", 1);
    configs.addEMC(getItem("crimsite"), "Crimsite", 1);
    configs.addEMC(getItem("ochrum"), "Ochrum", 1);
    configs.addEMC(getItem("veridium"), "Veridium", 1);
    configs.addEMC(getItem("asurine"), "Asurine", 1);
    configs.addEMC(getItem("powdered_obsidian"), "PowderedObsidian", 64);
    configs.addEMC(getItem("wheat_flour"), "WheatFlour", 64);
}

function ItemApplicationRecipe(mappers, recipe, output) {
    var inputIngrs = List.arrayList();

    if(!recipe.shouldKeepHeldItem()) inputIngrs.add(Ingredient.decode1(recipe.getRequiredHeldItem()));
    inputIngrs.add(Ingredient.decode1(recipe.getProcessedItem()));

    mappers.map(output, inputIngrs);
}

function ProcessingRecipeInputs(mappers, recipe) {
    var inputs = List.arrayList();
    var inputItems = recipe.getIngredients();
    var inputFluids = recipe.getFluidIngredients();
    inputs.addAll(Ingredient.decode(inputItems));
    for(var i = 0; i < inputFluids.size(); ++i)
        inputs.add(Ingredient.decode1(inputFluids.get(i).getMatchingFluidStacks()));
    return inputs;
}

function ProcessingRecipe(mappers, recipe) {
    var outputs = List.arrayList();
    var outputItems = recipe.getRollableResultsAsItemStacks();
    var outputFluids = recipe.getFluidResults();
    for(var i = 0; i < outputItems.size(); ++i) outputs.add(Ingredient.decode1(outputItems.get(i)));
    for(var i = 0; i < outputFluids.size(); ++i) outputs.add(Ingredient.decode1(outputFluids.get(i)));
    mappers.multiMap(outputs, ProcessingRecipeInputs(mappers, recipe));
}

function SequencedAssemblyRecipe(mappers, recipe, output) {
    var inputs = List.arrayList();
    inputs.add(Ingredient.decode1(recipe.getIngredient()));

    var loopCount = recipe.getLoops();

    var sequence = recipe.getSequence();
    var transitional = recipe.getTransitionalItem();

    for(var i = 0; i < sequence.size(); ++i) {
        var seq = sequence.get(i).getRecipe();

        var inputItems = seq.getIngredients();
        var inputFluids = seq.getFluidIngredients();

        for(var j = 0; j < inputItems.size(); ++j) {
            var ingr = inputItems.get(j);
            if(!ingr.test(transitional)) {
                inputs.add(Ingredient.decode1(ingr).stack(loopCount));
            }
        }

        for(var j = 0; j < inputFluids.size(); ++j) {
            inputs.add(Ingredient.decode1(inputFluids.get(j).getMatchingFluidStacks()).stack(loopCount));
        }
    }

    mappers.map(output, inputs);
}

function addMappers(mappers) {
    var ctx = mappers.context();

    var procRecipeFun = function (recipe, output, inputs) {
        ProcessingRecipe(mappers, recipe);
    };

    Recipe.mapRecipeType("${mod}:deploying", function (recipe, output, inputs) {
        ItemApplicationRecipe(mappers, recipe, output);
    });

    Recipe.mapRecipeType("${mod}:item_application", function (recipe, output, inputs) {
        ItemApplicationRecipe(mappers, recipe, output);
    });

    Recipe.mapRecipeType("${mod}:pressing", procRecipeFun);
    // Recipe.mapRecipeType("${mod}:crushing", procRecipeFun); // This breaks iron/gold/metals badly...
    Recipe.mapRecipeType("${mod}:compacting", procRecipeFun);
    Recipe.mapRecipeType("${mod}:mixing", procRecipeFun);
    Recipe.mapRecipeType("${mod}:sandpaper_polishing", procRecipeFun);
    Recipe.mapRecipeType("${mod}:filling", procRecipeFun);
    Recipe.mapRecipeType("${mod}:haunting", procRecipeFun);

    Recipe.mapRecipeType("${mod}:sequenced_assembly", function(recipe, output, inputs){
        SequencedAssemblyRecipe(mappers, recipe, output);
        // breakpoint(output, recipe);
    });
}