package sammobewick.pocketkitchen.data_objects;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Sam on 01/02/2017.
 */
public class Ingredient implements Serializable {
    // Matches: Data > GET Get Recipe Information
    // Usage: represents an ingredient from a recipe.
    private int     id;
    private String  aisle;
    private String  image;
    private String  name;
    private float   amount;
    private String  unitShort;
    private String  unitLong;
    private String  originalString;         // this includes the meta information in a String usually.

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Ingredient(String aisle, float amount, int id, String image, String name, String originalString, String unitLong, String unitShort) {
        this.aisle = aisle;
        this.amount = amount;
        this.id = id;
        this.image = image;
        this.name = name;
        this.originalString = originalString;
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
        this.name               = ingredient.getName();
        this.originalString     = ingredient.getOriginalString();
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

    public String getUnitShort() {
        return unitShort;
    }

    public String getAisle() {
        return aisle;
    }

    public String getImage() {
        return image;
    }

    public String getOriginalString() {
        return originalString;
    }

    public String getUnitLong() {
        return unitLong;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    // TODO: Not worth having the full toString so create your own essential information version!


    @Override
    public String toString() {
        return "Ingredient{" +
                "aisle='" + aisle + '\'' +
                ", id=" + id +
                ", image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", unitShort='" + unitShort + '\'' +
                ", unitLong='" + unitLong + '\'' +
                ", originalString='" + originalString + '\'' +
                '}';
    }
}
