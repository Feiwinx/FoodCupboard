package sammobewick.pocketkitchen.data_objects;

import com.google.gson.Gson;

/**
 * Created by Sam on 10/02/2017.
 */

public class Ingredient_Search {
    // Matches: Extract > POST Parse Ingredient
    // Usage: plain text search identifies the following attributes. Used when searching for kitchen items.
    private int id;
    private String original;
    private String name;
    private float amount;
    private String unitShort;
    private String unitLong;
    private String aisle;
    private String image;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Ingredient_Search(String aisle, float amount, int id, String image, String name, String original, String unitLong, String unitShort) {
        this.aisle = aisle;
        this.amount = amount;
        this.id = id;
        this.image = image;
        this.name = name;
        this.original = original;
        this.unitLong = unitLong;
        this.unitShort = unitShort;
    }

    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Ingredient_Search(String json){
        Gson gson = new Gson();
        Ingredient_Search ing = gson.fromJson(json, Ingredient_Search.class);
        this.id         = ing.getId();
        this.original   = ing.getOriginal();
        this.name       = ing.getName();
        this.amount     = ing.getAmount();
        this.unitShort  = ing.getUnitShort();
        this.unitLong   = ing.getUnitLong();
        this.aisle      = ing.getAisle();
        this.image      = ing.getImage();
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      GETTERS:                                              //
    // ****************************************************************************************** //

    public String getAisle() {
        return aisle;
    }

    public float getAmount() {
        return amount;
    }

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getOriginal() {
        return original;
    }

    public String getUnitLong() {
        return unitLong;
    }

    public String getUnitShort() {
        return unitShort;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    // TODO: Not worth having the full toString so create your own essential information version!

    @Override
    public String toString() {
        return "Ingredient_Search{" +
                "aisle='" + aisle + '\'' +
                ", id=" + id +
                ", original='" + original + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", unitShort='" + unitShort + '\'' +
                ", unitLong='" + unitLong + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
