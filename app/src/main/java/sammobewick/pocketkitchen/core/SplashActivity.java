package sammobewick.pocketkitchen.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Simple splash activity which using a drawable resource to show a introductory screen.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, LoginActivity.class);

        // Launch the login activity + exit this one:
        startActivity(intent);
        finish();
    }
}
