package sammobewick.pocketkitchen.data_objects;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Sam on 01/02/2017.
 */
public class Ingredient implements Serializable {
    // Matches: Data > GET Get Recipe Information
    // Usage: represents an ingredient from a recipe.
    private int id;
    private String aisle;
    private String image;
    private String name;
    private float amount;
    private String unitShort;
    private String unitLong;
    private String originalString;

    private boolean custom;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    // Standard Ingredient
    public Ingredient(String aisle, float amount, int id, String image, String name, String originalString, String unitLong, String unitShort) {
        this.aisle = aisle;
        this.amount = amount;
        this.id = id;
        this.image = image;
        this.name = name;
        this.originalString = originalString;
        this.unitLong = unitLong;
        this.unitShort = unitShort;

        // Initialise these to false. They are both set through setters when applicable.
        this.custom = false;
    }

    // Custom Ingredient
    public Ingredient(float amount, String name, String unitShort) {
        this.aisle = "";            // UNUSED
        this.amount = amount;
        this.id = 0;                // UNUSED
        this.name = name;
        this.originalString = "";   // UNUSED
        this.unitLong = unitShort;  // REPLICATED
        this.unitShort = unitShort;
        this.image = "";            // UNUSED
        this.custom = true;         // CUSTOM
    }


    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Ingredient(String json) {
        Gson gson = new Gson();
        Ingredient ingredient = gson.fromJson(json, Ingredient.class);
        this.aisle = ingredient.getAisle();
        this.amount = ingredient.getAmount();
        this.id = ingredient.getId();
        this.image = ingredient.getImage();
        this.name = ingredient.getName();
        this.originalString = ingredient.getOriginalString();
        this.unitLong = ingredient.getUnitLong();
        this.unitShort = ingredient.getUnitShort();
        this.custom = false;
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      MERGE + EQUALS:                                       //
    // ****************************************************************************************** //

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        // This is only reached when we know it's the same class!
        Ingredient that = (Ingredient) obj;
        return Objects.equals(id, that.getId()) &&
                Objects.equals(aisle, that.getAisle()) &&
                Objects.equals(name, that.getName()) &&
                Objects.equals(unitLong, that.unitLong) &&
                Objects.equals(unitShort, that.unitShort) &&
                Objects.equals(custom, that.isCustom());    // END-RETURN
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, aisle, name, unitLong, unitShort);
    }

    public Ingredient combineIngredient(Ingredient ingredient) {
        assert (this.equals(ingredient));
        return new Ingredient(
                this.aisle,
                this.amount + ingredient.amount,
                this.id,
                this.image,
                this.name,
                this.originalString,
                this.unitLong,
                this.unitShort
        );  // END-RETURN
    }

    public Ingredient removeIngredient(Ingredient ingredient) {
        assert (this.equals(ingredient));
        return new Ingredient(
                this.aisle,
                this.amount - ingredient.amount,
                this.id,
                this.image,
                this.name,
                this.originalString,
                this.unitLong,
                this.unitShort
        );  // END-RETURN
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

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnitShort(String unitShort) {
        this.unitShort = unitShort;
    }

    public void setUnitLong(String unitLong) {
        this.unitLong = unitLong;
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
                ", custom=" + custom +
                '}';
    }
}
