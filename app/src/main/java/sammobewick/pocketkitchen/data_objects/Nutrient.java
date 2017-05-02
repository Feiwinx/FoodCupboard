package sammobewick.pocketkitchen.data_objects;

import com.google.gson.Gson;

/**
 * Represents an Ingredient. The data is based off what is available from Spoonacular.
 * This was expected to be used throughout but as the API became more familiar it was found that
 * individual nutrient data was not always available or easy to query.
 * Created by Sam on 05/02/2017.
 */
public class Nutrient {
    // Matches: Data > GET Food Information
    // Usage: Would represent detailed information on an ingredient. May not be worth using but is
    // all set up for future work?
    private final String title;
    private final float amount;
    private final String unit;
    private final float percentOfDailyNeeds;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Nutrient(float amount, float percentOfDailyNeeds, String title, String unit) {
        this.amount = amount;
        this.title = title;
        this.unit = unit;
        this.percentOfDailyNeeds = percentOfDailyNeeds;
    }

    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Nutrient(String json) {
        Gson gson = new Gson();
        Nutrient nutrient = gson.fromJson(json, Nutrient.class);
        this.title = nutrient.getTitle();
        this.amount = nutrient.getAmount();
        this.unit = nutrient.getUnit();
        this.percentOfDailyNeeds = nutrient.getpercentOfDailyNeeds();
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

    public float getpercentOfDailyNeeds() {
        return percentOfDailyNeeds;
    }

    public String getTitle() {
        return title;
    }

    public String getUnit() {
        return unit;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    @Override
    public String toString() {
        return "Nutrient{" +
                "amount=" + amount +
                ", title='" + title + '\'' +
                ", unit='" + unit + '\'' +
                ", percentOfDailyNeeds=" + percentOfDailyNeeds +
                '}';
    }
}
