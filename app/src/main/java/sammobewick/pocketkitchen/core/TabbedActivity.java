package sammobewick.pocketkitchen.core;

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

import sammobewick.pocketkitchen.R;

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

    private String apiKey = "89uaBM1DYFmshbBN98rIAI62xuB2p1ZF3rfjsnWxuvaM0RPHpi";

    // TODO: Specify arrays for storing the objects (i.e. recipe info, kitchen info, etc.)

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

        // TODO: Correct the floating action button so it can be used on all screens.
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //testGetData();
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
            // TODO: This can also show information about when the item was last purchased or the like.
            Snackbar.make(findViewById(R.id.main_content), "Kitchen Action: " + kitchenID, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }

        @Override
        public void onRecipeFragmentInteraction(int recipeID) {
            // TODO: This should load the recipe in full.
            Snackbar.make(findViewById(R.id.main_content), "Recipe Action: " + recipeID, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }

        @Override
        public void onShoppingFragmentInteraction(int shoppingID) {
            // TODO: This should show the edit dialog for the shopping item selected!
            Snackbar.make(findViewById(R.id.main_content), "Shopping Action: " + shoppingID, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
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
