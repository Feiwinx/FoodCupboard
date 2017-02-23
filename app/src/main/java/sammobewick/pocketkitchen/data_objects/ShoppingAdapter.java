package sammobewick.pocketkitchen.data_objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sammobewick.pocketkitchen.R;

/**
 * Created by Sam on 31/01/2017.
 */

public class ShoppingAdapter extends BaseAdapter implements Filterable {

    private List<ListItem> filtered;
    private List<ListItem> data;
    private String urlStart;

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        TextView shoppingTitle;
        TextView shoppingQuantity;
        TextView shoppingMeasurement;
        Button   shoppingBtnDelete;
        Button   shoppingBtnEdit;
    }

    public ShoppingAdapter(String urlStart) {
        this.urlStart = urlStart;

        PocketKitchenData pkData = PocketKitchenData.getInstance();

        data        = pkData.getListItems();
        filtered    = data;
    }

    private void getFullListOfIngredients() {
        // TODO: When possible to compare current stock, this needs to be changed around!

        // We will need to combine the custom items with the recipe ones!
        PocketKitchenData pkData = PocketKitchenData.getInstance();

        List<ListItem> masterList = new ArrayList<>();

        Map<Integer, List<Ingredient>> pkDataSet = pkData.getRecipe_ingredients();

        if (pkDataSet != null) {
            for (Map.Entry<Integer, List<Ingredient>> entry : pkDataSet.entrySet()) {
                int id = entry.getKey();
                List<Ingredient> ingredients = entry.getValue();

                for (Ingredient i : ingredients) {
                    // Convert each ingredient to ListItem:
                    ListItem item = new ListItem(
                            i.getAmount(),
                            i.getId(),
                            i.getName(),
                            i.getUnitShort()
                    );
                    masterList.add(item);
                    System.out.println("ADDING ITEM: " + item.toString());
                }
            }
        }

        // Try a parse all ingredients so to simplfy the list:
        List<ListItem> mergedList = new ArrayList<>();

        for(ListItem item : masterList) {
            // First, check if the item already exists (using overriden class methods):
            int index = mergedList.indexOf(item);

            // If != -1, meaning if found. We will want to merge them if so!
            if (index != -1) {
                System.out.println("MERGED-ITEM: " + mergedList.get(index).toString() + " with " + item.toString());
                mergedList.set(index, mergedList.get(index).mergeAdd(item));
            } else {
                // Not found, just add to the list:
                mergedList.add(item);
            }
        }

        // Now to include custom items:
        List<ListItem> pkDataList = pkData.getListItems();
        if (pkDataList != null ) {
            mergedList.addAll(pkDataList);
        }

        // Now pass to be displayed:
        this.data       = mergedList;
        this.filtered   = mergedList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ListItem> filteredData = getFilteredResults(constraint);

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredData;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filtered = (List<ListItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private List<ListItem> getFilteredResults(CharSequence constraint) {
        List<ListItem> resultList = new ArrayList<>(data);

        for (ListItem i: resultList) {
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
            return 0; // TODO: Remove this value when done with testing.
        }
    }

    @Override
    public ListItem getItem(int position) {
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
            v = vi.inflate(R.layout.shopping_item, null);

            // Establish our holder information:
            vh.shoppingMeasurement  = (TextView) v.findViewById(R.id.shopping_measurement);
            vh.shoppingQuantity     = (TextView) v.findViewById(R.id.shopping_quantity);
            vh.shoppingTitle        = (TextView) v.findViewById(R.id.shopping_title);

            vh.shoppingMeasurement.setClickable(false);
            vh.shoppingQuantity.setClickable(false);
            vh.shoppingTitle.setClickable(false);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        // Here we get the object representing the list item:
        ListItem item = getItem(position);

        // Then, we use the object data to populate / set our vh attributes.
        vh.shoppingQuantity.setText(String.valueOf(item.getAmount()));
        vh.shoppingMeasurement.setText(item.getUnit());
        vh.shoppingTitle.setText(item.getName());

        System.out.println("DISPLAYING ITEM: " + item.toString());

        return v;
    }

    @Override
    public void notifyDataSetChanged() {
        this.getFullListOfIngredients();
        super.notifyDataSetChanged();
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    public List<ListItem> getData() {
        return data;
    }

    public void setFilterText(String filterText) {
        if (filterText.length() > 0) {
            this.getFilter().filter(filterText);
        }
    }
}
