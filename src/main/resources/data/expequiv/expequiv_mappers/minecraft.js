//#define getItem( getItem("minecraft",

function setupData() {
    Data.setup("minecraft:max_blaze_powders_from_rod", 2);
}

function addMappers(mappers) {
    var blazeRod = ItemStack.create(getItem("blaze_rod"));

    var powderFromRod = parseInt(Data.get("minecraft:max_blaze_powders_from_rod"));
    var blazePowder = ItemStack.create(getItem("blaze_powder"));

    // This does nothing....
    mappers.map(blazeRod, Ingredient.decode(blazePowder).stack(powderFromRod));
    mappers.map(blazePowder, powderFromRod, Ingredient.decode(blazeRod));
}