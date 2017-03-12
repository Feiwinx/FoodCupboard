package sammobewick.pocketkitchen.data_objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.DownloadImageAsync;
import sammobewick.pocketkitchen.supporting.DataListener;

/**
 * Created by Sam on 12/03/2017.
 */
public class SavedRecipesAdapter extends BaseAdapter implements DataListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private List<Recipe_Short>  data;
    private String              urlStart;

    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        data = pkData.getRecipesToCook();
        this.notifyDataSetChanged();
    }

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        ImageView   recipeImg;
        TextView    recipeTitle;
        ImageButton removeBtn;
    }

    public SavedRecipesAdapter(String urlStart) {
        this.urlStart = urlStart;
        this.dataUpdate();
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

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.item_recipe_saved, parent, false);

            // Establish our holder information:
            vh.recipeImg    = (ImageView)   v.findViewById(R.id.recipe_saved_img);
            vh.recipeTitle  = (TextView)    v.findViewById(R.id.recipe_saved_title);
            vh.removeBtn    = (ImageButton) v.findViewById(R.id.recipe_saved_del_btn);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        final Recipe_Short recipe = getItem(position);

        vh.recipeTitle.setText(recipe.getTitle());

        // Load the images using aSync task:
        String url = urlStart + recipe.getImage();

        /* DEBUG:
        System.out.println("URL: " + url);
        // END-DEBUG */

        new DownloadImageAsync(vh.recipeImg).execute(url);

        // Listener for the button:
        vh.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PocketKitchenData pkData = PocketKitchenData.getInstance();
                pkData.removeRecipeFromCookList(recipe);
            }
        });

        return v;
    }
}
