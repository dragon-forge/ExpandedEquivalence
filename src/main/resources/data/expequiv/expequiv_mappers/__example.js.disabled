/*
* This script is an example code for a module
*/

/*

// This scripting API provides the following structures that can be used:

// in this struct, configKey should be without spaces, and words are going to be separated for config description (ex. ConfigKey => Config Key)
struct Configs
{
    void addEMC(Item item, String configKey, long EMC);

    void addEMC(Item item, int metadata, String configKey, long EMC);
	
    void addEMC(Item item, String configKey, String displayName, long EMC);

    void addEMC(Item item, int metadata, String configKey, String displayName, long EMC);

    void addEMC(ItemStack stack, String configKey, long EMC);

    void addEMC(ItemStack stack, String configKey, String displayName, long EMC);
}

struct EMCMapping
{
    void map(Ingredient output, Ingredient... inputs);

	// EMC per one fake item
    FakeIngredient fake(long EMC);
}

struct FakeIngredient
{
    CountedIngredient stack(int amount);
}

struct Ingredient
{
    static Ingredient of(Item item, int amount);

    static Ingredient of(Item item, int metadata, int amount);
	
	static Ingredient of(int amount, NativeIngredient ingredient);
	
	static Ingredient of(FluidStack fluid);
	
	static Ingredient merge(Collection<Ingredient> ingredients);
	
	static boolean isEmpty(NativeIngredient ingredient);
	
	static @Nullable Ingredient decode(Object rawIngredient);
}

struct Data
{
    static void setup(String key, Object value);

    static void set(String key, Object value);

    static Object get(String key);
}

struct Reflection
{
    static Object getField(Class type, String staticFieldName);

    static Object getField(Object instance, String fieldName);
}

struct ItemStack
{
	static NativeItemStack EMPTY;
	
	static boolean isEmpty(NativeItemStack stack);
	
	static int getMetadata(NativeItemStack stack);
	
	static NativeItem getItem(NativeItemStack stack);
	
	static NativeItemStack copy(NativeItemStack stack);
	
	static NativeItemStack of(NativeItem item);
	
	static NativeItemStack of(NativeItem item, int amount);
	
	static NativeItemStack of(NativeItem item, int amount, int metadata);
	
	static NativeItemStack fromState(NativeBlockState state);
}

struct Lists
{
	static List<T> arrayList();
	
	static IntList intList();
	
	static Set<T> hashSet();
	
	static Stream<T> stream(T[] array);
	
	static Stream<T> stream(Collection<T> collection);
	
	static Stream<T> stream(Iterable<T> iterable);
}

struct Vanilla
{
	static Iterable<NativeRecipe> getCraftingRecipes();
	
	static NativeItemStack getRecipeOutput(NativeRecipe recipe);
	
	static NativeIngredient getIngredients(NativeRecipe recipe);
}

/// Static methods that can be used directly in the script:
struct this
{
	static warn(String msg);
	
	static error(String msg);
	
	static Item getItem(String namespace, String key);
}

struct MapperList
{
	void addMapper(String MapperFunc);
}

MapperFunc should be function(EMCMapping)

*/



function registerEMC(configs)
{
	
}

function addMappers(mappers)
{
	
}

function setupData()
{
    // Data.setup("key", value /*anything*/);
}

function tweakData()
{
    // Data.set("key", value /*anything*/);
}