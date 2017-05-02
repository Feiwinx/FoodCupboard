package sammobewick.pocketkitchen.activities_fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.adapters.CustomRecipeAdapter;
import sammobewick.pocketkitchen.adapters.SavedRecipesAdapter;
import sammobewick.pocketkitchen.aws_intents.Dynamo_Download_Json;
import sammobewick.pocketkitchen.data_objects.DynamoDB_Wrapper;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.Constants;

/**
 * This class is for displaying sets of recipes. One set is the custom recipes that the user has
 * created. The other set will be whatever ones the user has added to their cooklist (usually
 * Spoonacular ones, but sometimes user-created).
 */
public class MySavedRecipesActivity extends AppCompatActivity implements CustomRecipeAdapter.AdapterParent {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "MySavedRecipeActivity";

    private String              urlStart;
    private AbsListView         mListView;
    private BaseAdapter         mAdapter;
    private APIController       controller;

    private Recipe_Short        last_short;
    private boolean             customOnly;
    private boolean             editRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_saved_recipes);
        setupActionBar();

        // Get whether we need to only display custom or not from the intent extras:
        customOnly = false;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey(Constants.INTENT_CUSTOM_ID)) {
                customOnly = extras.getBoolean(Constants.INTENT_CUSTOM_ID);
            }
        }

        if (!customOnly) {
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

            mAdapter = new SavedRecipesAdapter(urlStart);

        } else {
            // We only need the other adapter here instead of the other stuff.
            mAdapter = new CustomRecipeAdapter(this);
            ((TextView)findViewById(R.id.saved_recipes_empty)).setText(getString(R.string.empty_my_recipes));

            // Set up intent filters for getting Broadcasts back from AWS intent-services:
            IntentFilter filter = new IntentFilter(Constants.BC_DOWNLOAD_NAME);
            LocalBroadcastManager.getInstance(this).registerReceiver(new downloadReceiver(), filter);
        }

        // UNIVERSAL SETUP OF ADAPTER(S):
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (customOnly)
            getMenuInflater().inflate(R.menu.menu_my_recipes, menu);
        else
            super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_custom_recipe:
                Intent intent = new Intent(this, AddRecipeActivity.class);
                startActivity(intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is the preparation method for loading recipes, which then defers to other methods
     * to transition to the ViewSingleActivity method.
     * @param recipe_short Recipe_Short - the recipe that was selected from the ListView.
     */
    private void loadRecipeFull(Recipe_Short recipe_short) {
        if (recipe_short != null) {
            last_short = recipe_short;

            Log.e(TAG, recipe_short.toString());
            Log.e(TAG, last_short.toString());

            // Check network connectivity:
            if (!ActivityHelper.isConnected(getApplicationContext())) {
                ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.wifi_warning_short);
            } else {
                editRequest = false;
                // Network connectivity exists! Load either type of recipe using other method:
                setProgressBar(true);
                if (customOnly) {
                    loadFromAWS(recipe_short);
                } else {
                    loadFromAPI(recipe_short);
                }
            }
        } else {
            Log.e(TAG, "passed recipe is null!");
        }
    }

    /**
     * Method to load an AWS recipe. Passes it to another activity after.
     * @param recipe_short Recipe_Short - recipe to display.
     */
    private void loadFromAWS(final Recipe_Short recipe_short) {
        Log.d(TAG, "Loading from AWS: " + recipe_short.getId());
        String jsonKey = String.valueOf(recipe_short.getId());
        Intent jsonDown = new Intent(MySavedRecipesActivity.this, Dynamo_Download_Json.class);
        jsonDown.putExtra(Constants.JSON_DYNAMO_KEY, jsonKey);
        this.startService(jsonDown);
    }

    /**
     * Method to load a recipe from the API. Passes it to another activity after.
     * @param recipe_short Recipe_Short - recipe to display.
     */
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

    private void awsDownloaded(Recipe_Full rf) {
        setProgressBar(false);
        if (editRequest) {
            editRecipe(last_short, rf);
        } else {
            viewRecipe(last_short, rf);
        }
    }

    private void editRecipe(Recipe_Short rs, Recipe_Full rf) {
        Intent intent = new Intent(this, AddRecipeActivity.class);
        Log.d(TAG, rs.toString());
        Log.d(TAG, rf.toString());
        intent.putExtra("view_single_recipe", rf);
        intent.putExtra("recipe_short", rs);
        startActivity(intent);
        finish();
    }

    /**
     * This method handles the launching of the ViewSingleRecipeActivity to display the full details of a
     * selected recipe.
     *
     * @param r_short - the short version of the selected recipe.
     * @param r_full  - the full version of the selected recipe.
     */
    private void viewRecipe(Recipe_Short r_short, Recipe_Full r_full) {
        // Call our intent, including the recipe details in it!
        Intent intent = new Intent(this, ViewSingleRecipeActivity.class);
        intent.putExtra("view_single_recipe", r_full);
        intent.putExtra("recipe_short", r_short);
        startActivity(intent);
        finish();
    }

    /**
     * This is passed back from the custom adapter, allowing us to prepare the edit process. The
     * selection process is handled by the ListView OnClickListener, but as the edit process is a
     * image button on the row, we need to pass it back up.
     * @param position int - being the position of the item in the list associated with the EditButton.
     */
    @Override
    public void OnEditButtonPressed(int position) {
        editRequest = true;
        last_short = (Recipe_Short) mAdapter.getItem(position);
        loadFromAWS((Recipe_Short)mAdapter.getItem(position));
    }

    /**
     * Private inner class to get Broadcast responses back in to this Activity.
     * We register this in the OnCreate but basically this just handles unpacking the intent.
     */
    private class downloadReceiver extends BroadcastReceiver {

        // Prevent instantiation:
        private downloadReceiver() { /* UPLOAD RECEIVER */ }

        @Override
        public void onReceive(Context context, Intent intent) {
            // Unpack Intent + pass to Activity:
            boolean result          = intent.getExtras().getBoolean(Constants.BC_DOWNLOAD_ID);

            Log.d(TAG, "DownloadBC: " + intent.getExtras().toString());

            if (result) {
                DynamoDB_Wrapper data = (DynamoDB_Wrapper) intent.getExtras().get(Constants.BC_DOWNLOAD_DATA);
                Recipe_Full recipe = new Recipe_Full(data != null ? data.getJsonString() : null);

                // Send callback:
                awsDownloaded(recipe);
            } else {
                ActivityHelper.displayUnknownError(MySavedRecipesActivity.this, getString(R.string.feedback_unknown_problem));
            }
        }
    }
}
