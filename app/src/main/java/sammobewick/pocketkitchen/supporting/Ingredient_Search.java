package sammobewick.pocketkitchen.supporting;

/**
 * Created by Sam on 10/02/2017.
 */

public class Ingredient_Search {
    // Matches: Extract > POST Parse Ingredient
    // Usage: plain text search identifies the following attributes. I think this is to be used
    // for the kitchen fragement. Need to be able to store this in my own database???
    private int id;
    private String original;
    private String name;
    private float amount;
    private String unitShort;
    private String unitLong;
    private String aisle;
    private String image;
    //private List<String> meta;    // don't see the point in saving / storing this.
}
