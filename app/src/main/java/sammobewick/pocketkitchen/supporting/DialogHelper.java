package sammobewick.pocketkitchen.supporting;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;

/**
 * Created by Sam on 25/02/2017.
 */
public class DialogHelper {

    public static void dialogShoppingItem(Context context, final Ingredient item) {
        final PocketKitchenData pkData  = PocketKitchenData.getInstance();
        final ActivityHelper helper     = new ActivityHelper(context);
        final Dialog dialog             = new Dialog(context);

        dialog.setContentView(R.layout.shopping_item_edit);

        // Get the views + set the text up:
        final EditText edit_qty_name    = (EditText) dialog.findViewById(R.id.edit_item_qty_name);
        final EditText edit_qty         = (EditText) dialog.findViewById(R.id.measurements_editText);
        final EditText edit_name        = (EditText) dialog.findViewById(R.id.edit_shopping_item);

        final Button dialog_discard     = (Button)   dialog.findViewById(R.id.btn_discard_itm);
        final Button dialog_save        = (Button)   dialog.findViewById(R.id.btn_save_itm);
        final ImageButton dialog_remove = (ImageButton)   dialog.findViewById(R.id.btn_remove_itm);

        if (item != null) {
            /* EDITING ITEM:
             * Set title.
             * Set the text/content of the dialog.
             * Disable editing if it's NOT CUSTOM.
             * Show the remove button as the item exists.
             * Show warnings as appropriate.
             */
            dialog.setTitle(R.string.lbl_dialog_edit_item);

            edit_name.setText(item.getName());
            edit_qty.setText(String.valueOf(item.getAmount()));
            edit_qty_name.setText(item.getUnitShort());

            // Disable some editing if not a custom.
            edit_name.setEnabled(item.isCustom());
            edit_qty.setEnabled(item.isCustom());
            edit_qty_name.setEnabled(item.isCustom());

            // Show remove button:
            dialog_remove.setVisibility(View.VISIBLE);

            if (item.isCustom()) {
                // Display warning typical for custom items:
                dialog.findViewById(R.id.txt_add_custom_kitchen_warning).setVisibility(View.VISIBLE);
            } else {
                dialog.findViewById(R.id.txt_edit_kitchen_warning).setVisibility(View.VISIBLE);

            }
        } else {
            /* ADDING ITEM:
             * Set title.
             * Enable editing. It will be custom regardless.
             * Hide the remove button as no item exists.
             * Show warnings as appropriate.
             */
            dialog.setTitle(R.string.lbl_dialog_new_item);

            edit_qty_name.setEnabled(true);
            edit_name.setEnabled(true);
            dialog.findViewById(R.id.txt_add_custom_kitchen_warning).setVisibility(View.VISIBLE);
            dialog_remove.setVisibility(View.GONE);
        }

        // Setting up the OnClickListeners:
        dialog_discard.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Provide feedback to the user + close dialog:
                        helper.displaySnackBarNoAction(R.id.main_content, R.string.feedback_cancelled_change);
                        dialog.dismiss();
                    }
                }
        );

        dialog_save.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        float qty       = Float.parseFloat(edit_qty.getText().toString().trim());
                        String qty_name = edit_qty_name.getText().toString().trim();
                        String name     = edit_name.getText().toString().trim();

                        if (qty > 0 & qty_name.length() > 0 & name.length() > 0) {
                            Ingredient customItem = new Ingredient(qty, name, qty_name);
                            if (item == null) {                         // ADD CUSTOM INGREDIENT:
                                pkData.addCustomIngredient(customItem);
                                dialog.dismiss();
                            } else if (item.isCustom()) {               // UPDATE CUSTOM INGREDIENT:
                                pkData.updateCustomIngredient(item, customItem);
                                dialog.dismiss();
                            }
                        } else { // Display warning that all fields are required:
                            dialog.findViewById(R.id.txt_item_requirements).setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        dialog_remove.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.isCustom()) { // REMOVE CUSTOM INGREDIENT:
                            Ingredient customItem = new Ingredient(
                                    Float.valueOf(edit_qty.getText().toString()),
                                    edit_name.getText().toString(),
                                    edit_qty_name.getText().toString()
                            );
                            pkData.removeCustomIngredient(customItem);
                            dialog.dismiss();
                        } else {            // REMOVE STANDARD NON-CUSTOM INGREDIENT:
                            item.setName(edit_name.getText().toString());
                            item.setAmount(Float.valueOf(edit_qty.getText().toString()));
                            item.setUnitShort(edit_qty_name.getText().toString());
                            // TODO: does unit long needs to be changed at all?
                            pkData.removeIngredient(item);
                            dialog.dismiss();
                        }
                    }
                }
        );
        // SHOW THE DIALOG:
        dialog.show();
    }

    public static void dialogKitchenItem(Context context, final Ingredient ingredient) {
        final PocketKitchenData pkData  = PocketKitchenData.getInstance();
        final ActivityHelper helper     = new ActivityHelper(context);
        final Dialog dialog             = new Dialog(context);

        dialog.setContentView(R.layout.kitchen_item_edit);

        // Get the views + set the text up:
        final EditText edit_qty_name    = (EditText) dialog.findViewById(R.id.edit_kitchen_qty_name);
        final EditText edit_qty         = (EditText) dialog.findViewById(R.id.measurements_editText_kitchen);
        final EditText edit_name        = (EditText) dialog.findViewById(R.id.edit_kitchen_item);

        final Button dialog_discard     = (Button)   dialog.findViewById(R.id.btn_discard_kitchen_itm);
        final Button dialog_save        = (Button)   dialog.findViewById(R.id.btn_save_kitchen_itm);
        final ImageButton dialog_remove      = (ImageButton)   dialog.findViewById(R.id.btn_remove_kitchen_itm);

        if (ingredient != null) {
            /* EDITING ITEM:
             * If custom, change all, and can use all buttons.
             * If not custom, can only change the quantity, but can use all buttons.
             */
            dialog.setTitle(R.string.lbl_dialog_edit_kitchen);

            // Enable buttons if custom (except qty):
            edit_qty_name.setEnabled(ingredient.isCustom());
            edit_name.setEnabled(ingredient.isCustom());
            edit_qty.setEnabled(true);

            // Insert existing text:
            edit_qty_name.setText(ingredient.getUnitShort());
            edit_name.setText(ingredient.getName());
            edit_qty.setText(String.valueOf(ingredient.getAmount()));

            // Show all buttons:
            dialog_discard.setVisibility(View.VISIBLE);
            dialog_remove.setVisibility(View.VISIBLE);
            dialog_save.setVisibility(View.VISIBLE);
        } else {
            /* ADDING ITEM:
             * Will be custom.
             * Can change all fields.
             * Can only cancel or save.
             */
            dialog.setTitle(R.string.lbl_dialog_new_kitchen);

            // Enable all fields:
            edit_qty_name.setEnabled(true);
            edit_name.setEnabled(true);
            edit_qty.setEnabled(true);

            // Show the applicable buttons:
            dialog_discard.setVisibility(View.VISIBLE);
            dialog_remove.setVisibility(View.GONE);
            dialog_save.setVisibility(View.VISIBLE);
        }

        // Setting OnClickListeners:
        dialog_discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Provide feedback to the user + close dialog:
                helper.displaySnackBarNoAction(R.id.main_content, R.string.feedback_cancelled_change);
                dialog.dismiss();
            }
        });

        dialog_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pkData.removeFromCupboard(ingredient);
            }
        });

        dialog_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float qty       = Float.parseFloat(edit_qty.getText().toString().trim());
                String qty_name = edit_qty_name.getText().toString().trim();
                String name     = edit_name.getText().toString().trim();

                if (qty > 0 & qty_name.length() > 0 & name.length() > 0) {
                    Ingredient newIng = new Ingredient(qty, name, qty_name);

                    if (ingredient == null) {
                        pkData.addToCupboard(newIng);
                        dialog.dismiss();
                    } else {
                        pkData.updateInCupbaord(ingredient, newIng);
                        dialog.dismiss();
                    }
                } else { // Display warning that all fields are required:
                    dialog.findViewById(R.id.txt_item_requirements).setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
