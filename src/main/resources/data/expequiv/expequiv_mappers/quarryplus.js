//#define ${mod} quarryplus
//#require isModLoaded("${mod}");

//#define getItem( getItem("${mod}",

function setupData() {
}

function registerEMC(configs) {
}

function addMappers(mappers) {
    var ctx = mappers.context();

    Recipe.mapRecipeType("${mod}:workbench_recipe", function (recipe, output, inputs) {
        var inputIngrs = List.arrayList();

        inputs = recipe.inputs(); // List<IngredientList>

        for(var i = 0; i < inputs.size(); ++i) {
            var ing = inputs.get(i); // IngredientList
            inputIngrs.add(Ingredient.decode1(ing.stackList()));
        }

        mappers.map(output, inputIngrs);
    });
}