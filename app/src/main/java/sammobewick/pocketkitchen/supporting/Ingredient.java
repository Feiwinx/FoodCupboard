package sammobewick.pocketkitchen.supporting;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 01/02/2017.
 */
public class Ingredient {
    // Matches: Data > GET Get Recipe Information
    // Usage: represents an ingredient from a recipe.
    private int     id;
    private String  aisle;
    private String  image;
    private String  name;
    private float   amount;
    private String  unit;                   // would like to leave out
    private String  unitShort;
    private String  unitLong;
    private String  originalString;         // this includes the meta information in a String usually.
    private List<String> metaInformation;   // would like to leave this out due to the originalString.

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Ingredient(String aisle, float amount, int id, String image, List<String> metaInformation, String name, String originalString, String unit, String unitLong, String unitShort) {
        this.aisle = aisle;
        this.amount = amount;
        this.id = id;
        this.image = image;
        this.metaInformation = metaInformation;
        this.name = name;
        this.originalString = originalString;
        this.unit = unit;
        this.unitLong = unitLong;
        this.unitShort = unitShort;
    }

    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Ingredient(String json) {
        Gson gson = new Gson();
        Ingredient ingredient = gson.fromJson(json, Ingredient.class);
        this.aisle              = ingredient.getAisle();
        this.amount             = ingredient.getAmount();
        this.id                 = ingredient.getId();
        this.image              = ingredient.getImage();
        this.metaInformation    = ingredient.getMetaInformation();
        this.name               = ingredient.getName();
        this.originalString     = ingredient.getOriginalString();
        this.unit               = ingredient.getUnit();
        this.unitLong           = ingredient.getUnitLong();
        this.unitShort          = ingredient.getUnitShort();
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      GETTERS:                                              //
    // ****************************************************************************************** //

    public float getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getUnitShort() {
        return unitShort;
    }

    public String getAisle() {
        return aisle;
    }

    public String getImage() {
        return image;
    }

    public List<String> getMetaInformation() {
        return metaInformation;
    }

    public String getOriginalString() {
        return originalString;
    }

    public String getUnitLong() {
        return unitLong;
    }

    // ****************************************************************************************** //
    //                                      SETTERS:                                              //
    // ****************************************************************************************** //



    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    // TODO: Not worth having the full toString so create your own essential information version!
}
