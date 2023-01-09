//#require Java.type("org.zeith.hammerlib.util.java.ReflectionUtil").fetchClass("ic2.api.core.IC2Classic") != null && Java.type("ic2.api.core.IC2Classic").getHelper() != null;

//#import org.zeith.hammerlib.util.java.ReflectionUtil;

//#import ic2.core.IC2;
//#import net.minecraftforge.registries.ForgeRegistries;
//#import ic2.core.item.misc.CellItem;
//#import net.minecraftforge.common.capabilities.ForgeCapabilities;
//#import ic2.api.recipes.ingridients.recipes.ChanceRecipeOutput;
//#import ic2.core.platform.recipes.misc.FoodCanRegistry;

function fakePopulateEMCTags(tags) {
    // This trickery allows us to fix missing EMC for aluminum because IC2 decided to name it as "Aluminium".
    tags.add(getItem("ic2", "ingot_aluminium"), "forge:ingots/aluminum");
}

function tweakData() {
    Data.set("minecraft:max_blaze_powders_from_rod", Math.max(5, Data.get("minecraft:max_blaze_powders_from_rod")));
}

function registerEMC(configs)
{
    configs.addEMC(getItem("ic2", "sticky_resin"), "StickyResin", 32);
    configs.addEMC(getItem("ic2", "coffee_beans"), "CoffeeBeans", 24);
    configs.addEMC(getItem("ic2", "uumatter"), "UuMatter", 8192 / 9);
    configs.addEMC(getItem("ic2", "ore_uranium"), "UraniumOre", 4096);
    configs.addEMC(getItem("ic2", "terrawart"), "Terrawart", 64);
}


function IInput(input, currentIng, inputItemFilter) {
    return ItemStack.toIngredientIf(input.asIngredient(), function (stack) {
        if (inputItemFilter) return inputItemFilter(stack, currentIng);
        return !ItemStack.doesRemain(stack);
    });
}

function handleIC2Recipe(mappers, IC2RecipeEntry, inputItemFilter) {
    var inputs = List.arrayList();

    // Add items that do not remain after recipe.
    var inputItems = IC2RecipeEntry.getInputs();
    for (var i = 0; i < inputItems.length; ++i) {
        var currentIng = i;
        var it = IInput(inputItems[i], currentIng, inputItemFilter);
        if (it && !it.isEmpty()) inputs.add(it);
    }

    var outputs = List.arrayList();

    var minIterations = 1;

    var out = IC2RecipeEntry.getOutput();
    if(out instanceof ChanceRecipeOutput) {  /* jdk.dynalink.beans.StaticClass.getRepresentedClass() converts to Java Class<?> */
        var chance = ReflectionUtil.lookupField(Class.forName("ic2.api.recipes.ingridients.recipes.ChanceRecipeOutput"), "chance").getFloat(out);
        minIterations = 1 / chance;
    }

    outputs.addAll(Ingredient.decode(out.getAllOutputs()));

    for (var i = 0; i < inputs.size(); ++i) inputs.set(i, inputs.get(i).stack(minIterations));

    mappers.multiMap(outputs, inputs);
}

function mapMachineRecipes(mappers, recipeRegistry, type) {
    var re = recipeRegistry.getAllEntries();
    info("Handling " + re.size() + " IC2-Classic recipes for " + type);
    for(var i = 0; i < re.size(); ++i) handleIC2Recipe(mappers, re.get(i));
}

function mapElectrolyzerRecipes(mappers, recipeList) {
    var re = recipeList.getRecipes();
    info("Handling " + re.size() + " IC2-Classic recipes for Electrolyzer");
    for(var i = 0; i < re.size(); ++i) {
        var r = re.get(i);
        mappers.map(Ingredient.decode(r.getOutput()), Ingredient.decode(r.getInput()));
    }
}

function mapRareEarthRecipes(mappers, recipeList) {
    var re = recipeList.getAllRecipes();
    info("Handling " + re.size() + " IC2-Classic recipes for Rare Earth Extractor");

    var rareEarth = getItem("ic2", "dust_rare_earth");
    for(var i = 0; i < re.size(); ++i) {
        var r = re.get(i);
        if(ItemStack.getItem(r.getOutput()) != rareEarth) continue;
        mappers.map(
            Ingredient.decode(r.getOutput()),
            Ingredient.decode(r.getInput()).stack(Math.ceil(1000.0 / r.getValue()))
        );
    }
}

function mapRefiningRecipes(mappers, recipeList) {
    var re = recipeList.getAllRecipes();
    info("Handling " + re.size() + " IC2-Classic recipes for Refinery");

    for(var i = 0; i < re.size(); ++i) {
        var r = re.get(i);

        var inputs = List.arrayList();
        var outputs = List.arrayList();

        if(!FluidStack.isEmpty(r.getFirstTank()))
            inputs.add(Ingredient.decode(r.getFirstTank()));

        if(!FluidStack.isEmpty(r.getSecondTank()))
            inputs.add(Ingredient.decode(r.getSecondTank()));

        var it = IInput(r.getItemInput(), 0);
        if (it && !it.isEmpty()) inputs.add(it);

        var res = r.getOutput();
        outputs.addAll(Ingredient.decode(res.getAllOutputs()));
        outputs.addAll(Ingredient.decode(res.getAllFluidOutputs()));

        mappers.multiMap(outputs, inputs);
    }
}

function getFoodAmount(stack) {
    return ItemStack.isEdible(stack) ? Math.max(1, Math.ceil(ItemStack.getNutrition(stack) / 2.0)) : (ItemStack.getItem(stack) == getItem1("cake") ? 6 : 0);
}

function addMappers(mappers)
{
    var RecipesIC2 = IC2.RECIPES.get(true); // true is for "simulating" a.k.a. server side of things.

    mappers.map(
        Ingredient.decode(FluidStack.create(getFluid("ic2", "alcohol"), 200)),
        Ingredient.decode(FluidStack.create(getFluid1("water"), 1000))
    );

    mapMachineRecipes(mappers, RecipesIC2.macerator, "Macerator");
    mapMachineRecipes(mappers, RecipesIC2.extractor, "Extractor");
    mapMachineRecipes(mappers, RecipesIC2.compressor, "Compressor");
    mapMachineRecipes(mappers, RecipesIC2.sawmill, "Sawmill");
    mapMachineRecipes(mappers, RecipesIC2.recycler, "Recycler");
    mapRareEarthRecipes(mappers, RecipesIC2.rare_earth);
    mapMachineRecipes(mappers, RecipesIC2.mixingFurnace, "Alloy Smelter");
    mapElectrolyzerRecipes(mappers, RecipesIC2.electrolyzer);
    mapRefiningRecipes(mappers, RecipesIC2.refining);

    mappers.map(
        Ingredient.decode(getItem("ic2", "cell_plasma")),
        Ingredient.decode(
            getItem("ic2", "cell_empty"),
            ItemStack.create(getItem("ic2", "uumatter"), 10)
        )
    );

    var items = ForgeRegistries.ITEMS.getValues().iterator();
    var emptyCell = getItem("ic2", "cell_empty");

    var foodCan = FoodCanRegistry.INSTANCE;

    while(items.hasNext()) {
        var item = items.next();
        var stack = ItemStack.create(item);

        if(item instanceof CellItem) {
            var fluidInCell = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
            if(fluidInCell && fluidInCell.getFluid && !FluidStack.isEmpty(fluidInCell.getFluid())) {
                mappers.map(stack,
                    Ingredient.decode(fluidInCell.getFluid()),
                    Ingredient.decode(emptyCell)
                );
            } else {
                warn("Invalid FluidHandlerItem capability for IC2-C CellItem!");
            }
        } else {
            var foodCanAmount = getFoodAmount(stack);
            if(foodCanAmount > 0) {
                var fcItem = foodCan.getItemForFood(stack);
                if(!fcItem) fcItem = getItem("ic2", "filled_tin_can");

                var output = ItemStack.create(fcItem, foodCanAmount);

                mappers.map(Ingredient.decode(output),
                    Ingredient.decode(stack),
                    Ingredient.decode(ItemStack.create(getItem("ic2", "tin_can"), foodCanAmount))
                );

                warn("MAP IC2 CLASSIC: " + stack + " + " + ItemStack.create(getItem("ic2", "tin_can"), foodCanAmount) + " -> " + output);
            }
        }
    }
}