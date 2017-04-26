package sammobewick.pocketkitchen.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.DownloadImageAsync;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.DataListener;

/**
 * This is the custom ListAdapter for displaying Recipe_Short objects.
 * This is used both by the SearchRecipesFragment and the MySavedRecipesActivity, so the setting of data is
 * called from those places.
 * Created by Sam on 31/01/2017.
 */
public class SearchedRecipesAdapter extends BaseAdapter implements DataListener {
    private List<Recipe_Short> data;
    private String urlStart;
    private boolean suggestion;

    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        data = pkData.getRecipesDisplayed();
        this.notifyDataSetChanged();
    }

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        ImageView   recipeImg;
        TextView    recipeTitle;
        TextView    recipeDesc;
    }

    public SearchedRecipesAdapter(String urlStart) {
        this.urlStart = urlStart;
        dataUpdate();
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

        View v = convertView;
        ViewHolder vh = new ViewHolder();

        // If null, then we need to get the view and inflate it:
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.item_recipe_short, parent, false);

            // Establish our holder information:
            vh.recipeImg    = (ImageView)   v.findViewById(R.id.recipe_search_img);
            vh.recipeDesc   = (TextView)    v.findViewById(R.id.recipe_search_desc);
            vh.recipeTitle  = (TextView)    v.findViewById(R.id.recipe_search_title);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        // Here we need to get the object representing the recipe:
        Recipe_Short recipe = getItem(position);

        vh.recipeTitle.setText(recipe.getTitle());

        // Description Text:
        String desc;
        if (!suggestion)
            desc = "This can be made in " + recipe.getReadyInMinutes() + " minutes.";
        else
            desc = "This recipe is ranked " + position+1 + " in how likely you can cook it.";

        vh.recipeDesc.setText(desc);

        // Load the images using aSync task:
        PocketKitchenData pkData = PocketKitchenData.getInstance();

        String url = urlStart + recipe.getImage();

        ///* DEBUG:
        System.out.println("URL: " + url);
        // END-DEBUG */

        Bitmap img = pkData.getDrawable(url);

        if (img != null)
            vh.recipeImg.setImageBitmap(img);
        else
            new DownloadImageAsync(vh.recipeImg).execute(url);

        return v;
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
    }

    public void setSuggestion(boolean suggestion) {
        this.suggestion = suggestion;
    }
}
