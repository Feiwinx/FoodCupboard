package sammobewick.pocketkitchen.data_objects;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Created by Sam on 09/02/2017.
 */
@DynamoDBTable(tableName = "PK_Recipes_Short")
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

    public String getJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    // ****************************************************************************************** //
    //                                      COMPARISON OVERRIDES:                                 //
    // ****************************************************************************************** //

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        // This is only reached when we know it's the same class!
        Recipe_Short that = (Recipe_Short) obj;
        return Objects.equals(id, that.getId()) &&
                Objects.equals(image, that.getImage()) &&
                Objects.equals(imageUrls, that.getImageUrls()) &&
                Objects.equals(readyInMinutes, that.getReadyInMinutes()) &&
                Objects.equals(title, that.getTitle());    // END-RETURN
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, image, imageUrls, readyInMinutes, title);
    }

    // ****************************************************************************************** //
    //                                      GETTERS:                                              //
    // ****************************************************************************************** //

    @DynamoDBHashKey(attributeName = "ID")
    public int getId() {
        return id;
    }

    @DynamoDBAttribute(attributeName = "IMAGE")
    public String getImage() {
        return image;
    }

    @DynamoDBAttribute(attributeName = "IMAGEURLS")
    public List<String> getImageUrls() {
        return imageUrls;
    }

    @DynamoDBAttribute(attributeName = "READYINMINUTES")
    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    @DynamoDBIndexRangeKey(attributeName = "TITLE")
    public String getTitle() {
        return title;
    }

    // ****************************************************************************************** //
    //                                      toString():                                           //
    // ****************************************************************************************** //

    @Override
    public String toString() {
        return "Recipe_Short{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", readyInMinutes=" + readyInMinutes +
                ", image='" + image + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
