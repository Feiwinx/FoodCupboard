package sammobewick.pocketkitchen.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.aws_intents.DownloadAWSImageAsync;
import sammobewick.pocketkitchen.aws_intents.Dynamo_Delete_Json;
import sammobewick.pocketkitchen.aws_intents.S3_Delete_Image;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.DataListener;

/**
 * Adapter for displaying custom recipes in the MySavedRecipesActivity. This allows support for the
 * edit and deletion buttons and slightly different usage when downloading images.
 * Created by Sam on 04/04/2017.
 */
public class CustomRecipeAdapter extends BaseAdapter implements DataListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private AdapterParent       adapterParent;
    private List<Recipe_Short>  data;

    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        data = pkData.getMyCustomRecipes();
        this.notifyDataSetChanged();
    }

    public CustomRecipeAdapter(Context context) {

        if (context instanceof AdapterParent) {
            adapterParent = (AdapterParent) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement AdapterParent!");
        }

        // Fetch data:
        dataUpdate();
    }

    private class ViewHolder {
        ImageView   recipeImg;
        TextView    recipeTitle;
        ImageButton removeBtn;
        ImageButton editBtn;
    }

    @Override
    public int getCount() {
        if (data == null)
            return 0;

        return data.size();
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder vh = new ViewHolder();

        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.item_recipe_saved, parent, false);

            // Establish our holder information:
            vh.recipeImg    = (ImageView)   v.findViewById(R.id.recipe_saved_img);
            vh.recipeTitle  = (TextView)    v.findViewById(R.id.recipe_saved_title);
            vh.removeBtn    = (ImageButton) v.findViewById(R.id.recipe_saved_del_btn);
            vh.editBtn      = (ImageButton) v.findViewById(R.id.recipe_saved_edit_btn);

            // Show the extra button (reusable view):
            v.findViewById(R.id.recipe_saved_edit_btn).setVisibility(View.VISIBLE);

            // This listener is only needed to be called on creation:
            vh.editBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterParent.OnEditButtonPressed(position);
                }
            });

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            vh = (ViewHolder) v.getTag();
        }

        // Get ViewHolder details:
        final Recipe_Short recipe = getItem(position);

        vh.recipeTitle.setText(recipe.getTitle());

        String url = String.valueOf(recipe.getId());
        new DownloadAWSImageAsync((Context)adapterParent, vh.recipeImg).execute(url);

        // Removal listener requires recipe information:
        vh.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PocketKitchenData pkData = PocketKitchenData.getInstance();
                pkData.removeFromMyRecipes(recipe);

                Context context = ((Context)adapterParent);
                Intent delJson = new Intent(context, Dynamo_Delete_Json.class);
                context.startService(delJson);

                Intent delImg = new Intent(context, S3_Delete_Image.class);
                context.startService(delImg);
            }
        });

        // Return our view:
        return v;
    }

    public interface AdapterParent {
        void OnEditButtonPressed(int position);
    }
}
