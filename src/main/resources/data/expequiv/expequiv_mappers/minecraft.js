//#define getItem( getItem("minecraft",

function setupData() {
    Data.setup("minecraft:max_blaze_powders_from_rod", 2);
}

function registerEMC(configs) {
    configs.addEMC(getItem("wither_skeleton_skull"), "WitherSkeletonSkull", 4096);
    configs.addEMC(getItem("dragon_head"), "DragonHead", 1024);
    configs.addEMC(getItem("experience_bottle"), "BottleO'Enchanting", 384);
}

function addMappers(mappers) {
    var blazeRod = ItemStack.create(getItem("blaze_rod"));

    var powderFromRod = parseInt(Data.get("minecraft:max_blaze_powders_from_rod"));
    var blazePowder = ItemStack.create(getItem("blaze_powder"));

    // This does nothing....
    mappers.map(blazeRod, Ingredient.decode(blazePowder).stack(powderFromRod));
    mappers.map(blazePowder, powderFromRod, Ingredient.decode(blazeRod));
}