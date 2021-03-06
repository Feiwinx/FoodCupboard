package sammobewick.pocketkitchen.activities_fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.Constants;
import sammobewick.pocketkitchen.supporting.LocalFileHelper;

public class TabbedActivity extends AppCompatActivity implements
        MyKitchenFragment.OnFragmentInteractionListener, SearchRecipesFragment.OnFragmentInteractionListener, MyListFragment.OnFragmentInteractionListener, View.OnClickListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "TabbedActivity";

    // ID values for the various fragments:
    private static final int KITCHEN_FRAG_ID = 2;
    private static final int LIST_FRAG_ID = 1;
    private static final int RECIPE_FRAG_ID = 0;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private int lastFragmentId;
    private boolean firstTimeUsage;

    private SharedPreferences sharedPreferences;

    private com.github.clans.fab.FloatingActionMenu menu;

    // API Controller for calls to Spoonacular:
    private APIController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        sharedPreferences = getSharedPreferences(ActivityHelper.getPREF_SET(), MODE_PRIVATE);

        if (sharedPreferences.contains("firstTimeUsage")) {
            firstTimeUsage = sharedPreferences.getBoolean("firstTimeUsage", false);
        }
        sharedPreferences.edit().putBoolean("firstTimeUsage", true).apply();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(mSectionsPagerAdapter.getCount());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // API key is stored in application context:
        com.mashape.p.spoonacularrecipefoodnutritionv1.Configuration.initialize(this.getBaseContext());

        SpoonacularAPIClient api_client = new SpoonacularAPIClient();
        controller = api_client.getClient();

        // Here we are setting listeners on the multiple Floating Action Buttons:
        com.github.clans.fab.FloatingActionButton fab;
        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_scan_receipts);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_add_recipe);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_add_list);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_search_recipes);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_view_saved);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_view_my_recipes);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_export_list);
        fab.setOnClickListener(this);

        fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab_suggest_recipes);
        fab.setOnClickListener(this);

        menu = (com.github.clans.fab.FloatingActionMenu) findViewById(R.id.fab);

        // Initialise this method with the first fragment:
        mViewPager.setCurrentItem(lastFragmentId);
        fragmentSelected(lastFragmentId);

        if (!firstTimeUsage) {
            Intent introIntent = new Intent(this, TutorialActivity.class);
            startActivity(introIntent);
        } else {
            Log.i(TAG, sharedPreferences.getAll().toString());
            // Only download from Drive if the settings allow for it:
            boolean drive = false;
            if (sharedPreferences.contains(getString(R.string.pref_files_disable_drive))) {
                boolean pref = sharedPreferences.getBoolean(getString(R.string.pref_files_disable_drive), false);
                Log.d(TAG, "preference: " + pref);
                if (!sharedPreferences.getBoolean(getString(R.string.pref_files_disable_drive), false)) {
                    if (ActivityHelper.isConnected(TabbedActivity.this)) {
                        ActivityHelper.downloadFromDrive(TabbedActivity.this);
                        drive = true;
                    }
                }
            }
            // If not using Drive, then use local storage:
            if (!drive) {
                Log.d(TAG, "Loading data from local as we haven't loaded from Drive");
                LocalFileHelper helper = new LocalFileHelper(TabbedActivity.this);
                helper.loadAll();
            }
        }
    }

    /**
     * Inflates option menu.
     * @param menu Menu - being the menu.
     * @return boolean - always true here.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    /**
     * Listens for interactions with the options menu (or action bar items).
     * @param item MenuItem - being the item that was pressed.
     * @return boolean - being the result (super).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int id = item.getItemId();

        // Here we check what option menu item was selected using resources:
        switch (id) {
            // Launch Settings activity:
            case R.id.action_settings:
                intent = new Intent(this, MySettingsActivity.class);
                startActivity(intent);
                break;

            // Launch LoginActivity with extra that we are logged in:
            case R.id.action_account:
                intent = new Intent(this, LoginActivity.class);
                intent.putExtra("signedIn", true);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Interface from fragment. Notifies us if the fragment has been interacted with (in this case,
     * an item selected from the ListView).
     * @param item Ingredient - being the item from the list.
     */
    @Override
    public void onKitchenFragmentInteraction(final Ingredient item) {
        ActivityHelper.dialogKitchenItem(TabbedActivity.this, item);
        /* DEBUG:
        helper.displaySnackBarNoAction(R.id.main_content, R.string.snackbar_wip_feature);
        // END-DEBUG */
    }

    /**
     * Here we get an update from the Kitchen fragment, and adjust our selected fragment.
     */
    @Override
    public void onKitchenFragmentSelected(boolean visible) {
        this.fragmentSelected(KITCHEN_FRAG_ID);
    }

    /**
     * Interface from fragment. Notifies us if the fragment has been interacted with (in this case,
     * if a recipe has been selected from the list, we will want to display it).
     * @param recipe_short Recipe_Short - being the basic recipe data.
     */
    @Override
    public void onRecipeFragmentInteraction(final Recipe_Short recipe_short) {
        // Check network connectivity:
        if (!ActivityHelper.isConnected(getApplicationContext())) {
            ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.wifi_warning_short);
        } else {
            // Network connectivity exists! Here we get the full recipe information:
            controller.getRecipeInformationAsync(recipe_short.getId(), new APICallBack<DynamicResponse>() {
                @Override
                public void onSuccess(HttpContext context, DynamicResponse response) {
                    try {
                        /* DEBUG: TEST GSON:
                        System.out.println("RESPONSE-HEADERS: " + response.getHeaders());
                        System.out.println("RESPONSE-STRING: " + response.parseAsString());
                        // END-DEBUG */

                        Gson gson = new Gson();
                        Recipe_Full recipe_full = gson.fromJson(response.parseAsString(), Recipe_Full.class);

                        /* DEBUG: TEST GSON:
                        * System.out.println("RESPONSE-GSON: " + view_single_recipe.getTitle());
                        // END-DEBUG */

                        // Call the method to launch the ViewSingleRecipeActivity:
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
                }
            });
        }
        /* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Recipe: " + recipeID, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        // END-DEBUG */
    }

    /**
     * Here we get an update from the Recipe fragment, and adjust our selected fragment.
     */
    @Override
    public void onRecipeFragmentSelected(boolean visible) {
        this.fragmentSelected(RECIPE_FRAG_ID);
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
    }

    /**
     * Interface from fragment. Notifies us if the fragment has been interacted with (in this case,
     * a item in the ListView has been selected).
     * @param item Ingredient - the item that was selected.
     */
    @Override
    public void onShoppingFragmentInteraction(final Ingredient item) {
        ActivityHelper.dialogShoppingItem(TabbedActivity.this, item);
        /* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Shopping Action: " + i.getId(), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        // END-DEBUG */
    }

    /**
     * Here we get an update from the ShoppingList fragment, and adjust our selected fragment.
     */
    @Override
    public void onShoppingFragmentSelected(boolean visible) {
        this.fragmentSelected(LIST_FRAG_ID);
    }

    /**
     * This method adjusts our Floating Action Buttons so that only the applicable actions are
     * visible.
     *
     * @param type - This is the INTEGER representing our fragment index from mSectionsPager.
     */
    private void fragmentSelected(int type) {
        Log.d(TAG, "Fragment selected. ID of " + type);
        this.lastFragmentId = type;
        menu.close(false);

        // These lines here solved the FAB issues that existed before:
        findViewById(R.id.fab_add_list).setVisibility(View.GONE);
        findViewById(R.id.fab_export_list).setVisibility(View.GONE);

        switch (type) {
            case KITCHEN_FRAG_ID:   // Kitchen Fragment
                findViewById(R.id.fab_add_list).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_scan_receipts).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_view_saved).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_view_my_recipes).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_search_recipes).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.GONE);
                findViewById(R.id.fab_export_list).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_suggest_recipes).setVisibility(View.GONE);
                break;

            case LIST_FRAG_ID:      // Shopping Fragment
                findViewById(R.id.fab_add_list).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_scan_receipts).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_view_saved).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_view_my_recipes).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_search_recipes).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_export_list).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.GONE);
                findViewById(R.id.fab_suggest_recipes).setVisibility(View.GONE);
                break;

            case RECIPE_FRAG_ID:    // Recipe Fragment:
                findViewById(R.id.fab_add_recipe).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_scan_receipts).setVisibility(View.GONE);
                //findViewById(R.id.fab_search_recipes).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_view_saved).setVisibility(View.VISIBLE);
                //findViewById(R.id.fab_view_my_recipes).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_suggest_recipes).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.GONE);
                findViewById(R.id.fab_export_list).setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Helper method which allows us to get the Export String
     * @return String - being the data that will be used when exporting the current list.
     */
    private String getExportString() {
        String result = "";
        List<Ingredient> data;
        PocketKitchenData pkData = PocketKitchenData.getInstance();

        // Construct our string using one of the following:
        if (lastFragmentId == LIST_FRAG_ID) {
            pkData.updateToBuy();
            data = pkData.getToBuy();
            result += "Shopping List:\n";
        } else {
            data = pkData.getInCupboards();
            result += "My Cupboards:\n";
        }

        // Collate information:
        for (Ingredient i: data) {
            result += i.getAmount() + " " + i.getUnitShort() + " of " + i.getName() + "\n";
        }
        return result + "\n";
    }

    /**
     * Allows us to listen for returns to this activity. We will want to save our data as in the
     * other activities, data can be changed! We will also check for network connections to inform
     * the user.
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();

        // Save our data:
        LocalFileHelper helper = new LocalFileHelper(this);
        helper.saveAll();

        // Check network connectivity on resume:
        if (!ActivityHelper.isConnected(getApplicationContext())) {
            Log.i(TAG, "No connection onPostResume!");
            ActivityHelper.displaySnackBarNoAction(TabbedActivity.this, R.id.main_content, R.string.wifi_warning_short);
        }

        // Return to last fragment selected:
        mViewPager.setCurrentItem(lastFragmentId);
    }

    /**
     * Overridden. This allows us to sync our data to Drive (if shared preferences state we can).
     */
    @Override
    public void finish() {
        if (sharedPreferences.contains(getString(R.string.pref_files_disable_drive))) {
            ///* DEBUG:
            boolean pref = sharedPreferences.getBoolean(getString(R.string.pref_files_disable_drive), false);
            Log.d(TAG, "preference: " + pref);
            //*/ END-DEBUG
            if (!sharedPreferences.getBoolean(getString(R.string.pref_files_disable_drive), false)) {
                if (ActivityHelper.isConnected(TabbedActivity.this))
                    ActivityHelper.uploadToDrive(TabbedActivity.this);
            }
        }
        super.finish();
    }

    /**
     * Overridden. This allows us to close the FAB menu (user requested feature) if open. Else, it
     * will show a dialog to confirm exiting was intended.
     */
    @Override
    public void onBackPressed() {
        FloatingActionMenu fam = (FloatingActionMenu) findViewById(R.id.fab);
        if (fam.isOpened()) {
            fam.close(true);
            return;
        }

        new AlertDialog.Builder(new ContextThemeWrapper(TabbedActivity.this, R.style.myDialog))
                .setTitle("Confirm Exit")
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Overridden. OnPause method for when the Activity is left. Save is called as most changes
     * will occur from this Activity.
     */
    @Override
    protected void onPause() {
        // Save all files:
        LocalFileHelper helper = new LocalFileHelper(this);
        helper.saveAll();
        super.onPause();
    }

    /**
     * OnClick as we listen for various clicks in this Activity.
     * @param v View - being the View which was pressed.
     */
    @Override
    public void onClick(View v) {
        Intent intent;

        // Switch our View ID:
        switch (v.getId()) {
            case R.id.fab_add_list:         // Adding to the Shopping List:
                if (lastFragmentId == LIST_FRAG_ID)
                    this.onShoppingFragmentInteraction(null);

                if (lastFragmentId == KITCHEN_FRAG_ID)
                    this.onKitchenFragmentInteraction(null);

                /* DEBUG:
                Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_wip_feature, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // END-DEBUG */
                break;
            case R.id.fab_add_recipe:       // Adding recipes:
                intent = new Intent(this, AddRecipeActivity.class);
                startActivity(intent);
                /* DEBUG:
                ActivityHelper.displaySnackBarNoAction(TabbedActivity.this, R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;
            case R.id.fab_scan_receipts:    // Scanning Receipts:
                ///* DEBUG:
                ActivityHelper.displaySnackBarNoAction(TabbedActivity.this, R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;
            case R.id.fab_search_recipes:   // Search Recipes [advanced]:
                intent = new Intent(this, AdvancedSearchActivity.class);
                startActivity(intent);
                /* DEBUG:
                ActivityHelper.displaySnackBarNoAction(TabbedActivity.this, R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;

            case R.id.fab_view_saved:       // View saved recipes.
                intent = new Intent(this, MySavedRecipesActivity.class);
                intent.putExtra(Constants.INTENT_CUSTOM_ID, false);
                startActivity(intent);
                /* DEBUG:
                ActivityHelper.displaySnackBarNoAction(TabbedActivity.this, R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;

            case R.id.fab_view_my_recipes:  // View My Created Recipes.
                intent = new Intent(this, MySavedRecipesActivity.class);
                intent.putExtra(Constants.INTENT_CUSTOM_ID, true);
                startActivity(intent);
                /* DEBUG:
                ActivityHelper.displaySnackBarNoAction(TabbedActivity.this, R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;

            case R.id.fab_export_list:  // Export current list.
                intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_TEXT, getExportString());
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject_line));
                intent.setType("text/plain");
                startActivity(Intent.createChooser(intent, getString(R.string.action_export_intent_list)));
                break;

            case R.id.fab_suggest_recipes: // Suggest Recipes
                suggestRecipes();
                break;
        }
    }

    /**
     * Method for getting recipe suggestions based on cupboards.
     * Passes the query to the fragment for searching, but handles empty kitchen here.
     */
    private void suggestRecipes() {
        PocketKitchenData pkData = PocketKitchenData.getInstance();

        String query = pkData.getIngredientQuery();

        if (query.length() > 0) {
            ((SearchRecipesFragment) getSupportFragmentManager().findFragmentByTag(getFragmentTag(RECIPE_FRAG_ID)))
                    .runSuggestionSearch(query);
        } else {
            ActivityHelper.displayKnownError(getApplicationContext(),
                    "To do this, you'll need some ingredients in your Kitchen first!");
        }
    }

    /**
     * Helper method for identifying a fragment using it's tag. The structure of which is defined
     * by Android.
     * @param index int - being the value of the fragment.
     * @return String - being the tag.
     */
    private String getFragmentTag(int index) {
        return "android:switcher:" + mViewPager.getId() + ":" + index;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            switch (position) {
                case KITCHEN_FRAG_ID:
                    fragment = MyKitchenFragment.newInstance();
                    break;

                case LIST_FRAG_ID:
                    fragment = MyListFragment.newInstance();
                    break;

                case RECIPE_FRAG_ID:
                    fragment = SearchRecipesFragment.newInstance();
                    break;

                default: // Should never be reached!
                    fragment = MyKitchenFragment.newInstance();
                    break;
            }

            // Passing metadata information to the fragments:
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;

                // Also add the dietary preferences from the shared preferences:
                if (sharedPreferences.contains(getString(R.string.pref_dietary_dairy))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_dairy), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_dairy), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_eggs))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_eggs), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_eggs), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_gluten))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_gluten), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_gluten), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_vegetarian))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_vegetarian), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_vegetarian), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_vegan))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_vegan), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_vegan), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_pescetarian))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_pescetarian), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_pescetarian), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_nuts))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_nuts), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_nuts), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_seafood))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_seafood), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_seafood), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_shellfish))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_shellfish), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_shellfish), value);
                }
                if (sharedPreferences.contains(getString(R.string.pref_dietary_soy))) {
                    boolean value = sharedPreferences.getBoolean(getString(R.string.pref_dietary_soy), false);
                    bundle.putBoolean(getString(R.string.pref_dietary_soy), value);
                }

                // Set the arguments:
                fragment.setArguments(bundle);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.getLocalizedMessage());
                ActivityHelper.displayUnknownError(getApplicationContext(), e.getLocalizedMessage());
            }

            return fragment;
        }

        /**
         * Will always return 3 (our number of fragments). Generated by Android Studio.
         */
        @Override
        public int getCount() {
            return 3;
        }

        /**
         * Gets the page title using the resources in strings.xml
         */
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case KITCHEN_FRAG_ID:
                    return getResources().getString(R.string.section_kitchen);
                case LIST_FRAG_ID:
                    return getResources().getString(R.string.section_list);
                case RECIPE_FRAG_ID:
                    return getResources().getString(R.string.section_recipes);
            }
            return null;
        }
    }
}
