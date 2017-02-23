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

/**
 * Created by Sam on 31/01/2017.
 */

public class KitchenAdapter extends BaseAdapter  implements Filterable{

    private List<Ingredient_Search> filtered;
    private List<Ingredient_Search> data;
    private String urlStart;

    private class ViewHolder{
        ImageView   kitchenImage;
        TextView    kitchenTitle;
        TextView    kitchenQuantity;
        TextView    kitchenMeasurement;
    }

    public KitchenAdapter(String urlStart) {
        this.urlStart = urlStart;

        PocketKitchenData pkData = PocketKitchenData.getInstance();
        data        = pkData.getIngredients();
        filtered    = data;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Ingredient_Search> filteredData = getFilteredResults(constraint);

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredData;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (List<Ingredient_Search>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<Ingredient_Search> getFilteredResults(CharSequence constraint) {
        List<Ingredient_Search> resultList = new ArrayList<>(data);

        for (Ingredient_Search i: resultList) {
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
    public Ingredient_Search getItem(int position) {
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
        Ingredient_Search ingredient = getItem(position);

        // Then, we use the object data to populate / set our vh attributes.
        vh.kitchenImage.setImageBitmap(null); // TODO: depends on what the attribute is.
        vh.kitchenMeasurement.setText(ingredient.getUnitShort());
        vh.kitchenQuantity.setText(String.valueOf(ingredient.getAmount()));
        vh.kitchenTitle.setText(ingredient.getName());

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        PocketKitchenData pkData = PocketKitchenData.getInstance();
        data = pkData.getIngredients();
        super.notifyDataSetChanged();
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    public List<Ingredient_Search> getData() {
        return data;
    }

    public void setFilterText(String filterText) {
        if (filterText.length() > 0) {
            this.getFilter().filter(filterText);
        }
    }
}
