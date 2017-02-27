package sammobewick.pocketkitchen.data_objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.DownloadImageAsync;

/**
 * This is the custom ListAdapter for displaying Recipe_Short objects.
 * This is used both by the RecipeFragment and the RecipeSavedActivity, so the setting of data is
 * called from those places.
 * Created by Sam on 31/01/2017.
 */
public class RecipeShortAdapter extends BaseAdapter {

    private List<Recipe_Short> data;
    private String             urlStart;

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        ImageView   recipeImg;
        ImageView   dietaryImg;
        TextView    recipeTitle;
        TextView    recipeDesc;
    }

    public RecipeShortAdapter(String urlStart) {
        this.urlStart = urlStart;
        PocketKitchenData pkData = PocketKitchenData.getInstance();
        data = pkData.getRecipesDisplayed();
    }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0;
        }
    }

    @Override
    public Recipe_Short getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v          = convertView;
        ViewHolder vh   = new ViewHolder();

        // If null, then we need to get the view and inflate it:
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.recipe_item, null);

            // Establish our holder information:
            vh.dietaryImg   = (ImageView) v.findViewById(R.id.recipe_dietary_image);
            vh.recipeImg    = (ImageView) v.findViewById(R.id.recipe_image);
            vh.recipeDesc   = (TextView)  v.findViewById(R.id.recipe_descr);
            vh.recipeTitle  = (TextView)  v.findViewById(R.id.recipe_title);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        // Here we need to get the object representing the recipe:
        Recipe_Short recipe = getItem(position);

        vh.recipeTitle.setText(recipe.getTitle());

        String desc = "This tasty recipe can be made in " + recipe.getReadyInMinutes() + " minutes!";
        vh.recipeDesc.setText(desc);

        // Load the images using aSync task:
        String url  = urlStart + recipe.getImage();

        /* DEBUG:
        System.out.println("URL: " + url);
        // END-DEBUG */

        new DownloadImageAsync(vh.recipeImg).execute(url);

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    public List<Recipe_Short> getData() {
        return data;
    }

    public void setData(List<Recipe_Short> data) {
        this.data = data;
        this.notifyDataSetChanged();

        /* DEBUG
        for(Recipe_Short r : data) {
            System.out.println("DATA: " + r.getTitle());
        } // END-DEBUG */
    }
}
