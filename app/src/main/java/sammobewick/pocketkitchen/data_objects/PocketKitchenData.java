package sammobewick.pocketkitchen.data_objects;

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

    private static PocketKitchenData instance;

    // Temporary list of last search results, prevents results being destroyed. Not saved to file!
    private List<Recipe_Short> recipesDisplayed;

    // Saved list of recipes the user wants to cook.
    private List<Recipe_Short> recipesToCook;

    // Saved list of ingredients from the recipes they want to cook AND custom ingredients.
    private Map<Integer, List<Ingredient>> ingredientsRequired;

    // Saved list of ingredients in their cupboards.
    private List<Ingredient> inCupboards;

    // This shouldn't be stored to file, but is a runtime calculated list of things they need.
    private List<Ingredient> toBuy;

    private static ArrayList<DataListener> listeners = new ArrayList<>();

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

    public void updateListeners() {
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

    public void setRecipesDisplayed(List<Recipe_Short> recipesDisplayed) {
        this.recipesDisplayed = recipesDisplayed;
        updateListeners();
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

    public List<Ingredient> getToBuy() {
        return toBuy;
    }

    public void updateToBuy() {
        this.toBuy = MapHelper.mergeIngredients(ingredientsRequired);
    }

    public void setRecipesToCook(List<Recipe_Short> recipesToCook) {
        this.recipesToCook = recipesToCook;
    }

    public void setIngredientsRequired(Map<Integer, List<Ingredient>> ingredientsRequired) {
        this.ingredientsRequired = ingredientsRequired;
    }

    public void setInCupboards(List<Ingredient> inCupboards) {
        this.inCupboards = inCupboards;
    }

    // ****************************************************************************************** //
    //                                      RECIPES-TO-COOK:                                      //
    // ****************************************************************************************** //

    public boolean addRecipeToCookList(Recipe_Short recipe, List<Ingredient> ingredient_set) {
        int id = recipe.getId();

        if (ingredientsRequired == null) {
            ingredientsRequired = new HashMap<>();
        }
        if (recipesToCook == null) {
            recipesToCook = new ArrayList<>();
        }

        ingredientsRequired.put(id, ingredient_set);
        recipesToCook.add(recipe);

        updateListeners();

        return (ingredientsRequired.containsKey(id) & recipesToCook.contains(recipe));
    }

    public boolean removeRecipeFromCookList(Recipe_Short recipe) {
        int id = recipe.getId();

        if (ingredientsRequired != null) {
            if (ingredientsRequired.containsKey(id)) {
                ingredientsRequired.remove(id);
            } else {
                return false;
            }
        }
        if (recipesToCook != null) {
            if (recipesToCook.contains(recipe)) {
                recipesToCook.remove(recipe);
            } else {
                return false;
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
        if (ingredientsRequired == null) {
            ingredientsRequired = new HashMap<>();
        }

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
            } else {
                return false;
            }
        } else {
            return false;
        }
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
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    // ****************************************************************************************** //
    //                                      INGREDIENTS IN CUPBOARDS:                             //
    // ****************************************************************************************** //

    public boolean addToCupboard(Ingredient item) {
        if (inCupboards == null) {
            inCupboards = new ArrayList<>();
        }

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
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean updateInCupbaord(Ingredient existing, Ingredient replacement) {
        if (inCupboards != null) {
            if (inCupboards.contains(existing)) {
                inCupboards.set(inCupboards.indexOf(existing), replacement);
                updateListeners();
                return inCupboards.contains(replacement);
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
