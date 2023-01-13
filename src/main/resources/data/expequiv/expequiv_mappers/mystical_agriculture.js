//#define ${mod} mysticalagriculture
//#require isModLoaded("${mod}");

function registerEMC(configs) {
    configs.addEMC(getItem("${mod}", "soulstone"), "Soulstone", 4);
    configs.addEMC(getItem("${mod}", "soulstone_cobble"), "Soulstone", 4);
    configs.addEMC(getItem("${mod}", "inferium_essence"), "InferiumEssence", 8);
    configs.addEMC(getItem("${mod}", "prosperity_shard"), "ProsperityShard", 64);
    configs.addEMC(getItem("${mod}", "fertilized_essence"), "FertilizedEssence", 96);
    configs.addEMC(getItem("${mod}", "experience_droplet"), "ExperienceDroplet", 32);
    configs.addEMC(getItem("${mod}", "soulium_dust"), "SouliumDust", 256);
    configs.addEMC(getItem("${mod}", "cognizant_dust"), "CognizantDust", 1024);
}

function addMappers(mappers) {
    var defRecipeMapper = Recipe.mapItems();

    var soulJar = [
        getItem("${mod}", "soul_jar"),
        getItem("${mod}", "experience_capsule")
    ];

    Recipe.mapRecipeType("${mod}:infusion", function (recipe, output, inputs) {
        inputs.removeIf(function (item) {
            for (var i in soulJar)
                if (Ingredient.testById(item, soulJar[i]))
                    return true;
            return false;
        });
        defRecipeMapper.accept(recipe, output, inputs);
    });

    Recipe.mapRecipeType("${mod}:reprocessor", defRecipeMapper);
    Recipe.mapRecipeType("${mod}:awakening", defRecipeMapper);
}