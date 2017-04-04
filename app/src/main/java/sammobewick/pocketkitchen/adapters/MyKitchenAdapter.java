package sammobewick.pocketkitchen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.DownloadImageAsync;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.supporting.DataListener;

/**
 * Created by Sam on 31/01/2017.
 */

public class MyKitchenAdapter extends BaseAdapter implements Filterable, DataListener {

    private List<Ingredient> filtered;
    private List<Ingredient> data;
    private String urlStart;

    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        data = pkData.getInCupboards();
        filtered = data;
        this.notifyDataSetChanged();
    }

    private class ViewHolder {
        ImageView kitchenImage;
        TextView kitchenTitle;
        TextView kitchenQuantity;
        TextView kitchenMeasurement;
    }

    public MyKitchenAdapter(String urlStart) {
        this.urlStart = urlStart;
        dataUpdate();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Ingredient> filteredData = getFilteredResults(constraint);

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredData;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                //noinspection unchecked // we are controlling this through the rest of the class.
                filtered = (List<Ingredient>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<Ingredient> getFilteredResults(CharSequence constraint) {
        if (data != null) {
            List<Ingredient> resultList = new ArrayList<>(data);

            for (Ingredient i : new ArrayList<>(data)) {
                if (!i.getName().contains(constraint)) {
                    resultList.remove(i);
                }
            }
            return resultList;
        } else {
            return null;
        }
    }

    @Override
    public int getCount() {
        if (filtered != null) {
            return filtered.size();
        } else {
            return 0;
        }
    }

    @Override
    public Ingredient getItem(int position) {
        return filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return filtered.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder vh = new ViewHolder();

        // If null, then we need to get the view and inflate it:
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.ingredient_item_kitchen, parent, false);

            // Establish our holder information:
            vh.kitchenImage = (ImageView) v.findViewById(R.id.kitchen_image);
            vh.kitchenMeasurement = (TextView) v.findViewById(R.id.kitchen_measurement);
            vh.kitchenQuantity = (TextView) v.findViewById(R.id.kitchen_quantity);
            vh.kitchenTitle = (TextView) v.findViewById(R.id.kitchen_title);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        // Here we get the object representing the ingredient!
        Ingredient ingredient = getItem(position);

        // Then, we use the object data to populate / set our vh attributes, starting with URL:
        String url = ingredient.getImage();

        if (url.length() > 0) {
            new DownloadImageAsync(vh.kitchenImage).execute(url);
        } else {
            vh.kitchenImage.setContentDescription(parent.getResources().getString(R.string.hint_img_for_custom));
        }

        vh.kitchenMeasurement.setText(ingredient.getUnitShort());
        vh.kitchenQuantity.setText(String.valueOf(ingredient.getAmount()));
        vh.kitchenTitle.setText(ingredient.getName());

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    public void setFilterText(String filterText) {
        this.getFilter().filter(filterText);
    }
}