package sammobewick.pocketkitchen.supporting;

import com.google.gson.Gson;

/**
 * Created by Sam on 06/02/2017.
 */

public class ListItem {
    // Variables - Names match the API JSON format used in Ingredient:
    private int     id;
    private float   amount;
    private String  unit;
    private String  name;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public ListItem(float amount, int id, String name, String unit) {
        this.amount = amount;
        this.id     = id;
        this.name   = name;
        this.unit   = unit;
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
