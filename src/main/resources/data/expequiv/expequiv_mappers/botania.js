//#define ${mod} botania
//#require isModLoaded("${mod}");

//#define getItem( getItem("${mod}",

//#import vazkii.botania.common.item.material.RuneItem;

function setupData() {
    Data.setup("botania:mana_per_emc", 10);
}

function gatherBlockers(sink) {
    sink.add(function (item, emc, tag) {
        // This makes sure to remove special flowers of Botania from default EMC value of 16.
        return (
            ItemStack.isInTag(getItem1(item), "botania:special_flowers")
            || ItemStack.isInTag(getItem1(item), "botania:mini_flowers")
        );
    });
}

function registerEMC(configs) {
    configs.addEMC(getItem("record_gaia_1"), "ScathedMusicDiscEndureEmptiness", 4096);
    configs.addEMC(getItem("record_gaia_2"), "ScathedMusicDiscFightForQuiescence", 4096);
    configs.addEMC(getItem("ender_air_bottle"), "EnderAirBottle", 2);
    configs.addEMC(getItem("life_essence"), "GaiaSpirit", 1024 * 12);
    configs.addEMC(getItem("overgrowth_seed"), "OvergrowthSeed", 1024 * 3);
    configs.addEMC(getItem("black_lotus"), "BlackLotus", mana2emc(8000));
    configs.addEMC(getItem("blacker_lotus"), "BlackerLotus", mana2emc(100000));
}

function mana2emc(mana) {
    return mana / Data.get("${mod}:mana_per_emc");
}

function isRune(stack) {
    return getItemFromStack(stack) instanceof RuneItem;
}

function addMappers(mappers) {
    var ctx = mappers.context();
    var oneManaEmc = mana2emc(1);

    var livingRock = Ingredient.decode(ItemStack.create(getItem("livingrock")));

    Recipe.mapRecipeType("${mod}:elven_trade", function (recipe, output, inputs) {
        var outputs = List.arrayList();
        var outsIS = recipe.getOutputs();
        for (var i = 0; i < outsIS.size(); ++i)
            outputs.add(Ingredient.decode(outsIS.get(i)));
        mappers.multiMap(outputs, Ingredient.decode(inputs));
    });

    Recipe.mapRecipeType("${mod}:mana_infusion", function (recipe, output, inputs) {
        var lst = List.arrayList();
        lst.add(Ingredient.decode(inputs.get(0)));
        lst.add(ctx.forEMC(oneManaEmc * recipe.getManaToConsume()).stack(1));

        if (ItemStack.isInTag(output, "botania:special_flowers")) {
            mappers.pushForcefulMapping();
            mappers.map(output, lst);
            mappers.popForcefulMapping();
        } else mappers.map(output, lst);
    });

    Recipe.mapRecipeType("${mod}:petal_apothecary", function (recipe, output, inputs) {
        var lst = List.arrayList();
        lst.addAll(Ingredient.decode(inputs));
        lst.add(Ingredient.decode(recipe.getReagent()));

        if (ItemStack.isInTag(output, "botania:special_flowers")) {
            mappers.pushForcefulMapping();
            mappers.map(output, lst);
            mappers.popForcefulMapping();
        } else mappers.map(output, lst);
    });

    Recipe.mapRecipeType("${mod}:pure_daisy", function (recipe, output, inputs) {
        mappers.map(ItemStack.fromState(recipe.getOutputState()),
            Ingredient.decode(recipe.getInput().getDisplayedStacks())
        );
    });

    Recipe.mapRecipeType("${mod}:runic_altar", function (recipe, output, inputs) {
        var lst = List.arrayList();
        for(var i = 0; i < inputs.size(); ++i) {
            lst.add(ItemStack.toIngredientIf(inputs.get(i), function (st0) {
                return !isRune(st0);
            }));
        }
        lst.add(livingRock);
        lst.add(ctx.forEMC(oneManaEmc * recipe.getManaUsage()).stack(1));
        mappers.map(output, lst);
    });

    Recipe.mapRecipeType("${mod}:terra_plate", function (recipe, output, inputs) {
        var lst = List.arrayList();
        lst.addAll(Ingredient.decode(inputs));
        lst.add(ctx.forEMC(oneManaEmc * 600000).stack(1));
        mappers.map(output, lst);
    });
}