package sammobewick.pocketkitchen.activities_fragments;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.HTTP_RecipeShort;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;

/**
 * Advanced Search Activity provides the interface for refining a recipe search. It then passes the
 * results back to PocketKitchenData and exits the activity. No preferences are saved / used  so
 * this gives the user quite a bit of control when searching!
 */
public class AdvancedSearchActivity extends AppCompatActivity implements View.OnClickListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS FRAGMENT:                                                   //
    //********************************************************************************************//
    private static final String TAG = "AdvSearchActivity";
    private APIController       controller;
    private ProgressBar         mProgressBar;

    // Strings for building our search query:
    private String              query;
    private String              cuisine;
    private String              diet;
    private String              intolerances;
    private String              type;
    private String              exclusions;

    /**
     * OnCreate method. Set up our API controller, checkboxes, and listeners:
     * @param savedInstanceState Bundle - saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);
        setupActionBar();

        // Prepare our API Controller:
        SpoonacularAPIClient api_client = new SpoonacularAPIClient();
        controller = api_client.getClient();

        // Set up the Vegan checkbox to disable/enable the vegetarian one:
        CheckBox chkVegan = (CheckBox) findViewById(R.id.check_diet_vegan);
        chkVegan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                findViewById(R.id.check_diet_vegetarian).setEnabled(isChecked);
            }
        });

        // Click listeners for drop-down sections:
        findViewById(R.id.lbl_recipe_type).setOnClickListener(this);
        findViewById(R.id.lbl_recipe_cuisine).setOnClickListener(this);
        findViewById(R.id.lbl_dietary_restrictions).setOnClickListener(this);

        // Prepare everything else:
        mProgressBar = (ProgressBar) findViewById(R.id.recipe_adv_search_progress);
        type = diet = intolerances = cuisine = exclusions = "";
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
     * Overriden to handle options menu items being pressed. In this case it's our back button (home),
     * clear button, and search button.
     * @param item MenuItem - the item that was pressed.
     * @return boolean - calls super.OnOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // The reason for this is to return to the recipe list rather than resetting the
            // TabbedActivity the default fragment. Preventing user confusion.
            case android.R.id.home:
                onBackPressed();
                return true;

            // Clear button:
            case R.id.action_clear:
                clear();
                break;

            // Search button:
            case R.id.action_search:
                if (!ActivityHelper.isConnected(AdvancedSearchActivity.this)) {
                    ActivityHelper.displaySnackBarNoAction(AdvancedSearchActivity.this, R.id.sv_adv_search, R.string.wifi_warning_short);
                } else { search(); }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to run our search. As we have our recipes displayed defined in our singleton then
     * all we need to do is handle the result as usual (using callback), and close this activity.
     */
    private void search() {
        // Show progress bar, set up the data, make query, then exit the activity:
        setProgressBar(true);
        setSearchData();
        controller.searchRecipesAsync(
                query,  // query                - required. The natural language recipe query.
                cuisine,   // cuisine              - optional.
                diet,   // diet                 - optional.
                exclusions,   // excludeIngredients   - optional.
                intolerances,   // intolerance         - optional.
                false,   // limitLicense         - optional.
                20,   // number               - optional, but default to 20 for now
                0,   // offset               - optional, returns number of results from result 0.
                type,   // type                 - optional.
                null,   // queryParameters      - optional.
                new APICallBack<DynamicResponse>() {
                    @Override
                    public void onSuccess(HttpContext context, DynamicResponse response) {
                        // Hide spinner:
                        setProgressBar(false);
                        try {

                            HTTP_RecipeShort handler = new HTTP_RecipeShort(response.parseAsString());

                            List<Recipe_Short> data = handler.getResults();

                            // Load data in handler:
                            PocketKitchenData pkData = PocketKitchenData.getInstance();
                            pkData.setRecipesDisplayed(data);

                            // End activity:
                            onBackPressed();

                        } catch (ParseException e) { // Thrown by parseAsString()
                            ActivityHelper.displayUnknownError(AdvancedSearchActivity.this, e.getLocalizedMessage());
                            Log.e(TAG, "Parsing recipe failed!\n" + e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onFailure(HttpContext context, Throwable error) {
                        ActivityHelper.displayUnknownError(AdvancedSearchActivity.this, error.getLocalizedMessage());
                        Log.e(TAG, "Recipe query failed!\n" + error.getLocalizedMessage());
                    }
                });
    }

    /**
     * Simple method to reset all fields to their default values (unticked / empty):
     */
    private void clear() {
        ((EditText) findViewById(R.id.edit_search_terms)).setText("");
        ((EditText) findViewById(R.id.edit_search_exclusions)).setText("");

        ((CheckBox) findViewById(R.id.check_american)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_african)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_british)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_caribbean)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_chinese)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_french)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_german)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_greek)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_jewish)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_indian)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_italian)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_irish)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_japanese)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_korean)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_mexican)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_middleeastern)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_spanish)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_southern)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_thai)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_vietnamese)).setChecked(false);

        ((CheckBox) findViewById(R.id.check_main)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_side)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_starter)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_dessert)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_salad)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_bread)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_breakfast)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_soup)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_drink)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_sauce)).setChecked(false);

        ((CheckBox) findViewById(R.id.check_diet_vegan)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_vegetarian)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_pescatarian)).setChecked(false);

        ((CheckBox) findViewById(R.id.check_diet_dairy)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_eggs)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_gluten)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_nuts)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_seafood)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_shellfish)).setChecked(false);
        ((CheckBox) findViewById(R.id.check_diet_soy)).setChecked(false);
    }

    /**
     * This creates our search criteria by checking what is ticked and adding it to the relevant
     * part of the query.
     */
    private void setSearchData() {
        // ***** QUERY: ***** //
        query = ((EditText) findViewById(R.id.edit_search_terms)).getText().toString();

        // ***** TYPE: ***** //
        if (((CheckBox) findViewById(R.id.check_main)).isChecked()) {
            type += "main course, ";
        }
        if (((CheckBox) findViewById(R.id.check_side)).isChecked()) {
            type += "side dish, ";
        }
        if (((CheckBox) findViewById(R.id.check_starter)).isChecked()) {
            type += "appetizer, ";
        }
        if (((CheckBox) findViewById(R.id.check_dessert)).isChecked()) {
            type += "dessert, ";
        }
        if (((CheckBox) findViewById(R.id.check_salad)).isChecked()) {
            type += "salad, ";
        }
        if (((CheckBox) findViewById(R.id.check_bread)).isChecked()) {
            type += "bread, ";
        }
        if (((CheckBox) findViewById(R.id.check_breakfast)).isChecked()) {
            type += "breakfast, ";
        }
        if (((CheckBox) findViewById(R.id.check_soup)).isChecked()) {
            type += "soup, ";
        }
        if (((CheckBox) findViewById(R.id.check_drink)).isChecked()) {
            type += "drink, ";
        }
        if (((CheckBox) findViewById(R.id.check_sauce)).isChecked()) {
            type += "sauce, ";
        }

        // ***** CUISINE: ***** //
        if (((CheckBox) findViewById(R.id.check_african)).isChecked()) {
            cuisine += "african, ";
        }
        if (((CheckBox) findViewById(R.id.check_american)).isChecked()) {
            cuisine += "american, ";
        }
        if (((CheckBox) findViewById(R.id.check_british)).isChecked()) {
            cuisine += "british, ";
        }
        if (((CheckBox) findViewById(R.id.check_caribbean)).isChecked()) {
            cuisine += "caribbean, ";
        }
        if (((CheckBox) findViewById(R.id.check_chinese)).isChecked()) {
            cuisine += "chinese, ";
        }
        if (((CheckBox) findViewById(R.id.check_french)).isChecked()) {
            cuisine += "french, ";
        }
        if (((CheckBox) findViewById(R.id.check_german)).isChecked()) {
            cuisine += "german, ";
        }
        if (((CheckBox) findViewById(R.id.check_greek)).isChecked()) {
            cuisine += "greek, ";
        }
        if (((CheckBox) findViewById(R.id.check_jewish)).isChecked()) {
            cuisine += "jewish, ";
        }
        if (((CheckBox) findViewById(R.id.check_indian)).isChecked()) {
            cuisine += "indian, ";
        }
        if (((CheckBox) findViewById(R.id.check_italian)).isChecked()) {
            cuisine += "italian, ";
        }
        if (((CheckBox) findViewById(R.id.check_irish)).isChecked()) {
            cuisine += "irish, ";
        }
        if (((CheckBox) findViewById(R.id.check_japanese)).isChecked()) {
            cuisine += "japanese, ";
        }
        if (((CheckBox) findViewById(R.id.check_korean)).isChecked()) {
            cuisine += "korean, ";
        }
        if (((CheckBox) findViewById(R.id.check_mexican)).isChecked()) {
            cuisine += "mexican, ";
        }
        if (((CheckBox) findViewById(R.id.check_middleeastern)).isChecked()) {
            cuisine += "middle eastern, ";
        }
        if (((CheckBox) findViewById(R.id.check_spanish)).isChecked()) {
            cuisine += "spanish, ";
        }
        if (((CheckBox) findViewById(R.id.check_southern)).isChecked()) {
            cuisine += "southern, ";
        }
        if (((CheckBox) findViewById(R.id.check_thai)).isChecked()) {
            cuisine += "thai, ";
        }
        if (((CheckBox) findViewById(R.id.check_vietnamese)).isChecked()) {
            cuisine += "vietnamese, ";
        }

        // ***** DIET: ***** //
        if (((CheckBox) findViewById(R.id.check_diet_vegan)).isChecked()) {
            diet += "vegan, ";
        } else if (((CheckBox) findViewById(R.id.check_diet_vegetarian)).isChecked()) {
            diet += "vegetarian, ";
        }
        if (((CheckBox) findViewById(R.id.check_diet_pescatarian)).isChecked()) {
            diet += "pescetarian, ";
        }

        // ***** INTOLERANCES ***** //
        if (((CheckBox) findViewById(R.id.check_diet_dairy)).isChecked()) {
            intolerances += "dairy, ";
        }
        if ((((CheckBox) findViewById(R.id.check_diet_eggs))).isChecked()) {
            intolerances += "egg, ";
        }
        if (((CheckBox) findViewById(R.id.check_diet_gluten)).isChecked()) {
            intolerances += "gluten, ";
        }
        if (((CheckBox) findViewById(R.id.check_diet_nuts)).isChecked()) {
            intolerances += "nuts, ";
        }
        if (((CheckBox) findViewById(R.id.check_diet_seafood)).isChecked()) {
            intolerances += "seafood, ";
        }
        if (((CheckBox) findViewById(R.id.check_diet_shellfish)).isChecked()) {
            intolerances += "shellfish, ";
        }
        if (((CheckBox) findViewById(R.id.check_diet_soy)).isChecked()) {
            intolerances += "soy, ";
        }

        // ***** EXCLUSIONS ***** //
        exclusions = ((EditText) findViewById(R.id.edit_search_exclusions)).getText().toString();
    }

    /**
     * Helper method which displays a progress spinner + enables/disables the Activity:
     * @param visible boolean - is the spinner visible?
     */
    private void setProgressBar(boolean visible){
        if (visible) {
            mProgressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    /**
     * Standard method to create our options menu.
     * @param menu Menu - being the menu.
     * @return boolean - being the result (always true).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_adv_search, menu);
        return true;
    }

    /**
     * This activity implements OnClickListener therefore we need this method.
     * Here we handle showing/hiding the gists related to the title areas. The basic logic here
     * is just to show/hide chunks of data to make it a bit nicer. We also change the image.
     * @param v View - the view which is clicked on.
     */
    @Override
    public void onClick(View v) {
        View gist;
        int id = v.getId();
        switch (id){
            case R.id.lbl_recipe_type:
                gist = findViewById(R.id.grid_recipe_types);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.lbl_recipe_cuisine:
                gist = findViewById(R.id.grid_recipe_cuisine);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.lbl_dietary_restrictions:
                gist = findViewById(R.id.grid_dietary_restrictions);
                if (gist.isShown()) {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_up, 0);
                    gist.setVisibility(View.GONE);
                } else {
                    ((TextView) v).setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
                    gist.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
