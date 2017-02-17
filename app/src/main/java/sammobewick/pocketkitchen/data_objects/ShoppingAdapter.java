package sammobewick.pocketkitchen.data_objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;

/**
 * Created by Sam on 31/01/2017.
 */

public class ShoppingAdapter extends BaseAdapter {

    private List<ListItem> data;

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        TextView shoppingTitle;
        TextView shoppingQuantity;
        TextView shoppingMeasurement;
    }

    public ShoppingAdapter(List<ListItem> data) {
        this.data = data;
    }

    public ShoppingAdapter() { /* empty */ }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0; // TODO: Remove this value when done with testing.
        }
    }

    @Override
    public ListItem getItem(int position) {
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
            v = vi.inflate(R.layout.shopping_item, null);

            // Establish our holder information:
            vh.shoppingMeasurement  = (TextView) v.findViewById(R.id.shopping_measurement);
            vh.shoppingQuantity     = (TextView) v.findViewById(R.id.shopping_quantity);
            vh.shoppingTitle        = (TextView) v.findViewById(R.id.shopping_quantity);

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

        return v;
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    public List<ListItem> getData() {
        return data;
    }

    public void setData(List<ListItem> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
