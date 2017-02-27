package sammobewick.pocketkitchen.core;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.RecipeShortAdapter;

public class RecipeSavedActivity extends AppCompatActivity {

    String urlStart;
    RecipeShortAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_saved);
        setupActionBar();

        // Get the URL start from MetaData:
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData.containsKey("recipe_image_url")) {
                urlStart = ai.metaData.getString("recipe_image_url");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Set up adapter:
        mAdapter = new RecipeShortAdapter(urlStart);
    }

    /**
     * This method simply sets up the action bar:
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
