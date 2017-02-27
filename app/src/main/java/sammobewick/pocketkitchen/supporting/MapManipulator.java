package sammobewick.pocketkitchen.supporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.data_objects.Ingredient;

/**
 * Helper class to handler manipulation for maps. Maps are used to store the ingredients
 * associated with recipes, and this class reduces the amount we have to store by allowing
 * us to manipulate the results into whatever we need.
 *
 * Crucially, it also merges ingredients inside of the map, shortening our ListView. The main
 * way this works is in the Ingredient class, Equals & Hash are overridden.
 *
 * Created by Sam on 25/02/2017.
 */

public class MapManipulator {

    public static List<Ingredient> mergeIngredients(Map<Integer, List<Ingredient>> map) {
        List<Ingredient> mergedList = new ArrayList<>();

        if (map == null) { return mergedList; }
        for (Map.Entry<Integer, List<Ingredient>> entry : map.entrySet()) {
                List<Ingredient> ingredients = entry.getValue();

                for (Ingredient i : ingredients) {
                    int index = mergedList.indexOf(i);
                    if (index != -1) {
                        mergedList.set(index, mergedList.get(index).combineIngredient(i));
                    } else {
                        mergedList.add(i);
                    }
                }
        }
        return mergedList;
    }

    public static List<Ingredient> filterMapForCustom(Map<Integer, List<Ingredient>> map, boolean custom) {
        List<Ingredient> results = new ArrayList<>();

        if (map == null) { return results; }
        for (Map.Entry<Integer, List<Ingredient>> entry : map.entrySet()) {
            List<Ingredient> ingredients = entry.getValue();

            for (Ingredient i : ingredients) {
                if (i.isCustom() == custom) {
                    results.add(i);
                }
            }
        }
        return results;
    }

    public static List<Ingredient> flattenMap(Map<Integer, List<Ingredient>> map) {
        List<Ingredient> results = new ArrayList<>();

        if (map == null) { return results; }
        for (Map.Entry<Integer, List<Ingredient>> entry : map.entrySet()) {
            List<Ingredient> ingredients = entry.getValue();

            for (Ingredient i : ingredients) {
                    results.add(i);
            }
        }
        return results;
    }
}
