package sammobewick.pocketkitchen.data_objects;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import sammobewick.pocketkitchen.R;

/**
 * This is a RecyclerView adapter for displaying ingredients in the Add Custom Recipe area of the
 * application. It basically handles blank rows, and will validate them at request, to produce a
 * list of Ingredients for the recipe.
 *
 * TODO: validation + return list of ingredients
 *
 * Created by Sam on 24/03/2017.
 */
public class RvA_Custom_Ingredients extends RecyclerView.Adapter<RvA_Custom_Ingredients.ViewHolder>{
    // Context + Data:
    private Context             context;
    private List<Ingredient>    data;

    /**
     * Public ViewHolder class.
     * Not used anywhere but in RvA_Custom_Ingredients, but required to be public for the
     * class definition.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private EditText    edit_name;
        private EditText    edit_qty;
        private EditText    edit_qty_name;
        private ImageButton img_btn_del;

        /**
         * Constructor. Requires our inflated view to fetch layouts from.
         * @param v - View to fetch layouts from.
         */
        public ViewHolder(View v) {
            super(v);

            edit_name = (EditText)      v.findViewById(R.id.edit_item_ingredient_name);
            edit_qty = (EditText)       v.findViewById(R.id.edit_item_ingredient_qty);
            edit_qty_name = (EditText)  v.findViewById(R.id.edit_item_ingredient_qty_name);
            img_btn_del = (ImageButton) v.findViewById(R.id.btn_item_ingredient_del);
        }
    }

    /**
     * Constructor. Requires context to inflate layouts.
     * @param context - Context to inflate with.
     */
    public RvA_Custom_Ingredients(Context context) {
        this.data       = new ArrayList<>();
        this.context    = context;
        addBlankItem();
    }

    /**
     * Adds a blank Ingredient to our RecyclerView:
     */
    public void addBlankItem() {
        if (data != null) {
            data.add(new Ingredient((float) 0, "", ""));
            notifyItemInserted(data.size() - 1);
            System.out.println("RV-SIZE:" + data.size());
        }
    }

    /**
     * Removes item at given position (unless it is the last one). This is because
     * we require at least 1 ingredient in a recipe!
     * @param position - int representing the position in the data.
     * @return boolean - did the data get removed?
     */
    public boolean removeItemAt(int position) {
        if (data != null) {
            if (data.size() > position & data.size() != 1) {
                data.remove(position);
                notifyItemRemoved(position);
                return true;
            }
        }
        return false;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View iv = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_custom, parent, false);

        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        Ingredient ing = data.get(position);

        holder.edit_name.setText(ing.getName());
        holder.edit_qty.setText(String.valueOf(ing.getAmount()));
        holder.edit_qty_name.setText(ing.getUnitShort());
        holder.img_btn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItemAt(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

}
