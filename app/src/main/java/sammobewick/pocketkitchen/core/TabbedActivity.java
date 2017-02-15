package sammobewick.pocketkitchen.core;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Button;
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
import sammobewick.pocketkitchen.supporting.Recipe_Full;
import sammobewick.pocketkitchen.supporting.Recipe_Short;

public class TabbedActivity extends AppCompatActivity implements
        KitchenFragment.OnFragmentInteractionListener, RecipeFragment.OnFragmentInteractionListener, ShoppingListFragment.OnFragmentInteractionListener
{
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS ACTIVITY:                                                   //
    //********************************************************************************************//

    private SectionsPagerAdapter    mSectionsPagerAdapter;
    private ViewPager               mViewPager;

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

        // TODO: Correct the floating action button so it can be used on all screens.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Logic here for FAB.
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Here we check what option menu item was selected using resources:
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onKitchenFragmentInteraction(int kitchenID) {
        // TODO: This should prompt for editing the item in the kitchen to update stock.
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.ingredient_edit);
        dialog.setTitle("Edit Ingredient");
        dialog.show();

        // Establish the dialog views we need to use:
        final TextView  txt_ing_name     = (TextView) dialog.findViewById(R.id.txt_ingredient_name);
        //txt_ing_name.setText();

        final TextView  txt_ing_qty_name = (TextView) dialog.findViewById(R.id.txt_ingredient_qty_name);
        //txt_ing_qty_name.setText();

        final EditText  edit_ing_qty     = (EditText) dialog.findViewById(R.id.edit_ingredient_qty);
        final Button    btn_save         = (Button)   dialog.findViewById(R.id.btn_save_ing);
        final Button    btn_discard      = (Button)   dialog.findViewById(R.id.btn_discard_ing);
        final Button    btn_remove       = (Button)   dialog.findViewById(R.id.btn_remove_ing);

        // TODO: This might be able to show additional information if the middleware supports it.

        // Set on click listeners:
        btn_discard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_remove.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: Program the removal of this item from the data.
                dialog.dismiss();
            }
        });

        btn_discard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: Program the updating of this item in the data.
                dialog.dismiss();
            }
        });

        ///* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Kitchen Action: " + kitchenID, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        ///* END-DEBUG
    }

    @Override
    public void onRecipeFragmentInteraction(final Recipe_Short recipe_short) {
        // TODO: Query Spoonacular for full recipe information:
        controller.getRecipeInformationAsync(recipe_short.getId(), new APICallBack<DynamicResponse>() {
            @Override
            public void onSuccess(HttpContext context, DynamicResponse response) {
                System.out.println("RESPONSE-HEADERS: " + response.getHeaders());
                try {
                    System.out.println("RESPONSE-STRING: " + response.parseAsString());

                    Gson gson = new Gson();
                    Recipe_Full recipe_full = gson.fromJson(response.parseAsString(), Recipe_Full.class);

                    /* DEBUG: TEST GSON:
                     * System.out.println("RESPONSE-GSON: " + recipe_full.getTitle());
                     */

                    // Call method which handles this data:
                    viewRecipe(recipe_short, recipe_full);

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(HttpContext context, Throwable error) {
                System.out.println("API-ERROR: " + error.getMessage());
            }
        });
        /* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Recipe: " + recipeID, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        /* END-DEBUG */
    }

    private void viewRecipe(Recipe_Short r_short, Recipe_Full r_full) {
        // Call our intent, including the recipe details in it!
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe_full", r_full);
        intent.putExtra("recipe_short", r_short);
        startActivity(intent);
    }

    @Override
    public void onShoppingFragmentInteraction(int shoppingID) {
        // TODO: This should prompt for editing the item in the shopping list.
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.ingredient_edit);
        dialog.setTitle("Edit Item");
        dialog.show();

        // Establish the dialog views we need to use:
        final TextView  txt_itm_qty_name    = (TextView) dialog.findViewById(R.id.txt_item_qty_name);
        final EditText  edit_itm_name       = (EditText) dialog.findViewById(R.id.edit_shopping_item);
        final EditText  edit_itm_qty        = (EditText) dialog.findViewById(R.id.measurements_editText);
        final Button    btn_save            = (Button)   dialog.findViewById(R.id.btn_save_itm);
        final Button    btn_discard         = (Button)   dialog.findViewById(R.id.btn_discard_itm);
        final Button    btn_remove          = (Button)   dialog.findViewById(R.id.btn_remove_itm);

        // TODO: This might be able to show additional information if the middleware supports it.

        // Set on click listeners:
        btn_discard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_remove.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: Program the removal of this item from the data.
                dialog.dismiss();
            }
        });

        btn_discard.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // TODO: Program the updating of this item in the data.
                dialog.dismiss();
            }
        });

        ///* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Shopping Action: " + shoppingID, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        ///* END-DEBUG
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
                // TODO: Proper error handling here.
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
                case 0:
                    return getResources().getString(R.string.section_kitchen);
                case 1:
                    return getResources().getString(R.string.section_list);
                case 2:
                    return getResources().getString(R.string.section_recipes);
            }
            return null;
        }
    }
}
