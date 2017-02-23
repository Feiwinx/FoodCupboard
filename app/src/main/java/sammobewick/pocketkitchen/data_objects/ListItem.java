package sammobewick.pocketkitchen.data_objects;

import com.google.gson.Gson;

import java.util.Objects;

/**
 * Created by Sam on 06/02/2017.
 */

public class ListItem {
    // Variables - Names match the API JSON format used in Ingredient:
    private int     id;
    private float   amount;
    private String  unit;
    private String  name;
    private boolean custom;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public ListItem(float amount, int id, String name, String unit) {
        this.amount = amount;
        this.id     = id;
        this.name   = name;
        this.unit   = unit;
        this.custom = false;
    }

    public ListItem(float amount, int id, String name, String unit, boolean custom) {
        this.amount = amount;
        this.id     = id;
        this.name   = name;
        this.unit   = unit;
        this.custom = custom;
    }

    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public ListItem(String json) {
        Gson gson = new Gson();
        ListItem item = gson.fromJson(json, ListItem.class);
        this.amount = item.getAmount();
        this.id     = item.getId();
        this.name   = item.getName();
        this.unit   = item.getUnit();
        this.custom = false;
    }

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      COMBINATION METHOD:                                   //
    // ****************************************************************************************** //

    public ListItem mergeAdd(ListItem item) {
        return new ListItem(
                this.amount + item.getAmount(),
                this.id,
                this.name,
                this.unit
        );
    }

    public ListItem mergeMin(ListItem item) {
        return new ListItem(
                this.amount - item.getAmount(),
                this.id,
                this.name,
                this.unit
        );
    }

    /**
     * We are overriding this method so we can easily check if this ListItem is the same as another.
     * We discount any difference in their amount, and check the rest is the same.
     * @return int - the hash value.
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.unit, this.name);
    }

    /**
     * We are overriding this method so we can easily check if this ListItem is the same as another.
     * We discount any difference in their amount, and check the rest is the same.
     * @param obj - the object to compare with.
     * @return boolean - are they the same or not?
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ListItem li = (ListItem) obj;
        return (Objects.equals(this.id, li.getId()) &&
                Objects.equals(this.name, li.getName()) &&
                Objects.equals(this.unit, li.getUnit()));
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

    public boolean isCustom() {
        return custom;
    }

    // ****************************************************************************************** //
    //                                      SETTERS:                                              //
    // ****************************************************************************************** //

    public void setAmount(float amount) {
        this.amount = amount;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    @Override
    public String toString() {
        return "ListItem{" +
                "amount=" + amount +
                ", id=" + id +
                ", unit='" + unit + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
