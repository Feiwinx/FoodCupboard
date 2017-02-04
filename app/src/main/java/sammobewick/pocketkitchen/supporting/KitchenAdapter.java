package sammobewick.pocketkitchen.supporting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import sammobewick.pocketkitchen.R;

/**
 * Created by Sam on 31/01/2017.
 */

// TODO: Have this class utilise an object / array details!

public class KitchenAdapter extends BaseAdapter {

    private class ViewHolder{
        ImageView   kitchenImage;
        TextView    kitchenTitle;
        TextView    kitchenQuantity;
        TextView    kitchenMeasurement;
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

        // TODO; Here I think I will need to get the object representing the recipe!

        // TODO: Then, we use the object data to populate / set our vh attributes.
        // e.g. vh.txt_field.setText(object.getValue());

        return v;
    }
}
