package sammobewick.pocketkitchen.core;

import android.app.Dialog;
import android.content.Intent;
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

import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.supporting.Recipe_Full;

public class TabbedActivity extends AppCompatActivity implements
        KitchenFragment.OnFragmentInteractionListener, RecipeFragment.OnFragmentInteractionListener, ShoppingListFragment.OnFragmentInteractionListener
{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private APIController controller;

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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
    public void onRecipeFragmentInteraction(int recipeID) {
        // TODO: Query Spoonacular for full recipe information:

        //controller ...

        // TODO: Call intent on whatever it is that will display this recipe in full!

        //Intent intent ...

        ///* DEBUG:
        Snackbar.make(findViewById(R.id.main_content), "Recipe: " + recipeID, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        ///* END-DEBUG
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

            // TODO: Match the parameters for new instance here! Likely pass arrays!
            switch (position){
                case 0:
                    fragment = KitchenFragment.newInstance("","");
                    break;

                case 1:
                    fragment = ShoppingListFragment.newInstance("", "");
                    break;

                case 2:
                    fragment = RecipeFragment.newInstance("", "");
                    break;

                default:
                    fragment = KitchenFragment.newInstance("","");
                    break;
            }
            return fragment;
        }

        @Override
        /**
         * Will always return 3. Generated by Android Studio.
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
