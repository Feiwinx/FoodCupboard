package sammobewick.pocketkitchen.supporting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.LoadDriveActivity;
import sammobewick.pocketkitchen.communication.SaveDriveActivity;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;

/**
 * Helper class filled with static methods. Mostly produce multi-purpose dialogs, but also contains
 * a method to check network connectivity. This is essential to informing the user about errors or
 * potential problems.
 *
 * Also contains dialog methods for adding items to the PocketKitchenData, but these sit hit more
 * for tidiness than any other reason!
 *
 * Created by Sam on 25/02/2017.
 */
public class ActivityHelper {
    private static final String TAG = "ActivityHelper";

    public static void uploadToDrive(Context context) {
        Intent driveTest = new Intent(context, SaveDriveActivity.class);
        driveTest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(driveTest);
    }

    public static void downloadFromDrive(Context context) {
        Log.d(TAG, "Download Called");
        Intent driveTest = new Intent(context, LoadDriveActivity.class);
        driveTest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(driveTest);
    }

    public static void deleteFromDrive(Context context) {
        // TODO: this.
    }

    /**
     * Helper method to establish if the device has a data connection (of any sort).
     * @param context Context - required to check network connectivity.
     * @return boolean - is the device connected?
     */
    public static boolean isConnected(final Context context) {
        // Confirm WiFi is connected:
        ConnectivityManager connMngr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo actNetwork = connMngr.getActiveNetworkInfo();

        return actNetwork != null && actNetwork.isConnectedOrConnecting();
    }

    /**
     * Placeholder method so that if this feature is implemented, all relevant areas already call
     * this method and pass the data along.
     * @param error String - being the error message.
     * @param extraInfo String - being additional information recorded at the time of the error.
     */
    public static void logErrorToServer(String error, String extraInfo) {
        // TODO: Extra feature to log to AWS somewhere.
    }

    /**
     * Helper method to display a network warning. Used in conjuntion with the isConnected method.
     * @param context Context - required to display the dialog.
     */
    public static void displayNetworkWarning(final Context context) {
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog))
                .setTitle("No Network Connection")
                .setMessage(R.string.wifi_warning)
                .setPositiveButton("Take Me There", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                })
                .setNegativeButton("Got It", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing, dismiss is automatically called.
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Helper method to display a basic dialog with dietary/symbol information.
     * @param context Context - required to display the dialog.
     */
    public static void displayDietaryInfo(final Context context) {
        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.dialog_dietary_key);
        dialog.setTitle(R.string.title_dietary_view_key);
        dialog.setCanceledOnTouchOutside(true);
        dialog.findViewById(R.id.gv_dietary_key).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Helper method to display the 'About' information box.
     * @param context Context - required to display the dialog.
     */
    public static void displayAboutInfo(final Context context) {
        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle(R.string.pref_about_title);
        dialog.setCanceledOnTouchOutside(true);
        dialog.findViewById(R.id.ll_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * Helper method to display a unexpected error.
     * @param context Context - required to display the dialog.
     * @param errorMessage String - error message to display [multi-purpose]
     */
    public static void displayUnknownError(final Context context, final String errorMessage) {
        logErrorToServer(errorMessage, context.getPackageCodePath());
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog))
                .setTitle("Oops! Something went wrong:")
                .setMessage("Error: " + errorMessage + "\nYou may want to try again or report the error!")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "Unknown Error: " + errorMessage);
                    }
                })
                .show();
    }

    /**
     * Helper method to display a known error (i.e. something that makes sense or can be expected).
     * @param context Context - required to display the dialog.
     * @param errorMessage String - the base error message to display.
     */
    public static void displayKnownError(final Context context, final String errorMessage) {
        logErrorToServer(errorMessage, context.getPackageCodePath());
        new AlertDialog.Builder(new android.view.ContextThemeWrapper(context, R.style.myDialog))
                .setTitle("Oops! There's a problem:")
                .setMessage(errorMessage)
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG, "Known Error: " + errorMessage);
                    }
                })
                .show();
    }

    /**
     * Helper method to display some information in the snackbar.
     * @param context Context - required to display the snackbar.
     * @param layoutID int - required resource ID to display the snackbar.
     * @param stringResource int - required string resource to display the message.
     */
    public static void displaySnackBarNoAction(final Context context, int layoutID, int stringResource) {
        View layout = ((Activity) context).findViewById(layoutID);
        Snackbar.make(layout, stringResource, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    /**
     * Helper method to convert HTML. This should be used for recipe instructions as they can
     * sometimes include HTML (depending on the source). However, in testing, I can no longer find
     * and example of this!
     * @param html String - being the HTML string.
     * @return Spanned - being the Spanned representation of the original String [HTML-accounted for].
     */
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else result = Html.fromHtml(html);
        return result;
    }

                            /*  LENGTHILY DIALOGS BELOW HERE */

    /**
     * Dialog method to add a shopping item to PocketKitchenData.
     * Everything is handled from here, sending the data on once a button is pressed.
     * @param context Context - required to display the dialog.
     * @param item Ingredient - can be NULL if no existing details are to be displayed.
     */
    public static void dialogShoppingItem(final Context context, final Ingredient item) {
        final PocketKitchenData pkData = PocketKitchenData.getInstance();
        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.dialog_edit_ingredient_list);

        // Get the views + set the text up:
        final EditText edit_qty_name = (EditText) dialog.findViewById(R.id.edit_item_qty_name);
        final EditText edit_qty = (EditText) dialog.findViewById(R.id.measurements_editText);
        final EditText edit_name = (EditText) dialog.findViewById(R.id.edit_shopping_item);

        final Button dialog_discard = (Button) dialog.findViewById(R.id.btn_discard_itm);
        final Button dialog_save = (Button) dialog.findViewById(R.id.btn_save_itm);
        final ImageButton dialog_remove = (ImageButton) dialog.findViewById(R.id.btn_remove_itm);

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
                        displaySnackBarNoAction(context, R.id.main_content, R.string.feedback_cancelled_change);
                        dialog.dismiss();
                    }
                }
        );

        dialog_save.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        float qty = Float.parseFloat(edit_qty.getText().toString().trim());
                        String qty_name = edit_qty_name.getText().toString().trim();
                        String name = edit_name.getText().toString().trim();

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
                        if (item != null && item.isCustom()) { // REMOVE CUSTOM INGREDIENT:
                            Ingredient customItem = new Ingredient(
                                    Float.valueOf(edit_qty.getText().toString()),
                                    edit_name.getText().toString(),
                                    edit_qty_name.getText().toString()
                            );
                            pkData.removeCustomIngredient(customItem);
                            dialog.dismiss();
                        } else {            // REMOVE STANDARD NON-CUSTOM INGREDIENT:
                            assert item != null;
                            item.setName(edit_name.getText().toString());
                            item.setAmount(Float.valueOf(edit_qty.getText().toString()));
                            item.setUnitShort(edit_qty_name.getText().toString());
                            pkData.removeIngredient(item);
                            dialog.dismiss();
                        }
                    }
                }
        );
        // SHOW THE DIALOG:
        dialog.show();
    }

    /**
     * Dialog method to add a kitchen item to PocketKitchenData.
     * Everything is handled from here, sending the data on once a button is pressed.
     * @param context Context - required to show the dialog.
     * @param ingredient Ingredient - can be NULL if no existing details are to be displayed.
     */
    public static void dialogKitchenItem(final Context context, final Ingredient ingredient) {
        final PocketKitchenData pkData = PocketKitchenData.getInstance();
        final Dialog dialog = new Dialog(context);

        dialog.setContentView(R.layout.dialog_edit_ingredient_kitchen);

        // Get the views + set the text up:
        final EditText edit_qty_name = (EditText) dialog.findViewById(R.id.edit_kitchen_qty_name);
        final EditText edit_qty = (EditText) dialog.findViewById(R.id.measurements_editText_kitchen);
        final EditText edit_name = (EditText) dialog.findViewById(R.id.edit_kitchen_item);

        final Button dialog_discard = (Button) dialog.findViewById(R.id.btn_discard_kitchen_itm);
        final Button dialog_save = (Button) dialog.findViewById(R.id.btn_save_kitchen_itm);
        final ImageButton dialog_remove = (ImageButton) dialog.findViewById(R.id.btn_remove_kitchen_itm);

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

            // Display warning if non-custom:
            if (ingredient.isCustom())
                dialog.findViewById(R.id.txt_item_kitchen_edit_warning).setVisibility(View.VISIBLE);

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

            edit_qty_name.setText(context.getString(R.string.lbl_item_for_dialog));
            edit_qty.setText(context.getString(R.string.lbl_1_for_dialog));

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
                displaySnackBarNoAction(context, R.id.main_content, R.string.feedback_cancelled_change);
                dialog.dismiss();
            }
        });

        dialog_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pkData.removeFromCupboard(ingredient);
                dialog.dismiss();
            }
        });

        dialog_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float qty = Float.parseFloat(edit_qty.getText().toString().trim());
                String qty_name = edit_qty_name.getText().toString().trim();
                String name = edit_name.getText().toString().trim();

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
        // SHOW THE DIALOG:
        dialog.show();
    }
}
