package sammobewick.pocketkitchen.data_objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 22/02/2017.
 */

public class PocketKitchenData {

    private static PocketKitchenData        instance;

    private List<Recipe_Short>              recipes;            // RECIPES LAST LOADED
    private List<Ingredient_Search>         ingredients;        // INGREDIENTS IN KITCHEN
    private List<ListItem>                  listItems;          // CUSTOM INGREDIENTS
    private Map<Integer, List<Ingredient>>  recipe_ingredients; // INGREDIENTS FROM SAVED RECIPES

    private PocketKitchenData() { /* Empty Constructor */ }

    public static PocketKitchenData getInstance() {
        if (instance == null) {
            instance = new PocketKitchenData();
        }
        return instance;
    }

    // Getters + Setters:

    public List<Ingredient_Search> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient_Search> ingredients) {
        this.ingredients = ingredients;
    }

    public List<ListItem> getListItems() {
        return listItems;
    }

    public void setListItems(List<ListItem> listItems) {
        this.listItems = listItems;
    }

    public List<Recipe_Short> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe_Short> recipes) {
        this.recipes = recipes;
    }

    public Map<Integer, List<Ingredient>> getRecipe_ingredients() {
        return recipe_ingredients;
    }

    // Adding / Removing / Updating:

    public boolean addSetOfIngredients(int id, List<Ingredient> ingredients) {
        if (recipe_ingredients != null) {
            recipe_ingredients.put(id, ingredients);
        } else {
            recipe_ingredients = new HashMap<>();
            recipe_ingredients.put(id, ingredients);
        }
        return recipe_ingredients.containsKey(id);
    }

    public boolean removeSetOfIngredients(int id) {
        if (recipe_ingredients != null & recipe_ingredients.containsKey(id)) {
            recipe_ingredients.remove(id);
        }
        return !recipe_ingredients.containsKey(id);
    }

    public boolean updateSetOfIngredients(int id, List<Ingredient> ingredients) {
        if (recipe_ingredients != null & recipe_ingredients.containsKey(id)) {
            recipe_ingredients.put(id, ingredients);
        }
        return recipe_ingredients.containsKey(id);
    }

    public boolean checkForSetOfIngredients(int id) {
        if (recipe_ingredients != null) {
            return recipe_ingredients.containsKey(id);
        } else { return false; }
    }

    public boolean addToIngredients(Ingredient_Search item) {
        if (ingredients != null) {
            ingredients.add(item);
        } else {
            ingredients = new ArrayList<>();
            ingredients.add(item);
        }
        return ingredients.contains(item);
    }

    public boolean updateInIngredients(Ingredient_Search old, Ingredient_Search item) {
        if (ingredients != null & ingredients.contains(item)) {
            ingredients.set(ingredients.indexOf(old), item);
            return ingredients.contains(item);
        } else { return false; }
    }

    public boolean removeFromIngredients(Ingredient_Search item) {
        if (ingredients != null & ingredients.contains(item)) {
            ingredients.remove(item);
            return !ingredients.contains(item);
        } else { return false; }
    }

    public boolean addToListItems(ListItem item) {
        if (listItems != null) {
            listItems.add(item);
        } else {
            listItems = new ArrayList<>();
            listItems.add(item);
        }
        return listItems.contains(item);
    }

    public boolean removeFromListItems(ListItem item) {
        if (listItems != null & listItems.contains(item)) {
            listItems.remove(item);
            return !listItems.contains(item);
        } else { return false; }
    }

    public boolean updateInListItems(ListItem old, ListItem item) {
        if (listItems != null & listItems.contains(item)) {
            listItems.set(listItems.indexOf(old), item);
            return listItems.contains(item);
        } else { return false; }
    }
}
