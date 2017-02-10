package sammobewick.pocketkitchen.supporting;

import com.google.gson.Gson;

import java.util.List;

/**
 * Created by Sam on 01/02/2017.
 */
public class Recipe_Full {
    // Matches: Data > GET Get Recipe Information
    // Usage: Represents a full recipe, used in the process of viewing more info on a recipe.
    private boolean vegetarian;
    private boolean vegan;
    private boolean glutenFree;
    private boolean dairyFree;
    private boolean veryHealthy;
    private boolean cheap;
    private boolean veryPopular;
    private boolean sustainable;
    private int     weightWatcherSmartPoints;
    private String  gaps;
    private boolean lowFodmap;
    private boolean ketogenic;
    private boolean whole30;
    private int     servings;
    // private String sourceUrl;    // Left out as hopefully not needed!
    private String  spoonacularSourceUrl;
    // private String creditText;   // left out as hopefully not needed!
    private List<Ingredient> extendedIngredients;
    private int     id;
    private String  title;
    private int     readyInMinutes;
    private String  instructions;   // hopefully just a String?

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Recipe_Full(boolean cheap, boolean dairyFree, List<Ingredient> extendedIngredients, String gaps,
                       boolean glutenFree, int id, String instructions, boolean ketogenic, boolean lowFodmap,
                       int readyInMinutes, int servings, String spoonacularSourceUrl, boolean sustainable,
                       String title, boolean vegan, boolean vegetarian, boolean veryHealthy,
                       boolean veryPopular, int weightWatcherSmartPoints, boolean whole30) {
        this.cheap = cheap;
        this.dairyFree = dairyFree;
        this.extendedIngredients = extendedIngredients;
        this.gaps = gaps;
        this.glutenFree = glutenFree;
        this.id = id;
        this.instructions = instructions;
        this.ketogenic = ketogenic;
        this.lowFodmap = lowFodmap;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.spoonacularSourceUrl = spoonacularSourceUrl;
        this.sustainable = sustainable;
        this.title = title;
        this.vegan = vegan;
        this.vegetarian = vegetarian;
        this.veryHealthy = veryHealthy;
        this.veryPopular = veryPopular;
        this.weightWatcherSmartPoints = weightWatcherSmartPoints;
        this.whole30 = whole30;
    }


    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Recipe_Full(String json) {
        Gson gson = new Gson();
        Recipe_Full recipeFull = gson.fromJson(json, Recipe_Full.class);
        this.cheap                  = recipeFull.isCheap();
        this.dairyFree              = recipeFull.isDairyFree();
        this.extendedIngredients    = recipeFull.getExtendedIngredients();
        this.gaps                   = recipeFull.getGaps();
        this.glutenFree             = recipeFull.isGlutenFree();
        this.id                     = recipeFull.getId();
        this.instructions           = recipeFull.getInstructions();
        this.ketogenic              = recipeFull.isKetogenic();
        this.lowFodmap              = recipeFull.isLowFodmap();
        this.readyInMinutes         = recipeFull.getReadyInMinutes();
        this.servings               = recipeFull.getServings();
        this.spoonacularSourceUrl   = recipeFull.getSpoonacularSourceUrl();
        this.sustainable            = recipeFull.isSustainable();
        this.title                  = recipeFull.getTitle();
        this.vegan                  = recipeFull.isVegan();
        this.vegetarian             = recipeFull.isVegetarian();
        this.veryHealthy            = recipeFull.isVeryHealthy();
        this.veryPopular            = recipeFull.isVeryPopular();
        this.weightWatcherSmartPoints = recipeFull.getWeightWatcherSmartPoints();
        this.whole30                = recipeFull.isWhole30();
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      GETTERS:                                              //
    // ****************************************************************************************** //

    public boolean isCheap() {
        return cheap;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }

    public List<Ingredient> getExtendedIngredients() {
        return extendedIngredients;
    }

    public String getGaps() {
        return gaps;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public int getId() {
        return id;
    }

    public String getInstructions() {
        return instructions;
    }

    public boolean isKetogenic() {
        return ketogenic;
    }

    public boolean isLowFodmap() {
        return lowFodmap;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public int getServings() {
        return servings;
    }

    public String getSpoonacularSourceUrl() {
        return spoonacularSourceUrl;
    }

    public boolean isSustainable() {
        return sustainable;
    }

    public String getTitle() {
        return title;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public boolean isVeryHealthy() {
        return veryHealthy;
    }

    public boolean isVeryPopular() {
        return veryPopular;
    }

    public int getWeightWatcherSmartPoints() {
        return weightWatcherSmartPoints;
    }

    public boolean isWhole30() {
        return whole30;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    // TODO: Not worth having the full toString so create your own essential information version!
}
