package sammobewick.pocketkitchen.core;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.Ingredient;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;
import sammobewick.pocketkitchen.supporting.LocalFileHelper;

public class TabbedActivity extends AppCompatActivity implements
        MyKitchenFragment.OnFragmentInteractionListener, SearchRecipesFragment.OnFragmentInteractionListener, MyListFragment.OnFragmentInteractionListener, View.OnClickListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//
    private static final String TAG = "TabbedActivity";

    // ID values for the various fragments:
    private static final int KITCHEN_FRAG_ID = 0;
    private static final int LIST_FRAG_ID = 1;
    private static final int RECIPE_FRAG_ID = 2;

    private static final String SHARED_PREFERENCES = "pocketKitchenPreferences";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private int lastFragmentId;

    private SharedPreferences sharedPreferences;

    // API Controller for calls to Spoonacular:
    private APIController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        // Initialise this method with the first fragment:
        this.fragmentSelected(KITCHEN_FRAG_ID);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

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
        /* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Recipe: " + recipeID, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        // END-DEBUG */
    }

    /**
     * Here we get an update from the Recipe fragment, and adjust our selected fragment.
     */
    @Override
    public void onRecipeFragementSelected(boolean visible) {
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
        switch (type) {
            case KITCHEN_FRAG_ID:   // Kitchen Fragment
                //findViewById(R.id.fab_scan_receipts).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_view_saved).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_search_recipes).setVisibility(View.GONE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.GONE);
                break;

            case LIST_FRAG_ID:      // Shopping Fragment
                //findViewById(R.id.fab_scan_receipts).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_view_saved).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_search_recipes).setVisibility(View.GONE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.GONE);
                break;

            case RECIPE_FRAG_ID:    // Recipe Fragment:
                findViewById(R.id.fab_search_recipes).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_view_saved).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.GONE);
                //findViewById(R.id.fab_scan_receipts).setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // Check network connectivity on resume:
        if (!ActivityHelper.isConnected(getApplicationContext())) {
            Log.i(TAG, "No connection onPostResume!");
            ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.wifi_warning_short);
        }

        // Return to last fragment selected:
        mViewPager.setCurrentItem(lastFragmentId);
    }

    @Override
    protected void onPause() {
        // Save all files:
        LocalFileHelper helper = new LocalFileHelper(this);
        helper.saveAll();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
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
                ///* DEBUG:
                ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;
            case R.id.fab_scan_receipts:    // Scanning Receipts:
                ///* DEBUG:
                ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;
            case R.id.fab_search_recipes:   // Search Recipes [advanced]:
                ///* DEBUG:
                ActivityHelper.displaySnackBarNoAction(getApplicationContext(), R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;

            case R.id.fab_view_saved:       // View saved recipes.
                Intent intent = new Intent(this, MySavedRecipesActivity.class);
                startActivity(intent);
                /* DEBUG:
                helper.displaySnackBarNoAction(R.id.main_content, R.string.snackbar_wip_feature);
                // END-DEBUG */
                break;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;

            switch (position) {
                case 0:
                    fragment = MyKitchenFragment.newInstance();
                    break;

                case 1:
                    fragment = MyListFragment.newInstance();
                    break;

                case 2:
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
                ActivityHelper.displayErrorDialog(getApplicationContext(), e.getLocalizedMessage());
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
