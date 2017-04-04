package sammobewick.pocketkitchen.activities_fragments;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.adapters.SavedRecipesAdapter_C;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.adapters.SavedRecipesAdapter;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.Constants;

public class MySavedRecipesActivity extends AppCompatActivity {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "MySavedRecipeActivity";

    private String              urlStart;
    private AbsListView         mListView;
    private BaseAdapter         mAdapter;
    private APIController       controller;
    private boolean             customOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_saved_recipes);
        setupActionBar();

        // Get whether custom or not from the Activity arguments:
        customOnly = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(Constants.INTENT_CUSTOM_ID)) {
                customOnly = extras.getBoolean(Constants.INTENT_CUSTOM_ID);
            }
        }

        // Get the URL start from MetaData:
        ApplicationInfo ai;
        try {
            ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if (ai.metaData.containsKey("recipe_image_url")) {
                urlStart = ai.metaData.getString("recipe_image_url");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SpoonacularAPIClient api_client = new SpoonacularAPIClient();
        controller = api_client.getClient();

        // Set up adapter (dependant on custom or not):
        if (customOnly)
            mAdapter = new SavedRecipesAdapter_C(urlStart);
        else
            mAdapter = new SavedRecipesAdapter(urlStart);

        // Rest is universal to either adapter:
        mListView = (AbsListView) findViewById(R.id.saved_recipes_list);
        mListView.setEmptyView(findViewById(R.id.saved_recipes_empty));
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Selected saved recipe: " + position);
                loadRecipeFull((Recipe_Short)mAdapter.getItem(position));
            }
        });

        // Hide progress bar:
        setProgressBar(false);
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

    /**
     * Helper method which displays a progress spinner + enables/disables the Activity:
     * @param visible boolean - is the spinner visible?
     */
    private void setProgressBar(boolean visible) {
        if (visible) {
            findViewById(R.id.saved_recipes_progress).setVisibility(View.VISIBLE);
            mListView.setEnabled(false);
        } else {
            findViewById(R.id.saved_recipes_progress).setVisibility(View.GONE);
            mListView.setEnabled(true);
        }
    }

    private void loadRecipeFull(Recipe_Short recipe_short) {
        // TODO: account for customOnly here!
        // Check network connectivity:
        if (!ActivityHelper.isConnected(getApplicationContext())) {
            ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.wifi_warning_short);
        } else {
            // Network connectivity exists! Load recipe depending on custom:
            setProgressBar(true);
            if (customOnly)
                loadFromAWS(recipe_short);
            else
                loadFromAPI(recipe_short);
        }
    }

    private void loadFromAWS(final Recipe_Short recipe_short) {
        // TODO: this.
    }

    private void loadFromAPI(final Recipe_Short recipe_short) {
        controller.getRecipeInformationAsync(recipe_short.getId(), new APICallBack<DynamicResponse>() {
            @Override
            public void onSuccess(HttpContext context, DynamicResponse response) {
                try {
                    Gson gson = new Gson();
                    Recipe_Full recipe_full = gson.fromJson(response.parseAsString(), Recipe_Full.class);

                    // Call the method to launch the ViewSingleRecipeActivity:
                    setProgressBar(false);
                    viewRecipe(recipe_short, recipe_full);

                } catch (ParseException e) {
                    ActivityHelper.displayUnknownError(getApplicationContext(), e.getLocalizedMessage());
                    Log.e(TAG, "Parsing recipe information failed!\n" + e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                ActivityHelper.displayUnknownError(getApplicationContext(), error.getLocalizedMessage());
                Log.e(TAG, "Getting recipe information failed! See below:\n" + error.getLocalizedMessage());
                setProgressBar(false);
            }
        });
    }

    /**
     * This method handles the launching of the ViewSingleRecipeActivity to display the full details of a
     * selected recipe.
     *
     * @param r_short - the short version of the selected recipe.
     * @param r_full  - the full version of the selected recipe.
     */
    private void viewRecipe(Recipe_Short r_short, Recipe_Full r_full) {
        // TODO: account for custom recipes here!?
        // Call our intent, including the recipe details in it!
        Intent intent = new Intent(this, ViewSingleRecipeActivity.class);
        intent.putExtra("view_single_recipe", r_full);
        intent.putExtra("recipe_short", r_short);
        startActivity(intent);
    }
}
