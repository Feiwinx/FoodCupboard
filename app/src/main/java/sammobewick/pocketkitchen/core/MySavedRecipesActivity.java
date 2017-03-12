package sammobewick.pocketkitchen.core;

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

import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.data_objects.SavedRecipesAdapter;
import sammobewick.pocketkitchen.data_objects.SearchedRecipesAdapter;
import sammobewick.pocketkitchen.supporting.ActivityHelper;

public class MySavedRecipesActivity extends AppCompatActivity {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "MySavedRecipeActivity";

    private String              urlStart;
    private AbsListView         mListView;
    private SavedRecipesAdapter mAdapter;
    private APIController       controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_saved_recipes);
        setupActionBar();

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

        // Set up adapter:
        mAdapter = new SavedRecipesAdapter(urlStart);

        mListView = (AbsListView) findViewById(R.id.saved_recipes_list);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findViewById(R.id.saved_recipes_empty));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Selected saved recipe: " + position);
                System.out.println("SLECTED");
                loadRecipeFull(mAdapter.getItem(position));
            }
        });

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

    private void setProgressBar(boolean visible) {
        if (visible) {
            findViewById(R.id.saved_recipes_progress).setVisibility(View.VISIBLE);
            mListView.setEnabled(false);
        } else {
            findViewById(R.id.saved_recipes_progress).setVisibility(View.GONE);
            mListView.setEnabled(true);
        }
    }

    private void loadRecipeFull(final Recipe_Short recipe_short) {
        // Check network connectivity:
        if (!ActivityHelper.isConnected(getApplicationContext())) {
            ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.wifi_warning_short);
        } else {
            setProgressBar(true);
            // Network connectivity exists! Here we get the full recipe information:
            controller.getRecipeInformationAsync(recipe_short.getId(), new APICallBack<DynamicResponse>() {
                @Override
                public void onSuccess(HttpContext context, DynamicResponse response) {
                    try {
                        Gson gson = new Gson();
                        Recipe_Full recipe_full = gson.fromJson(response.parseAsString(), Recipe_Full.class);

                        // Call the method to launch the ViewSingleRecipeActivity:
                        viewRecipe(recipe_short, recipe_full);

                    } catch (ParseException e) {
                        ActivityHelper.displayErrorDialog(getApplicationContext(), e.getLocalizedMessage());
                        Log.e(TAG, "Parsing recipe information failed!\n" + e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(HttpContext context, Throwable error) {
                    ActivityHelper.displayErrorDialog(getApplicationContext(), error.getLocalizedMessage());
                    Log.e(TAG, "Getting recipe information failed! See below:\n" + error.getLocalizedMessage());
                }
            });
        }
    }

    /**
     * This method handles the launching of the ViewSingleRecipeActivity to display the full details of a
     * selected recipe.
     *
     * @param r_short - the short version of the selected recipe.
     * @param r_full  - the full version of the selected recipe.
     */
    private void viewRecipe(Recipe_Short r_short, Recipe_Full r_full) {
        setProgressBar(false);
        // Call our intent, including the recipe details in it!
        Intent intent = new Intent(this, ViewSingleRecipeActivity.class);
        intent.putExtra("view_single_recipe", r_full);
        intent.putExtra("recipe_short", r_short);
        startActivity(intent);
    }
}
