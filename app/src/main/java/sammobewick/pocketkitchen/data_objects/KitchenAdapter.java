package sammobewick.pocketkitchen.data_objects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;

/**
 * Created by Sam on 31/01/2017.
 */

public class KitchenAdapter extends BaseAdapter {

    private List<Ingredient> data;

    private class ViewHolder{
        ImageView   kitchenImage;
        TextView    kitchenTitle;
        TextView    kitchenQuantity;
        TextView    kitchenMeasurement;
    }

    public KitchenAdapter(List<Ingredient> data) {
        this.data = data;
    }

    public KitchenAdapter() { /* empty */ }

    @Override
    public int getCount() {
        if (data != null) {
            return data.size();
        } else {
            return 0; // TODO: Remove this value when testing is done.
        }
    }

    @Override
    public Ingredient getItem(int position) {
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
        vh.kitchenImage.setImageBitmap(null); // TODO: depends on what the attribute is.
        vh.kitchenMeasurement.setText(ingredient.getUnit());
        vh.kitchenQuantity.setText(String.valueOf(ingredient.getAmount()));
        vh.kitchenTitle.setText(ingredient.getName());

        return v;
    }

    // ****************************************************************************************** //
    //                                      SETTER + GETTER :                                     //
    // ****************************************************************************************** //

    public List<Ingredient> getData() {
        return data;
    }

    public void setData(List<Ingredient> data) {
        this.data = data;
        this.notifyDataSetChanged();
    }
}
