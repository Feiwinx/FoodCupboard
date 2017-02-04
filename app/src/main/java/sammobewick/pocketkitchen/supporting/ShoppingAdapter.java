package sammobewick.pocketkitchen.supporting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import sammobewick.pocketkitchen.R;

/**
 * Created by Sam on 31/01/2017.
 */

// TODO: Have this class utilise an object / array details!

public class ShoppingAdapter extends BaseAdapter {

    // Here we use this inner class to hold our views for this item:
    private class ViewHolder {
        TextView shoppingTitle;
        TextView shoppingQuantity;
        TextView shoppingMeasurement;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
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

        // TODO; Here I think I will need to get the object representing the recipe!

        // TODO: Then, we use the object data to populate / set our vh attributes.
        // e.g. vh.txt_field.setText(object.getValue());

        return v;
    }
}
