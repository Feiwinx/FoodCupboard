package sammobewick.pocketkitchen.data_objects;

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
import sammobewick.pocketkitchen.supporting.DataListener;

/**
 * Created by Sam on 31/01/2017.
 */

public class KitchenAdapter extends BaseAdapter  implements Filterable, DataListener {

    private List<Ingredient> filtered;
    private List<Ingredient> data;
    private String urlStart;

    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        data        = pkData.getInCupboards();
        filtered    = data;
        this.notifyDataSetChanged();
    }

    private class ViewHolder{
        ImageView   kitchenImage;
        TextView    kitchenTitle;
        TextView    kitchenQuantity;
        TextView    kitchenMeasurement;
    }

    public KitchenAdapter(String urlStart) {
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
                filtered = (List<Ingredient>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<Ingredient> getFilteredResults(CharSequence constraint) {
        List<Ingredient> resultList = new ArrayList<>(data);

        for (Ingredient i: resultList) {
            if (!i.getName().contains(constraint)) {
                resultList.remove(i);
            }
        }
        return resultList;
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

        View v          = convertView;
        ViewHolder vh   = new ViewHolder();

        // If null, then we need to get the view and inflate it:
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.kitchen_item, null);

            // Establish our holder information:
            vh.kitchenImage         = (ImageView) v.findViewById(R.id.kitchen_image);
            vh.kitchenMeasurement   = (TextView)  v.findViewById(R.id.kitchen_measurement);
            vh.kitchenQuantity      = (TextView)  v.findViewById(R.id.kitchen_quantity);
            vh.kitchenTitle         = (TextView)  v.findViewById(R.id.kitchen_title);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        // Here we get the object representing the ingredient!
        Ingredient ingredient = getItem(position);

        // Then, we use the object data to populate / set our vh attributes.
        String url = ingredient.getImage();

        System.out.println("URL: " + url);

        new DownloadImageAsync(vh.kitchenImage).execute(url);

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
        if (filterText.length() > 0) {
            this.getFilter().filter(filterText);
        }
    }
}
