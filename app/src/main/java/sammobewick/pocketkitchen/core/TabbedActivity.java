package sammobewick.pocketkitchen.core;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.data_objects.Ingredient_Search;
import sammobewick.pocketkitchen.data_objects.ListItem;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Full;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;

public class TabbedActivity extends AppCompatActivity implements
        KitchenFragment.OnFragmentInteractionListener, RecipeFragment.OnFragmentInteractionListener, ShoppingListFragment.OnFragmentInteractionListener, View.OnClickListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//

    // ID values for the various fragments:
    private static final int KITCHEN_FRAG_ID = 0;
    private static final int LIST_FRAG_ID    = 1;
    private static final int RECIPE_FRAG_ID  = 2;

    private SectionsPagerAdapter    mSectionsPagerAdapter;
    private ViewPager               mViewPager;

    // TODO: Decide if useful?
    private int lastFragmentId;

    // API Controller for calls to Spoonacular:
    private APIController           controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

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
                intent = new Intent(this, SettingsActivity.class);
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
    public void onKitchenFragmentInteraction(final Ingredient_Search i) {
        // Here we check that we have got an Ingredient_Search to display:
        if (i != null) {
            // Now display the dialog that allows us to edit the amounts of this ingredient:
            final Dialog dialog = new Dialog(TabbedActivity.this);

            dialog.setContentView(R.layout.ingredient_edit);
            dialog.setTitle("Edit List Item");

            // Get the views + set the text up:
            ((TextView) dialog.findViewById(R.id.txt_ingredient_name)).setText(i.getName());
            ((TextView) dialog.findViewById(R.id.txt_ingredient_qty_name)).setText(i.getUnitShort());

            final EditText edit_qty = (EditText) dialog.findViewById(R.id.edit_ingredient_qty);
            edit_qty.setText(String.valueOf(i.getAmount()));

            dialog.findViewById(R.id.btn_discard_ing).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    }
            );

            dialog.findViewById(R.id.btn_save_ing).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Ingredient_Search newI = new Ingredient_Search(
                                    i.getAisle(),
                                    Float.valueOf(edit_qty.getText().toString()),
                                    i.getId(),
                                    i.getImage(),
                                    i.getName(),
                                    i.getOriginal(),
                                    i.getUnitLong(),
                                    i.getUnitShort()
                            );

                            // Pass this update back to the fragment/adapter + give feedback:
                            KitchenFragment fragment = (KitchenFragment) mSectionsPagerAdapter.getItem(KITCHEN_FRAG_ID);
                            if (fragment.updateItemInData(i, newI)){
                                Snackbar.make(findViewById(R.id.main_content), R.string.feedback_success_change, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            } else {
                                Snackbar.make(findViewById(R.id.main_content), R.string.feedback_failed_change, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            }
                            // Close dialog:
                            dialog.dismiss();
                        }
                    }
            );
        }
        /* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Kitchen Action: " + i.getId(), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
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
     *
     * @param recipe_short
     */
    @Override
    public void onRecipeFragmentInteraction(final Recipe_Short recipe_short) {
        // Check network connectivity:
        ActivityHelper helper = new ActivityHelper(this);
        if (!helper.isConnected()) {
            Snackbar.make(findViewById(R.id.main_content), R.string.wifi_warning_short, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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
                        * System.out.println("RESPONSE-GSON: " + recipe_full.getTitle());
                        // END-DEBUG */

                        // Call the method to launch the RecipeActivity:
                        viewRecipe(recipe_short, recipe_full);

                    } catch (ParseException e) {
                        e.printStackTrace();
                        ActivityHelper helper = new ActivityHelper(getApplicationContext());
                        helper.displayErrorDialog(e.getLocalizedMessage());
                    }
                }

                @Override
                public void onFailure(HttpContext context, Throwable error) {
                    error.printStackTrace();
                    ActivityHelper helper = new ActivityHelper(getApplicationContext());
                    helper.displayErrorDialog(error.getLocalizedMessage());
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
     * This method handles the launching of the RecipeActivity to display the full details of a
     * selected recipe.
     * @param r_short - the short version of the selected recipe.
     * @param r_full - the full version of the selected recipe.
     */
    private void viewRecipe(Recipe_Short r_short, Recipe_Full r_full) {
        // Call our intent, including the recipe details in it!
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe_full", r_full);
        intent.putExtra("recipe_short", r_short);
        startActivity(intent);
    }

    @Override
    public void onShoppingFragmentInteraction(final ListItem item) {
        // TODO: Perhaps insert a limit on the minimum or a warning if this item is not custom.
        // Create a dialog so that we can add/amend an item:
        final Dialog dialog = new Dialog(TabbedActivity.this);

        dialog.setContentView(R.layout.shopping_item_edit);
        dialog.setTitle("Edit List Item"); // TODO: We may want to change this.

        // Get the views + set the text up:

        final EditText edit_qty_name    = (EditText) dialog.findViewById(R.id.edit_item_qty_name);
        final EditText edit_qty         = (EditText) dialog.findViewById(R.id.measurements_editText);
        final EditText edit_name        = (EditText) dialog.findViewById(R.id.edit_shopping_item);

        // As we are sometimes adding fresh ones, we need to only set this when not null:
        if (item != null) {
            edit_name.setText(item.getName());
            edit_qty.setText(String.valueOf(item.getAmount()));
            edit_qty_name.setText(item.getUnit());

            // Disable some editing if not a custom.
            if (!item.isCustom()) {
                edit_name.setEnabled(false);
                edit_qty_name.setEnabled(false);
            }
        }

        final ShoppingListFragment fragment = (ShoppingListFragment) mSectionsPagerAdapter.getItem(LIST_FRAG_ID);

        // Setting up the OnClickListeners:
        dialog.findViewById(R.id.btn_discard_itm).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Provide feedback to the user + close dialog:
                        Snackbar.make(findViewById(R.id.main_content), R.string.feedback_cancelled_change, Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                        dialog.dismiss();
                    }
                }
        );
        dialog.findViewById(R.id.btn_save_itm).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListItem newItem = new ListItem(
                                Float.valueOf(edit_qty.getText().toString()), // AMOUNT
                                0,                                  // TODO: ID
                                edit_name.getText().toString(),     // NAME
                                edit_qty_name.getText().toString()  // UNIT
                        );

                        // TODO: Update item in the current list:
                        if (item != null) {
                            if (fragment.updateItemInData(item, item)) {
                                // Provide feedback to the user + close dialog:
                                Snackbar.make(findViewById(R.id.main_content), R.string.feedback_success_change, Snackbar.LENGTH_SHORT)
                                        .setAction("Action", null).show();
                            } else {
                                // TODO: Error.
                            }
                        } else {
                            // TODO: Add a new item to the list:
                        }
                        dialog.dismiss();
                    }
                }
        );

        dialog.findViewById(R.id.btn_remove_itm).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fragment.removeItemInData(item)) {
                            Snackbar.make(findViewById(R.id.main_content), R.string.feedback_deleted_change, Snackbar.LENGTH_SHORT)
                                    .setAction("Action", null).show();
                        } else {
                            // TODO: Error.
                        }
                        dialog.dismiss();
                    }
                }
        );
        dialog.show();

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
     * @param type - This is the INTEGER representing our fragment index from mSectionsPager.
     */
    private void fragmentSelected(int type) {
        this.lastFragmentId = type;
        System.out.println("FRAG-SELECTED:" + type);
        switch (type) {
            case KITCHEN_FRAG_ID:   // Kitchen Fragment
                findViewById(R.id.fab_search_recipes).setVisibility(View.GONE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.GONE);
                findViewById(R.id.fab_scan_receipts).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.GONE);
                break;

            case LIST_FRAG_ID:      // Shopping Fragment
                findViewById(R.id.fab_search_recipes).setVisibility(View.GONE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.GONE);
                findViewById(R.id.fab_scan_receipts).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.VISIBLE);
                break;

            case RECIPE_FRAG_ID:    // Recipe Fragment:
                findViewById(R.id.fab_search_recipes).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_list).setVisibility(View.GONE);
                findViewById(R.id.fab_add_recipe).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_scan_receipts).setVisibility(View.GONE);
                break;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        // Check network connectivity on resume:
        ActivityHelper helper = new ActivityHelper(this);
        if (!helper.isConnected()) {
            Snackbar.make(findViewById(R.id.main_content), R.string.wifi_warning_short, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add_list:         // Adding to the Shopping List:
                this.onShoppingFragmentInteraction(null);

                /* DEBUG:
                Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_wip_feature, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // END-DEBUG */
                break;
            case R.id.fab_add_recipe:       // Adding recipes:
                ///* DEBUG:
                Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_wip_feature, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // END-DEBUG */
                break;
            case R.id.fab_scan_receipts:    // Scanning Receipts:
                ///* DEBUG:
                Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_wip_feature, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                // END-DEBUG */
                break;
            case R.id.fab_search_recipes:   // Search Recipes [advanced]:
                ///* DEBUG:
                Snackbar.make(findViewById(R.id.main_content), R.string.snackbar_wip_feature, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

            switch (position){
                case 0:
                    fragment = KitchenFragment.newInstance();
                    break;

                case 1:
                    fragment = ShoppingListFragment.newInstance();
                    break;

                case 2:
                    fragment = RecipeFragment.newInstance();
                    break;

                default:
                    fragment = KitchenFragment.newInstance();
                    break;
            }

            // Passing metadata information to the fragments:
            try {
                ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = ai.metaData;
                fragment.setArguments(bundle);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                ActivityHelper helper = new ActivityHelper(getApplicationContext());
                helper.displayErrorDialog(e.getLocalizedMessage());
            }

            return fragment;
        }

        @Override
        /**
         * Will always return 3 (our number of fragments). Generated by Android Studio.
         */
        public int getCount() {
            return 3;
        }

        @Override
        /**
         * Gets the page title using the resources in strings.xml
         */
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
