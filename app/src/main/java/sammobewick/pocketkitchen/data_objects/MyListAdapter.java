package sammobewick.pocketkitchen.data_objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.supporting.DataListener;

/**
 * Created by Sam on 31/01/2017.
 */
public class MyListAdapter extends BaseAdapter implements Filterable, DataListener {

    private List<Ingredient> filtered;
    private List<Ingredient> data;

    @Override
    public void dataUpdate() {
        PocketKitchenData pkData = PocketKitchenData.getInstance(this);
        pkData.updateToBuy();

        data = pkData.getToBuy();
        filtered = data;
        this.notifyDataSetChanged();
    }

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        TextView shoppingTitle;
        TextView shoppingQuantity;
        TextView shoppingMeasurement;
        ImageButton shoppingBtnDelete;
        ImageButton shoppingBtnBought;
    }

    public MyListAdapter() {
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
                //noinspection unchecked // we have controlled this using the rest of the class.
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
                    System.out.println("removing: " + i.getName());
                    resultList.remove(i);
                }
            }
            return resultList;
        } else { return null; }
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
    public Ingredient getItem(int position) {
        return filtered.get(position);
    }

    @Override
    public long getItemId(int position) {
        return filtered.get(position).getId();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder vh = new ViewHolder();

        // If null, then we need to get the view and inflate it:
        if (v == null) {
            LayoutInflater vi = LayoutInflater.from(parent.getContext());
            v = vi.inflate(R.layout.item_ingredient_list, parent, false);

            // Establish our holder information:
            vh.shoppingMeasurement = (TextView) v.findViewById(R.id.shopping_measurement);
            vh.shoppingQuantity = (TextView) v.findViewById(R.id.shopping_quantity);
            vh.shoppingTitle = (TextView) v.findViewById(R.id.shopping_title);
            vh.shoppingBtnDelete = (ImageButton) v.findViewById(R.id.shopping_del_btn);
            vh.shoppingBtnBought = (ImageButton) v.findViewById(R.id.shopping_buy_btn);

            vh.shoppingMeasurement.setClickable(false);
            vh.shoppingQuantity.setClickable(false);
            vh.shoppingTitle.setClickable(false);

            vh.shoppingBtnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ingredient selected = getItem(position);
                    PocketKitchenData pkData = PocketKitchenData.getInstance();
                    if (selected.isCustom()) {
                        pkData.removeCustomIngredient(selected);
                    } else {
                        pkData.removeIngredient(selected);
                    }
                }
            });
            vh.shoppingBtnDelete.setFocusable(false);

            vh.shoppingBtnBought.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Ingredient selected = getItem(position);
                    PocketKitchenData pkData = PocketKitchenData.getInstance();

                    pkData.addToCupboard(selected);

                    if (selected.isCustom()) {
                        pkData.removeCustomIngredient(selected);
                    } else {
                        pkData.removeIngredient(selected);
                    }
                }
            });
            vh.shoppingBtnBought.setFocusable(false);

            // Save the holder as a tag on the view:
            v.setTag(vh);
        } else {
            // If not null, then we have a tag already!
            vh = (ViewHolder) v.getTag();
        }

        // Here we get the object representing the list item:
        Ingredient item = getItem(position);

        // Then, we use the object data to populate / set our vh attributes.
        vh.shoppingQuantity.setText(String.valueOf(item.getAmount()));
        vh.shoppingMeasurement.setText(item.getUnitShort());
        vh.shoppingTitle.setText(item.getName());

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
