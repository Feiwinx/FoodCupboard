package sammobewick.pocketkitchen.core;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import sammobewick.pocketkitchen.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, TabbedActivity.class);

        // TODO: Instead of main, direct this to the login activity:
        // Launch activity + exit this one:
        startActivity(intent);
        finish();

        /* EXAMPLE DIALOG:
            new AlertDialog.Builder(this.getBaseContext())
                    .setTitle("No Network Connection")
                    .setMessage(R.string.wifi_warning)
                    .setPositiveButton("Take Me There", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                            finish();
                        }
                    })
                    .setNegativeButton("Nevermind!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }
        */
    }
}
