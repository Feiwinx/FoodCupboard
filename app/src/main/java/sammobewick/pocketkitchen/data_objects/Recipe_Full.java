package sammobewick.pocketkitchen.data_objects;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

/**
 * Here we implement Serializable so we can pass this object between activities!
 * It also allows for it to be written to file.
 * Created by Sam on 01/02/2017.
 */
@DynamoDBTable(tableName = "PK_Recipes_Full")
public class Recipe_Full implements Serializable {
    // Matches: Data > GET Get Recipe Information
    // Usage: Represents a full recipe, used in the process of viewing more info on a recipe.
    private boolean vegetarian;
    private boolean vegan;
    private boolean glutenFree;
    private boolean dairyFree;
    private boolean veryHealthy;
    private boolean cheap;
    private boolean veryPopular;
    //private boolean sustainable;                  // Left out
    //private int     weightWatcherSmartPoints;     // Left out
    //private String  gaps;                         // Left out
    //private boolean lowFodmap;                    // Left out
    //private boolean ketogenic;                    // Left out
    //private boolean whole30;                      // Left out
    private int servings;
    // private String sourceUrl;                    // Left out
    private String spoonacularSourceUrl;
    private String creditText;                      // left out
    private List<Ingredient> extendedIngredients;
    private int id;
    private String title;
    private int readyInMinutes;
    private String instructions;

    // Elements required for custom (but not given in API):
    private boolean contEggs;
    private boolean contNuts;
    private boolean contSoy;
    private boolean contShellfish;
    private boolean contSeafood;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Recipe_Full(boolean cheap, boolean dairyFree, List<Ingredient> extendedIngredients,
                       boolean glutenFree, int id, String instructions,
                       int readyInMinutes, int servings, String spoonacularSourceUrl, String creditText,
                       String title, boolean vegan, boolean vegetarian, boolean veryHealthy,
                       boolean veryPopular) {
        this.cheap = cheap;
        this.dairyFree = dairyFree;
        this.extendedIngredients = extendedIngredients;
        this.glutenFree = glutenFree;
        this.id = id;
        this.instructions = instructions;
        this.readyInMinutes = readyInMinutes;
        this.servings = servings;
        this.spoonacularSourceUrl = spoonacularSourceUrl;
        this.creditText = creditText;
        this.title = title;
        this.vegan = vegan;
        this.vegetarian = vegetarian;
        this.veryHealthy = veryHealthy;
        this.veryPopular = veryPopular;

        // General creation of this object might not require these, so left as false:
        this.contEggs = false;
        this.contNuts = false;
        this.contSeafood = false;
        this.contShellfish = false;
        this.contSoy = false;
    }

    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Recipe_Full(String json) {
        Gson gson = new Gson();
        Recipe_Full recipeFull = gson.fromJson(json, Recipe_Full.class);
        this.cheap = recipeFull.isCheap();
        this.dairyFree = recipeFull.isDairyFree();
        this.extendedIngredients = recipeFull.getExtendedIngredients();
        this.glutenFree = recipeFull.isGlutenFree();
        this.id = recipeFull.getId();
        this.instructions = recipeFull.getInstructions();
        this.readyInMinutes = recipeFull.getReadyInMinutes();
        this.servings = recipeFull.getServings();
        this.spoonacularSourceUrl = recipeFull.getSpoonacularSourceUrl();
        this.creditText = recipeFull.getCreditText();
        this.title = recipeFull.getTitle();
        this.vegan = recipeFull.isVegan();
        this.vegetarian = recipeFull.isVegetarian();
        this.veryHealthy = recipeFull.isVeryHealthy();
        this.veryPopular = recipeFull.isVeryPopular();

        this.contEggs = recipeFull.isContEggs();
        this.contNuts = recipeFull.isContNuts();
        this.contSeafood = recipeFull.isContSeafood();
        this.contShellfish = recipeFull.isContShellfish();
        this.contSoy = recipeFull.isContSoy();
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      GETTERS:                                              //
    // ****************************************************************************************** //

    @DynamoDBAttribute(attributeName = "CHEAP")
    public boolean isCheap() {
        return cheap;
    }

    @DynamoDBAttribute(attributeName = "DAIRYFREE")
    public boolean isDairyFree() {
        return dairyFree;
    }

    @DynamoDBAttribute(attributeName = "EXTENDEDINGREDIENTS")
    public List<Ingredient> getExtendedIngredients() {
        return extendedIngredients;
    }

    @DynamoDBAttribute(attributeName = "GLUTENFREE")
    public boolean isGlutenFree() {
        return glutenFree;
    }

    @DynamoDBHashKey(attributeName = "ID")
    public int getId() {
        return id;
    }

    @DynamoDBAttribute(attributeName = "INSTRUCTIONS")
    public String getInstructions() {
        return instructions;
    }

    @DynamoDBAttribute(attributeName = "READYINTMINUTES")
    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    @DynamoDBAttribute(attributeName = "SERVINGS")
    public int getServings() {
        return servings;
    }

    @DynamoDBAttribute(attributeName = "SPOONACULARSOURCEURL")
    public String getSpoonacularSourceUrl() {
        return spoonacularSourceUrl;
    }

    @DynamoDBIndexRangeKey(attributeName = "CREDITTEXT")
    public String getCreditText() {
        return creditText;
    }

    @DynamoDBAttribute(attributeName = "TITLE")
    public String getTitle() {
        return title;
    }

    @DynamoDBAttribute(attributeName = "VEGAN")
    public boolean isVegan() {
        return vegan;
    }

    @DynamoDBAttribute(attributeName = "VEGETARIAN")
    public boolean isVegetarian() {
        return vegetarian;
    }

    @DynamoDBAttribute(attributeName = "VERYHEALTHY")
    public boolean isVeryHealthy() {
        return veryHealthy;
    }

    @DynamoDBAttribute(attributeName = "VERYPOPULAR")
    public boolean isVeryPopular() {
        return veryPopular;
    }

    // ****************************************************************************************** //
    //                             CUSTOM-FOCUSED GETTERS / SETTERS                               //
    // ****************************************************************************************** //

    @DynamoDBAttribute(attributeName = "CONTAINS_EGGS")
    public boolean isContEggs() {
        return contEggs;
    }

    @DynamoDBAttribute(attributeName = "CONTAINS_EGGS")
    public boolean isContNuts() {
        return contNuts;
    }

    @DynamoDBAttribute(attributeName = "CONTAINS_SOY")
    public boolean isContSoy() {
        return contSoy;
    }

    @DynamoDBAttribute(attributeName = "CONTAINS_SHELLFISH")
    public boolean isContShellfish() {
        return contShellfish;
    }

    @DynamoDBAttribute(attributeName = "CONTAINS_SEAFOOD")
    public boolean isContSeafood() {
        return contSeafood;
    }

    public void setContEggs(boolean contEggs) {
        this.contEggs = contEggs;
    }

    public void setContNuts(boolean contNuts) {
        this.contNuts = contNuts;
    }

    public void setContSoy(boolean contSoy) {
        this.contSoy = contSoy;
    }

    public void setContShellfish(boolean contShellfish) {
        this.contShellfish = contShellfish;
    }

    public void setContSeafood(boolean contSeafood) {
        this.contSeafood = contSeafood;
    }


    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    @Override
    public String toString() {
        return "Recipe_Full{" +
                "vegetarian=" + vegetarian +
                ", vegan=" + vegan +
                ", glutenFree=" + glutenFree +
                ", dairyFree=" + dairyFree +
                ", veryHealthy=" + veryHealthy +
                ", cheap=" + cheap +
                ", veryPopular=" + veryPopular +
                ", servings=" + servings +
                ", spoonacularSourceUrl='" + spoonacularSourceUrl + '\'' +
                ", creditText='" + creditText + '\'' +
                ", extendedIngredients=" + extendedIngredients +
                ", id=" + id +
                ", title='" + title + '\'' +
                ", readyInMinutes=" + readyInMinutes +
                ", instructions='" + instructions + '\'' +
                ", contEggs=" + contEggs +
                ", contNuts=" + contNuts +
                ", contSoy=" + contSoy +
                ", contShellfish=" + contShellfish +
                ", contSeafood=" + contSeafood +
                '}';
    }

    /**
     * Helper method to allow exporting of the Recipe to an Intent.
     * @return String - produce a readable + exportable Recipe string.
     */
    public String toExportableString() {
        String result = "";

        result += "Title: " + title;

        result += "Ingredients: ";
        for (Ingredient i: extendedIngredients) {
            result += (i.getAmount() + " " + i.getUnitShort() + " of " + i.getName() + "\n");
        }

        result += "Instructions: " + instructions + "\n";

        result += "Serves " + servings + " portions.\n";

        result += "Takes " + readyInMinutes + " minutes to make.";

        result += "Additional Information:\n"       +
                "Cheap: "           + cheap         +
                "\nVegetarian: "    + vegetarian    +
                "\nVegan: "         + vegan         +
                "\nGluten Free: "   + glutenFree    +
                "\nDairy Free: "    + dairyFree     +
                "\nHealthy: "       + veryHealthy   +
                "\nPopular: "       + veryPopular;

        result += "\n\nBy " + creditText;
        result += "\nAvailable: " + spoonacularSourceUrl;

        return result;
    }
}
