package sammobewick.pocketkitchen.supporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.data_objects.Ingredient;

/**
 * Helper class to handler manipulation for maps. Maps are used to store the ingredients
 * associated with recipes, and this class reduces the amount we have to store by allowing
 * us to manipulate the results into whatever we need.
 * <p>
 * Crucially, it also merges ingredients inside of the map, shortening our ListView. The main
 * way this works is in the Ingredient class, Equals & Hash are overridden.
 * <p>
 * Created by Sam on 25/02/2017.
 */

public abstract class MapHelper {

    /**
     * Merges a Map containing List<Ingredient> into a single ArrayList. Due to the comparisons
     * included in the Ingredient class, this allows us to combine the ingredients, simplifying
     * the map dramatically.
     * @param map Map<Integer, List<Ingredient>> - being the original data.
     * @return ArrayList<Ingredient> - being the simplified results.
     */
    public static List<Ingredient> mergeIngredients(Map<Integer, List<Ingredient>> map) {
        List<Ingredient> mergedList = new ArrayList<>();

        if (map == null) {
            return mergedList;
        }
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

    /**
     * Finds custom ingredients from within an entire map. Set up in case of need but seemingly not
     * useful to my application so far.
     * @param map Map<Integer, List<Ingredient>> - map to work with
     * @param custom boolean - whether to look for custom or non-custom items.
     * @return List<Ingredient> - being the resulting list.
     */
    public static List<Ingredient> filterMapForCustom(Map<Integer, List<Ingredient>> map, boolean custom) {
        List<Ingredient> results = new ArrayList<>();

        if (map == null) {
            return results;
        }
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

    /**
     * Flattens a map so a single list.
     * @param map Map<Integer, List<Ingredient>> - map to flattnen
     * @return List<Ingredient>> - all ingredients from the map.
     */
    public static List<Ingredient> flattenMap(Map<Integer, List<Ingredient>> map) {
        List<Ingredient> results = new ArrayList<>();

        if (map == null) {
            return results;
        }
        for (Map.Entry<Integer, List<Ingredient>> entry : map.entrySet()) {
            List<Ingredient> ingredients = entry.getValue();

            for (Ingredient i : ingredients) {
                results.add(i);
            }
        }
        return results;
    }
}
