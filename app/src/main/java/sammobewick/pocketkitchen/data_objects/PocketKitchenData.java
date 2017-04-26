package sammobewick.pocketkitchen.data_objects;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.supporting.DataListener;
import sammobewick.pocketkitchen.supporting.MapHelper;

/**
 * Created by Sam on 22/02/2017.
 */

public final class PocketKitchenData {

    private static PocketKitchenData        instance;
    private static ArrayList<DataListener>  listeners = new ArrayList<>();

    // Temporary list of last search results, prevents results being destroyed. Not saved to file!
    private List<Recipe_Short> recipesDisplayed;

    // Saved list of recipes the user wants to cook.
    private List<Recipe_Short> recipesToCook;

    // Saved list of ingredients from the recipes they want to cook AND custom ingredients.
    private Map<Integer, List<Ingredient>> ingredientsRequired;

    // Attempt at making images work better:
    private static Map<String, Bitmap> cachedDrawables;

    // Saved list of ingredients in their cupboards.
    private List<Ingredient> inCupboards;

    // Saved list of recipe_shorts to allow fetching from Amazon:
    private List<Recipe_Short> myCustomRecipes;

    // This shouldn't be stored to file, but is a runtime calculated list of things they need.
    private List<Ingredient> toBuy;

    // ****************************************************************************************** //
    //                                      CONSTRUCTOR + INSTANCE:                               //
    // ****************************************************************************************** //

    private PocketKitchenData() { /* Empty Constructor */ }

    public static PocketKitchenData getInstance() {
        if (instance == null) {
            instance = new PocketKitchenData();
        }
        return instance;
    }

    public static PocketKitchenData getInstance(DataListener listener) {
        if (instance == null) {
            instance = new PocketKitchenData();
        }
        if (listener != null) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
        return instance;
    }

    // ****************************************************************************************** //
    //                                      LISTENERS:                                            //
    // ****************************************************************************************** //

    public boolean listen(DataListener listener) {
        if (listener != null) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
                return true;
            }
        }
        return false;
    }

    public boolean ignore(DataListener listener) {
        if (listener != null) {
            if (listeners.contains(listener)) {
                listeners.remove(listener);
                return true;
            }
        }
        return false;
    }

    private void updateListeners() {
        updateToBuy();
        if (!listeners.isEmpty()) {
            for (int d = 0; d < listeners.size(); d++) {
                listeners.get(d).dataUpdate();
            }
        }
    }

    // ****************************************************************************************** //
    //                                      GETTERS + SETTERS:                                    //
    // ****************************************************************************************** //

    public List<Recipe_Short> getRecipesDisplayed() {
        return recipesDisplayed;
    }

    public Map<Integer, List<Ingredient>> getRecipe_ingredients() {
        return ingredientsRequired;
    }

    public List<Recipe_Short> getRecipesToCook() {
        return recipesToCook;
    }

    public List<Ingredient> getInCupboards() {
        return inCupboards;
    }

    public List<Recipe_Short> getMyCustomRecipes() {
        return myCustomRecipes;
    }

    public List<Ingredient> getToBuy() {
        return toBuy;
    }

    public void updateToBuy() {
        this.toBuy = MapHelper.mergeIngredients(ingredientsRequired);
    }

    // SETTERS - for loading data into the application:

    public void setRecipesToCook(List<Recipe_Short> recipesToCook) {
        if (recipesToCook == null & this.recipesToCook != null)
            this.recipesToCook.clear();
        else
            this.recipesToCook = recipesToCook;
        updateListeners();
    }

    public void setIngredientsRequired(Map<Integer, List<Ingredient>> ingredientsRequired) {
        if (ingredientsRequired == null & this.ingredientsRequired != null)
            this.ingredientsRequired.clear();
        else
            this.ingredientsRequired = ingredientsRequired;
        updateListeners();
    }

    public void setInCupboards(List<Ingredient> inCupboards) {
        if (inCupboards == null & this.inCupboards != null)
            this.inCupboards.clear();
        else
            this.inCupboards = inCupboards;
        updateListeners();
    }

    public void setMyCustomRecipes(List<Recipe_Short> myCustomRecipes) {
        if (myCustomRecipes == null & this.myCustomRecipes != null)
            this.myCustomRecipes.clear();
        else
            this.myCustomRecipes = myCustomRecipes;
        updateListeners();
    }

    // ****************************************************************************************** //
    //                                     RECIPES TO DISPLAY:                                    //
    // ****************************************************************************************** //

    public void addRecipesDisplayed(List<Recipe_Short> recipes) {
        if (recipesDisplayed == null)
            recipesDisplayed = new ArrayList<>();

        recipesDisplayed.addAll(recipes);
        updateListeners();
    }

    public void setRecipesDisplayed(List<Recipe_Short> recipes) {
        if (recipesDisplayed != null & recipes == null) { recipesDisplayed.clear(); }
        else { recipesDisplayed = recipes; }
        updateListeners();
    }

    // ****************************************************************************************** //
    //                                      RECIPES-TO-COOK:                                      //
    // ****************************************************************************************** //

    public boolean addOnlyRecipe(Recipe_Short recipe) {
        if (recipesToCook == null)
            recipesToCook = new ArrayList<>();

        if (!recipesToCook.contains(recipe))
            recipesToCook.add(recipe);

        updateListeners();

        return (recipesToCook.contains(recipe));
    }

    public boolean removeOnlyRecipe(Recipe_Short recipe) {
        if (recipesToCook != null) {
            if (recipesToCook.contains(recipe)) {
                recipesToCook.remove(recipe);
                updateListeners();
            }
            return !recipesToCook.contains(recipe);
        }
        return false;
    }

    public boolean addRecipeToCookList(Recipe_Short recipe, List<Ingredient> ingredient_set) {
        int id = recipe.getId();

        if (ingredientsRequired == null) {
            ingredientsRequired = new HashMap<>();
        }
        if (recipesToCook == null) {
            recipesToCook = new ArrayList<>();
        }

        if (!ingredientsRequired.containsKey(id))
            ingredientsRequired.put(id, ingredient_set);

        if (!recipesToCook.contains(recipe))
            recipesToCook.add(recipe);

        updateListeners();

        return (ingredientsRequired.containsKey(id) & recipesToCook.contains(recipe));
    }

    public boolean removeRecipeFromCookList(Recipe_Short recipe) {
        int id = recipe.getId();

        if (ingredientsRequired != null) {
            if (ingredientsRequired.containsKey(id)) {
                ingredientsRequired.remove(id);
            }
        }
        if (recipesToCook != null) {
            if (recipesToCook.contains(recipe)) {
                recipesToCook.remove(recipe);
            }
        }

        updateListeners();

        return (!ingredientsRequired.containsKey(id) & !recipesToCook.contains(recipe));
    }

    public boolean updateRecipeInCooklist(Recipe_Short recipe, List<Ingredient> ingredient_set) {
        int id = recipe.getId();

        if (ingredientsRequired != null & recipesToCook != null) {

            if (ingredientsRequired.containsValue(ingredient_set)) {
                ingredientsRequired.put(id, ingredient_set);
            }
            for (Recipe_Short r : recipesToCook) {
                if (r.getId() == recipe.getId()) {
                    recipesToCook.set(recipesToCook.indexOf(r), recipe);
                    break;
                }
            }
        } else {
            return false;
        }

        updateListeners();

        return ingredientsRequired.containsKey(id);
    }

    public boolean checkForSetOfIngredients(Recipe_Short recipe) {
        return ingredientsRequired != null & recipesToCook != null && (ingredientsRequired.containsKey(recipe.getId()) & recipesToCook.contains(recipe));
    }

    public boolean checkForSavedRecipe(Recipe_Short recipe) {
        return recipesToCook != null && (recipesToCook.contains(recipe));
    }

    // ****************************************************************************************** //
    //                                      INGREDIENT CHANGES:                                   //
    // ****************************************************************************************** //

    public boolean removeIngredient(Ingredient item) {
        float amount = item.getAmount();

        if (ingredientsRequired != null) {
            for (Map.Entry<Integer, List<Ingredient>> entry : ingredientsRequired.entrySet()) {
                int id = entry.getKey();
                List<Ingredient> ingredientList = entry.getValue();

                System.out.println("REM-ING: Comparing items...");
                for (int z = 0; z < ingredientList.size(); z++) {
                    Ingredient i = ingredientList.get(z);
                    if (i.equals(item)) {
                        float iAmount = i.getAmount();
                        System.out.println("REM-ING: amount=" + amount + ", iAmount=" + iAmount);
                        if (amount >= iAmount) {
                            // Remove this ingredient entirely, as we would go negative amount:
                            System.out.println("REM-ING: Reducing by a portion.");
                            amount -= iAmount;
                            ingredientList.remove(item);
                        } else if (amount > 0) {
                            // Replace existing ingredient which will have its amount reduced:
                            System.out.println("REM-ING: Final reduction.");
                            int id2 = ingredientList.indexOf(i);
                            i.removeIngredient(item);
                            ingredientList.set(id2, i);
                            amount = -1;
                            break;
                        }
                    }
                }
                ingredientsRequired.put(id, ingredientList);

                // Exit all iterations if we've reduced amount enough:
                if (amount < 0) break;
            }
            if (ingredientsRequired.get(item.getId()) != null) {
                System.out.println("NULL RETURN - REMOVING REFERENCE");
                ingredientsRequired.remove(item.getId());
            }
            updateListeners();
        }
        // Final check + return:
        if (amount < 0) {
            updateListeners();
            return true;
        } else {
            return false;
        }
    }

    // ****************************************************************************************** //
    //                                      CUSTOM INGREDIENTS:                                   //
    // ****************************************************************************************** //

    public boolean addCustomIngredient(Ingredient item) {
        if (ingredientsRequired == null)
            ingredientsRequired = new HashMap<>();

        List<Ingredient> customSet;

        if (!ingredientsRequired.containsKey(0)) {
            customSet = new ArrayList<>();
        } else {
            customSet = ingredientsRequired.get(0);
        }

        customSet.add(item);
        ingredientsRequired.put(0, customSet);

        updateListeners();

        return (customSet.contains(item) & ingredientsRequired.containsValue(customSet));
    }

    public boolean removeCustomIngredient(Ingredient item) {
        if (ingredientsRequired == null) {
            return false;
        }

        List<Ingredient> customSet;

        if (ingredientsRequired.containsKey(0)) {
            customSet = ingredientsRequired.get(0);
            if (customSet.contains(item)) {
                customSet.remove(item);
                ingredientsRequired.put(0, customSet);
                updateListeners();
                return true;
            }
        }
        return false;
    }

    public boolean updateCustomIngredient(Ingredient old, Ingredient item) {
        if (ingredientsRequired == null) {
            return false;
        }

        List<Ingredient> customSet;

        if (ingredientsRequired.containsKey(0)) {
            customSet = ingredientsRequired.get(0);

            if (customSet.contains(old)) {
                customSet.set(customSet.indexOf(old), item);
                ingredientsRequired.put(0, customSet);
                updateListeners();
                return true;
            }
        }
        return false;
    }

    // ****************************************************************************************** //
    //                                      INGREDIENTS IN CUPBOARDS:                             //
    // ****************************************************************************************** //

    public boolean addToCupboard(Ingredient item) {
        if (inCupboards == null)
            inCupboards = new ArrayList<>();

        inCupboards.add(item);
        updateListeners();

        return inCupboards.contains(item);
    }

    public boolean removeFromCupboard(Ingredient item) {
        if (inCupboards != null) {
            if (inCupboards.contains(item)) {
                inCupboards.remove(item);
                updateListeners();
                return !inCupboards.contains(item);
            }
        }
        return false;
    }

    public boolean updateInCupbaord(Ingredient existing, Ingredient replacement) {
        if (inCupboards != null) {
            if (inCupboards.contains(existing)) {
                inCupboards.set(inCupboards.indexOf(existing), replacement);
                updateListeners();
                return inCupboards.contains(replacement);
            }
        }
        return false;
    }

    public String getIngredientQuery() {
        String result = "";
        if (inCupboards == null)
            return result;

        for (Ingredient i: inCupboards) {
            result += i.getName() + ", ";
        }
        return result;
    }

    // ****************************************************************************************** //
    //                                      MY CUSTOM RECIPES:                                    //
    // ****************************************************************************************** //

    public boolean addToMyRecipes(Recipe_Short item) {
        if (myCustomRecipes == null)
            myCustomRecipes = new ArrayList<>();

        myCustomRecipes.add(item);
        updateListeners();

        return myCustomRecipes.contains(item);
    }

    public boolean removeFromMyRecipes(Recipe_Short item) {
        this.removeRecipeFromCookList(item);
        if (myCustomRecipes != null) {
            if (myCustomRecipes.contains(item)) {
                myCustomRecipes.remove(item);
                updateListeners();
                return !myCustomRecipes.contains(item);
            }
        }
        return false;
    }

    public boolean updateInMyRecipes(Recipe_Short existing, Recipe_Short replacement) {
        if (myCustomRecipes != null) {
            if (myCustomRecipes.contains(existing)) {
                myCustomRecipes.set(myCustomRecipes.indexOf(existing), replacement);
                updateListeners();
                return myCustomRecipes.contains(replacement);
            }
        }
        return false;
    }

    // ****************************************************************************************** //
    //                                   CACHED DRAWABLES:                                        //
    // ****************************************************************************************** //

    private static boolean checkForDrawable(String url) {
        return cachedDrawables != null && cachedDrawables.containsKey(url);

    }

    public static Bitmap getDrawable(String url) {
        if (checkForDrawable(url)) {
            return cachedDrawables.get(url);
        }
        return null;
    }

    public static void putDrawable(String url, Bitmap drawable) {
        if (cachedDrawables == null)
            cachedDrawables = new HashMap<>();

        if (!checkForDrawable(url))
            cachedDrawables.put(url, drawable);
    }

    public static void clearDrawables() {
        if (cachedDrawables != null)
            cachedDrawables.clear();
    }
}
