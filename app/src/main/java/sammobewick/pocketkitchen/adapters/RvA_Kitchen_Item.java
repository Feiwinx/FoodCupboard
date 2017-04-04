package sammobewick.pocketkitchen.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.supporting.DataListener;

// TODO: Finish class. This is a more optimal way of having a list in the activity. Will not put
// TODO: work in now, as it is arguably wasteful for the project.

/**
 * Created by Sam on 24/03/2017.
 */
public class RvA_Kitchen_Item extends RecyclerView.Adapter<RvA_Kitchen_Item.ViewHolder> implements
        DataListener {

    private List<Ingredient> data;
    private Context context;

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView   kitchenImage;
        private TextView    kitchenTitle;
        private TextView    kitchenQuantity;
        private TextView    kitchenMeasurement;

        public ViewHolder(View v) {
            super(v);

            // Establish our holder information:
            kitchenImage        = (ImageView)   v.findViewById(R.id.kitchen_image);
            kitchenMeasurement  = (TextView)    v.findViewById(R.id.kitchen_measurement);
            kitchenQuantity     = (TextView)    v.findViewById(R.id.kitchen_quantity);
            kitchenTitle        = (TextView)    v.findViewById(R.id.kitchen_title);
        }
    }

    public RvA_Kitchen_Item(Context context) {
        this.context = context;
    }

    @Override
    public void dataUpdate() {
    }

    @Override
    public RvA_Kitchen_Item.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RvA_Kitchen_Item.ViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
