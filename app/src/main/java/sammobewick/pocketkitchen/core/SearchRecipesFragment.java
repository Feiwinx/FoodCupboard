package sammobewick.pocketkitchen.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.text.ParseException;
import java.util.List;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.HTTP_RecipeShort;
import sammobewick.pocketkitchen.data_objects.PocketKitchenData;
import sammobewick.pocketkitchen.data_objects.SearchedRecipesAdapter;
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
public class SearchRecipesFragment extends Fragment implements SearchView.OnQueryTextListener {
    //********************************************************************************************//
    //  VARIABLES / HANDLERS FOR THIS FRAGMENT:                                                   //
    //********************************************************************************************//
    private static final String TAG = "SearchRecipesFragment";

    private AbsListView             mListView;
    private SearchedRecipesAdapter  mAdapter;
    private SearchView              mSearchView;
    private ProgressBar             mProgressBar;
    private APIController           controller;
    private String                  intolerances;
    private String                  dietQuery;

    private OnFragmentInteractionListener mListener;

    // ****************************************************************************************** //
    //                                 CONSTRUCTORS + SET-UP:                                     //
    // ****************************************************************************************** //

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch our arguments:
        String urlStart = "";
        intolerances = "";
        dietQuery = "";

        if (getArguments() != null) {
            Bundle args = getArguments();
            if (args.containsKey("recipe_image_url")) {
                urlStart = args.getString("recipe_image_url");
            }

            // Generate the search queries:
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_recipes, container, false);

        // Prepare our ListView:
        mListView = (AbsListView) view.findViewById(R.id.recipe_list);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty_recipe));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    @Override
    public void onResume() {
        super.onResume();
        setProgressBar(false);
    }

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

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setProgressBar(boolean visible){
        if (visible) {
            mProgressBar.setVisibility(View.VISIBLE);
            mListView.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mListView.setEnabled(true);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mAdapter.setData(null);
        setProgressBar(true);
        // Make the API call using the submitted data:
        controller.searchRecipesAsync(
                query,  // query                - required. The natural language recipe query.
                "",   // cuisine              - optional.
                dietQuery,   // diet                 - optional.
                "",   // excludeIngredients   - optional.
                intolerances,   // intolerance         - optional.
                false,   // limitLicense         - optional.
                20,   // number               - optional, but default to 20 for now
                0,   // offset               - optional, returns number of results from result 0.
                "",   // type                 - optional.
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

                            /* [OLD] Before listeners were used, this was.
                            // Pass to adapter:
                            mAdapter.setData(pkData.getRecipesDisplayed());
                            //*/

                            // Provides proper feedback when no results are returned:
                            if (data.size() == 0) {
                                ((TextView) mListView.getEmptyView()).setText(R.string.no_results);
                            }

                        } catch (ParseException e) {
                            ActivityHelper.displayErrorDialog(getActivity(), e.getLocalizedMessage());
                            Log.e(TAG, "Parsing recipe failed!\n" + e.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onFailure(HttpContext context, Throwable error) {
                        ActivityHelper.displayErrorDialog(getActivity(), error.getLocalizedMessage());
                        Log.e(TAG, "Recipe query failed!\n" + error.getLocalizedMessage());
                    }
                });

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mListener != null) {
            mListener.onRecipeFragementSelected(isVisibleToUser);
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

        void onRecipeFragementSelected(boolean visible);
    }
}
