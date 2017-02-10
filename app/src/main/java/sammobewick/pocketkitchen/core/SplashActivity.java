package sammobewick.pocketkitchen.core;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;

import sammobewick.pocketkitchen.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Confirm WiFi is connected:
        ConnectivityManager connMngr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo actNetwork = connMngr.getActiveNetworkInfo();

        boolean isConnected = actNetwork != null && actNetwork.isConnectedOrConnecting();

        if (isConnected) {
            // TODO: Direct to login activity:
            Intent intent = new Intent(this, TabbedActivity.class);

            //intent.putExtra("data","null"); // TODO: This is how to share data!

            // Launch activity + exit this one:
            startActivity(intent);
            finish();
        } else {
            // TODO: Prompt to turn on WiFi:
            // can use:
            // startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.snackbar_wifi_warning, Snackbar.LENGTH_SHORT).setAction("Action", null).show();
        }
    }
}
