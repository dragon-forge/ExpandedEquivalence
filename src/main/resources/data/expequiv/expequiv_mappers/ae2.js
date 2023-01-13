//#require isModLoaded("ae2");

function setupData() {
    // Data.setup("key", value /*anything*/);
}

function tweakData() {
    // Data.set("key", value /*anything*/);
}

function registerEMC(configs) {
    configs.addEMC(getItem("ae2", "sky_stone_block"), "SkyStoneBlock", 64);
    configs.addEMC(getItem("ae2", "certus_quartz_crystal"), "CertusQuartz", 64);
    configs.addEMC(getItem("ae2", "matter_ball"), "MatterBall", 256);
    configs.addEMC(getItem("ae2", "singularity"), "Singularity", 256000);
}

function addMappers(mappers) {
    var presses = [
        ItemStack.create(getItem("ae2", "engineering_processor_press")),
        ItemStack.create(getItem("ae2", "calculation_processor_press")),
        ItemStack.create(getItem("ae2", "logic_processor_press")),
        ItemStack.create(getItem("ae2", "silicon_press"))
    ];

    var qes = getItem("ae2", "quantum_entangled_singularity");

    var defRecipeMapper = Recipe.mapItems();

    Recipe.mapRecipeType("ae2:charger", function (recipe, output, inputs) {
        mappers.map(output,
            Ingredient.decode(recipe.getIngredient()),
            Ingredient.forgeEnergy(1600)
        );
    });

    Recipe.mapRecipeType("ae2:transform", function (recipe, output, inputs) {
        if (ItemStack.getItem(output) == qes) return; // Skip quantum entangled singularity
        defRecipeMapper.accept(recipe, output, inputs);
    });

    Recipe.mapRecipeType("ae2:inscriber", function (recipe, output, inputs) {
        inputs.removeIf(function (item) {
            for (var i in presses)
                if (item.test(presses[i]))
                    return true;
            return false;
        });
        defRecipeMapper.accept(recipe, output, inputs);
    });
}