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
 * Adapter for items in the kitchen. This differs from the Shopping List as it doesn't support any
 * buttons.
 * Created by Sam on 31/01/2017.
 */
public class MyKitchenAdapter extends BaseAdapter implements Filterable, DataListener {
    private List<Ingredient> filtered;
    private List<Ingredient> data;
    private String urlStart;

    /**
     * Call required by interface. Allows PocketKitchenData to update this adapter when data changes.
     */
    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        data = pkData.getInCupboards();
        filtered = data;
        this.notifyDataSetChanged();
    }

    /**
     * Contains our view elements for the row layout.
     */
    private class ViewHolder {
        ImageView kitchenImage;
        TextView kitchenTitle;
        TextView kitchenQuantity;
        TextView kitchenMeasurement;
    }

    /**
     * Constructor. Needs to fetch the URLStart so that it can retrieve images.
     * @param urlStart String - being the URLStart.
     */
    public MyKitchenAdapter(String urlStart) {
        this.urlStart = urlStart;
        dataUpdate();
    }

    /**
     * Gets the Filter for enabling the SearchView to filter items in the Adapter.
     * @return Filter - being the Filter details.
     */
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

    /**
     * Get the filtered results from our dataset.
     * @param constraint CharSequence - being the constraining phrase imposed on the data.
     * @return List<Ingredient> - the resulting data that matches the constraint.
     */
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

    /**
     * Gets the count of the data we have (references filtered set).
     * @return int - being the count.
     */
    @Override
    public int getCount() {
        if (filtered != null) {
            return filtered.size();
        } else {
            return 0;
        }
    }

    /**
     * Gets the Ingredient at position in the filtered data.
     * @param position int - being the position in the list.
     * @return Ingredient - being the item at position.
     */
    @Override
    public Ingredient getItem(int position) {
        return filtered.get(position);
    }

    /**
     * Get the ID of the Ingredient at position in the filtered data.
     * @param position int - being the position in the list.
     * @return long - being the ID of the item.
     */
    @Override
    public long getItemId(int position) {
        return filtered.get(position).getId();
    }

    /**
     * Gets the view (i.e. the row) in the list.
     * @param position int - position.
     * @param convertView View - being the row's view.
     * @param parent ViewGroup - the parent of the row.
     * @return View - the resulting row view.
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

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

    /**
     * Notifies our Adapter that data has changed.
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    /**
     * Sets the filter text (allows for this to be set by the parent fragment).
     * @param filterText String - being the new filter text.
     */
    public void setFilterText(String filterText) {
        this.getFilter().filter(filterText);
    }
}
