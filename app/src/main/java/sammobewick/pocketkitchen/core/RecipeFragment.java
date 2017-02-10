package sammobewick.pocketkitchen.core;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.google.gson.Gson;
import com.mashape.p.spoonacularrecipefoodnutritionv1.SpoonacularAPIClient;
import com.mashape.p.spoonacularrecipefoodnutritionv1.controllers.APIController;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.APICallBack;
import com.mashape.p.spoonacularrecipefoodnutritionv1.http.client.HttpContext;
import com.mashape.p.spoonacularrecipefoodnutritionv1.models.DynamicResponse;

import java.text.ParseException;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.communication.HTTP_RecipeShort;
import sammobewick.pocketkitchen.supporting.Recipe_Full;
import sammobewick.pocketkitchen.supporting.RecipeShortAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecipeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecipeFragment extends Fragment implements SearchView.OnQueryTextListener {
    // Generated argument code (unsure if to be used yet):
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";
    //private String mParam1;
    //private String mParam2;

    private AbsListView         mListView;
    private RecipeShortAdapter  mAdapter;
    private SearchView          mSearchView;
    private APIController       controller;

    private OnFragmentInteractionListener mListener;

    // ****************************************************************************************** //
    //                                 CONSTRUCTORS + SET-UP:                                     //
    // ****************************************************************************************** //

    public RecipeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecipeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecipeFragment newInstance(String param1, String param2) {
        RecipeFragment fragment = new RecipeFragment();

        // Generated argument code (unsure if to be used yet):
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            // Generated argument code (unsure if to be used yet):
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Prepare our adapter:
        mAdapter = new RecipeShortAdapter();

        // Prepare our API Controller:
        SpoonacularAPIClient api_client = new SpoonacularAPIClient();
        controller = api_client.getClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        // Prepare our ListView:
        mListView = (AbsListView) view.findViewById(R.id.recipe_list);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty_recipe));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // TODO: Here we want to pass the recipe information (maybe) so we can load it in full!
                    mListener.onRecipeFragmentInteraction(mAdapter.getItem(position).getId());
                }
            }
        );

        // Prepare our SearchView:
        mSearchView = (SearchView) view.findViewById(R.id.recipe_search);
        mSearchView.setOnQueryTextListener(this);

        return view;
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        // Make the API call using the submitted data:
        controller.searchRecipesAsync(
                query,  // query                - required. The natural language recipe query.
                "",   // cuisine              - optional.
                "",   // diet                 - optional.
                "",   // excludeIngredients   - optional.
                "",   // intolerance         - optional.
                true,   // limitLicense         - optional.
                10,   // number               - optional, but default to 10 for now
                0,   // offset               - optional, returns number of results from result 0.
                "",   // type                 - optional.
                null,   // queryParameters      - optional.
                new APICallBack<DynamicResponse>() {
                    @Override
                    public void onSuccess(HttpContext context, DynamicResponse response) {
                        //API_Response.ReadHTTPResponse(response);
                        try {

                            Gson gson = new Gson();
                            HTTP_RecipeShort handler = new HTTP_RecipeShort(response.parseAsString());
                            mAdapter.setData(handler.getResults());

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(HttpContext context, Throwable error) {
                        System.out.println("API-ERROR: " + error.getMessage());
                    }
                });

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onRecipeFragmentInteraction(int recipeID);
    }
}
