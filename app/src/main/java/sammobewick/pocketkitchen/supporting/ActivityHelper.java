package sammobewick.pocketkitchen.supporting;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;

import sammobewick.pocketkitchen.R;

/**
 * Created by Sam on 17/02/2017.
 */

public class ActivityHelper {

    private Context context;

    public ActivityHelper(Context context) {
        this.context = context;
    }

    public boolean isConnected() {
        // Confirm WiFi is connected:
        ConnectivityManager connMngr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo actNetwork = connMngr.getActiveNetworkInfo();

        return actNetwork != null && actNetwork.isConnectedOrConnecting();
    }

    public void displayNetworkWarning() {
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
                        // do nothing?
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void displayErrorDialog(String errorMessage) {
        new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog))
                .setTitle("Oops! Something went wrong:")
                .setMessage("Error: " + errorMessage + "\nYou may want to try again or report the error!")
                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: add any feature required here.
                    }
                })
                .show();
    }

    public void displaySnackBarNoAction(int layoutID, int stringResource) {
        View layout = ((Activity) context).findViewById(layoutID);
        Snackbar.make(layout, stringResource, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }
}
