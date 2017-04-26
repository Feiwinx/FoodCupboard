package sammobewick.pocketkitchen.activities_fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.FindByIngredientsModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.adapters.SearchedRecipesAdapter;
import sammobewick.pocketkitchen.communication.HTTP_RecipeShort;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.Recipe_Short;
import sammobewick.pocketkitchen.supporting.ActivityHelper;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchRecipesFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchRecipesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchRecipesFragment extends Fragment implements SearchView.OnQueryTextListener, AbsListView.OnScrollListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS FRAGMENT:                                                   //
    //********************************************************************************************//
    private static final String TAG = "SearchRecipesFragment";
    private static final int    RESULT_COUNT = 20;

    private static final boolean    LIMIT_LICENSE = false;

    private AbsListView             mListView;      // ListView
    private SearchedRecipesAdapter  mAdapter;       // ListView Adapter.
    private SearchView              mSearchView;    // SearchView itself.
    private ProgressBar             mProgressBar;   // Search Progress Bar.
    private APIController           controller;     // API controller.
    private String                  intolerances;   // from Settings.
    private String                  dietQuery;      // from Settings.
    private String                  lastQuery;      // last known query.
    private int                     offsetCount;    // last known result max.
    private boolean                 suggestions;    // toggle between search modes.

    // Parent (our Activity):
    private OnFragmentInteractionListener mListener;

    // ****************************************************************************************** //
    //                                 CONSTRUCTORS + SET-UP:                                     //
    // ****************************************************************************************** //

    /**
     * Empty Constructor.
     */
    public SearchRecipesFragment() { /* Empty constructor */ }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment SearchRecipesFragment.
     */
    public static SearchRecipesFragment newInstance() {
        return new SearchRecipesFragment();
    }

    /**
     * OnCreate method. Sets up the fragment, including adapter and dietary information to be used
     * in the searches.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prepare our arguments:
        String urlStart = "";
        intolerances    = "";
        dietQuery       = "";

        if (getArguments() != null) {
            Bundle args = getArguments();
            if (args.containsKey("recipe_image_url")) {
                urlStart = args.getString("recipe_image_url");
            }

            // Generate the preference information as singular queries:
            if (args.containsKey(getString(R.string.pref_dietary_vegan))) {
                if (args.getBoolean(getString(R.string.pref_dietary_vegan))) {
                    dietQuery += "vegan, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_vegetarian))) {
                if (args.getBoolean(getString(R.string.pref_dietary_vegetarian))) {
                    dietQuery += "vegetarian, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_pescetarian))) {
                if (args.getBoolean(getString(R.string.pref_dietary_pescetarian))) {
                    dietQuery += "pescetarian, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_dairy))) {
                if (args.getBoolean(getString(R.string.pref_dietary_dairy))) {
                    intolerances += "dairy, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_eggs))) {
                if (args.getBoolean(getString(R.string.pref_dietary_eggs))) {
                    intolerances += "egg, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_gluten))) {
                if (args.getBoolean(getString(R.string.pref_dietary_gluten))) {
                    intolerances += "gluten, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_nuts))) {
                if (args.getBoolean(getString(R.string.pref_dietary_nuts))) {
                    intolerances += "nuts, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_seafood))) {
                if (args.getBoolean(getString(R.string.pref_dietary_seafood))){
                    intolerances += "seafood, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_shellfish))) {
                if (args.getBoolean(getString(R.string.pref_dietary_shellfish))) {
                    intolerances += "shellfish, ";
                }
            }
            if (args.containsKey(getString(R.string.pref_dietary_soy))) {
                if (args.getBoolean(getString(R.string.pref_dietary_soy))) {
                    intolerances += "soy, ";
                }
            }
        }

        // Prepare our adapter:
        mAdapter = new SearchedRecipesAdapter(urlStart);

        // Pass any background saved data to adapter:
        PocketKitchenData pkData = PocketKitchenData.getInstance();
        mAdapter.setData(pkData.getRecipesDisplayed());

        // Prepare our API Controller:
        SpoonacularAPIClient api_client = new SpoonacularAPIClient();
        controller = api_client.getClient();
    }

    /**
     * Once the view is inflated, this is called and will set up our ListView and ProgressBar.
     * @param inflater LayoutInflater - inflater
     * @param container ViewGroup - container
     * @param savedInstanceState Bundle - saved state
     * @return View - our fragment's view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_recipes, container, false);

        // Prepare our ListView:
        mListView = (AbsListView) view.findViewById(R.id.recipe_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(this);
        mListView.setEmptyView(view.findViewById(R.id.empty_recipe));
        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        setProgressBar(true);
                        mListener.onRecipeFragmentInteraction(mAdapter.getItem(position));
                    }
                }
        );

        // Prepare our SearchView:
        mSearchView = (SearchView) view.findViewById(R.id.recipe_search);
        mSearchView.setOnQueryTextListener(this);

        mProgressBar = (ProgressBar)view.findViewById(R.id.recipe_search_progress);
        setProgressBar(false);

        return view;
    }

    /**
     * Overridden to prevent progress bar getting stuck.
     */
    @Override
    public void onResume() {
        super.onResume();
        setProgressBar(false);
    }

    /**
     * Standard for fragment usage. Created by IDE to ensure the Fragment is managed.
     * @param context Context - being the parent Activity.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * Standard for fragment usage. Created by IDE.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Helper method which displays a progress spinner + enables/disables the Activity:
     * @param visible boolean - is the spinner visible?
     */
    private void setProgressBar(boolean visible){
        if (visible) {
            closeKeyboard(getActivity(), mSearchView.getWindowToken());

            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mListView.setEnabled(true);
        }
    }

    /**
     * Required by SearchView interface and allows the text from there to be handled here (on submit).
     * Will pass the details to runSearch(...) to process it.
     * @param query String - being the text in the SearchView at the time of submission.
     * @return boolean - always false, results are handled in the callbacks.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        suggestions = false;
        lastQuery = query;
        runSearch(false);
        return false;
    }

    /**
     * Allows a search to be input from outside of the fragment (i.e. the suggestion FAB button on
     * the TabbedActivity). This sets up the runSearch() function to carry out the search as required.
     * @param query String - being the query to build off of. This should be a list of ingredients.
     */
    public void runSuggestionSearch(String query) {
        suggestions = true;
        lastQuery = query;
        runSearch(false);
    }

    /**
     * Helper method to close the keyboard (helps searching become more user friendly!).
     * @param c Context - our application.
     * @param windowToken - our Window token.
     */
    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }

    /**
     * Runs a search, with the information that it is new (false) or the same (true).
     * @param nextSet boolean - is the search a new one, or the same as previous?
     */
    public void runSearch(final boolean nextSet) {
        if (nextSet) {
            offsetCount += RESULT_COUNT;
        } else {
            PocketKitchenData.clearDrawables();
            mAdapter.setData(null);
            offsetCount = 0;
        }

        setProgressBar(true);
        // Make the API call using the submitted data:
        if (!suggestions) {
            mAdapter.setSuggestion(false);
            controller.searchRecipesAsync(
                    lastQuery,  // query                - required.
                    "",   // cuisine                    - optional.
                    dietQuery,   // diet                - optional.
                    "",   // excludeIngredients         - optional.
                    intolerances,   // intolerance      - optional.
                    LIMIT_LICENSE,   // limitLicense    - optional.
                    RESULT_COUNT,   // number           - optional.
                    offsetCount,   // offset            - optional.
                    "",   // type                       - optional.
                    null,   // queryParameters          - optional.
                    new MyAPICallback(nextSet));
        } else {
            mAdapter.setSuggestion(true);
            controller.findByIngredientsAsync(
                    lastQuery,      // query
                    LIMIT_LICENSE,  // limitlicense
                    RESULT_COUNT,   // number
                    1,              // rank (sorting)
                    null,           // query parameters
                    new MyAPICallbackSuggestion(nextSet));
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        /* Intended to clear the results when the text is removed. Minor usability idea from a user,
         * However, so far it has crashed horribly every time, and may not be worth the time to debug
         * given the various other features that matter. */

        /* ERROR:
        if (newText.length() == 0) {
            PocketKitchenData pkData = PocketKitchenData.getInstance();
            pkData.setRecipesDisplayed(new ArrayList<Recipe_Short>());
        }
        ///* END-ERROR */
        return false;
    }

    /**
     * Is called when the fragment visibility has changed; i.e. when swiped to or away from.
     * Allows the main activity to track which fragment is currently displayed.
     * @param isVisibleToUser - boolean is the fragment visible?
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mListener != null) {
            mListener.onRecipeFragmentSelected(isVisibleToUser);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        /* DO NOTHING. Required by interface usage. */
    }

    /**
     * This method is implemented as part of the OnScrollListener interface we use. It allows the
     * ListView to be continually scrolled, loading as many recipes as the API can provide.
     * @param view - ListView which is listened to.
     * @param firstVisibleItem - index of first visible
     * @param visibleItemCount - count of visible
     * @param totalItemCount - count of total in the list
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // Firstly, ensure we have a query, then check for when we reach the bottom of the list.
        if (lastQuery != null) {
            if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                runSearch(true);
            }
        }
    }

    /**
     * Custom callback class for standard recipe query. This is required by the API, and is triggered
     * once the query has been handled or timed out.
     */
    private class MyAPICallback implements APICallBack<DynamicResponse> {

        // Boolean for the type of query we are making (new or next).
        private boolean nextSet;

        public MyAPICallback(boolean nextSet) {
            this.nextSet = nextSet;
        }

        @Override
        public void onSuccess(HttpContext context, DynamicResponse response) {
            // Hide spinner:
            setProgressBar(false);
            try {
                // Convert resulting query into a set of Http result data (matching API):
                HTTP_RecipeShort handler = new HTTP_RecipeShort(response.parseAsString());

                // Get the recipe results:
                List<Recipe_Short> data = handler.getResults();

                // Load data in handler:
                PocketKitchenData pkData = PocketKitchenData.getInstance();
                if (!nextSet) {
                    pkData.setRecipesDisplayed(data);
                } else {
                    pkData.addRecipesDisplayed(data);
                }

                // Provides proper feedback when no results are returned:
                if (data.size() == 0) {
                    ((TextView) mListView.getEmptyView()).setText(R.string.no_results);
                }

            } catch (ParseException e) { // Thrown by parseAsString()
                ActivityHelper.displayUnknownError(getActivity(), e.getLocalizedMessage());
                Log.e(TAG, "Parsing recipe failed!\n" + e.getLocalizedMessage());
            }
        }

        @Override
        public void onFailure(HttpContext context, Throwable error) {
            // Unknown error. Originating from API or Mashape Key most likely.
            ActivityHelper.displayUnknownError(getActivity(), error.getLocalizedMessage());
            Log.e(TAG, "Recipe query failed!\n" + error.getLocalizedMessage());
            error.printStackTrace();
        }
    }

    /**
     * Custom callback class for suggested recipe query. This is required by the API, and is triggered
     * once the query has been handled or timed out. This variation allows for a Ingredient model,
     * rather than the simple text query. Reaches a different end-point on Spoonacular.
     */
    private class MyAPICallbackSuggestion implements APICallBack<List<FindByIngredientsModel>> {

        // Boolean for the type of query we are making (new or next).
        private boolean nextSet;

        public MyAPICallbackSuggestion(boolean nextSet) {
            this.nextSet = nextSet;
        }

        @Override
        public void onSuccess(HttpContext context, List<FindByIngredientsModel> response) {
            // Hide spinner:
            setProgressBar(false);
            try {
                // Convert resulting query into a set of Ingredient models, then use that to
                // construct a list of recipe results.
                List<Recipe_Short> data = new ArrayList<>();

                for (FindByIngredientsModel model : response) {
                    data.add(new Recipe_Short(
                            model.getId(),
                            model.getImage(),
                            null,
                            0,
                            model.getTitle()
                    ));
                }

                // Load data in handler:
                PocketKitchenData pkData = PocketKitchenData.getInstance();
                if (!nextSet) {
                    pkData.setRecipesDisplayed(data);
                } else {
                    pkData.addRecipesDisplayed(data);
                }

                // Provides proper feedback when no results are returned:
                if (data.size() == 0) {
                    ((TextView) mListView.getEmptyView()).setText(R.string.no_results);
                }

            } catch (Exception e) {
                // A parse exception isn't thrown by the methods, but has been thrown during testing
                // this is a little rough way of catching it without having the IDE kick up a fuss.
                ActivityHelper.displayUnknownError(getActivity(), e.getLocalizedMessage());
                Log.e(TAG, "Parsing recipe failed!\n" + e.getLocalizedMessage());
            }
        }

        @Override
        public void onFailure(HttpContext context, Throwable error) {
            // Unknown error. Originating from API or Mashape Key most likely.
            ActivityHelper.displayUnknownError(getActivity(), error.getLocalizedMessage());
            Log.e(TAG, "Suggestion query failed!\n" + error.getLocalizedMessage());
            error.printStackTrace();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onRecipeFragmentInteraction(Recipe_Short recipe_short);
        void onRecipeFragmentSelected(boolean visible);
    }
}
