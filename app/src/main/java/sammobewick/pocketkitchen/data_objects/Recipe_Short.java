package sammobewick.pocketkitchen.data_objects;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Sam on 09/02/2017.
 */
public class Recipe_Short implements Serializable {
    // Matches: Search > GET Search Recipes
    // Usage: represents the brief amount of information gotten from text searches. This is what
    // can be displayed in the listview.
    private int id;
    private String title;
    private int readyInMinutes;
    private String image;
    private List<String> imageUrls;

    // ****************************************************************************************** //
    //                                      CONSTRUCTORS:                                         //
    // ****************************************************************************************** //

    public Recipe_Short(int id, String image, List<String> imageUrls, int readyInMinutes, String title) {
        this.id = id;
        this.image = image;
        this.imageUrls = imageUrls;
        this.readyInMinutes = readyInMinutes;
        this.title = title;
    }

    // ****************************************************************************************** //
    //                                      JSON CONVERSIONS:                                     //
    // ****************************************************************************************** //

    public Recipe_Short(String json) {
        Gson gson = new Gson();
        Recipe_Short recipe_short = gson.fromJson(json, Recipe_Short.class);
        this.id = recipe_short.getId();
        this.image = recipe_short.getImage();
        this.imageUrls = recipe_short.getImageUrls();
        this.readyInMinutes = recipe_short.getReadyInMinutes();
        this.title = recipe_short.getTitle();
    }

    // ****************************************************************************************** //
    //                                      GETTERS:                                              //
    // ****************************************************************************************** //

    public int getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public String getTitle() {
        return title;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    // TODO: Not worth having the full toString so create your own essential information version!
}
