//#define ${mod} botanicadds
//#require isModLoaded("${mod}");

//#define getItem( getItem("${mod}",

function setupData() {
}

function gatherBlockers(sink) {
}

function registerEMC(configs) {
    configs.addEMC(getItem("sculk_petal"), "SculkPetal", 128);
    configs.addEMC(getItem("reduced_sculk_sensor"), "ReducedSculkSensor", 2168);
}

function mana2emc(mana) {
    return mana / Data.get("botania:mana_per_emc");
}

function addMappers(mappers) {
    var ctx = mappers.context();
    var oneManaEmc = mana2emc(1);

    var livingRock = Ingredient.decode(ItemStack.create(getItem("livingrock")));

    Recipe.mapRecipeType("${mod}:gaia_plate", function (recipe, output, inputs) {
        var lst = List.arrayList();
        lst.addAll(Ingredient.decode(inputs));
        lst.add(ctx.forEMC(oneManaEmc * 600000).stack(1));
        mappers.map(output, lst);
    });
}