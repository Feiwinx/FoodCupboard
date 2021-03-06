package sammobewick.pocketkitchen.activities_fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.SearchView;

import sammobewick.pocketkitchen.R;
import sammobewick.pocketkitchen.adapters.MyKitchenAdapter;
import sammobewick.pocketkitchen.data_objects.Ingredient;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MyKitchenFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MyKitchenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyKitchenFragment extends Fragment implements SearchView.OnQueryTextListener {
    private MyKitchenAdapter mAdapter;
    private OnFragmentInteractionListener mListener;

    // ****************************************************************************************** //
    //                                 CONSTRUCTORS + SET-UP:                                     //
    // ****************************************************************************************** //

    /**
     * Empty constructor.
     */
    public MyKitchenFragment() { /* Empty constructor */ }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment MyKitchenFragment.
     */
    public static MyKitchenFragment newInstance() {
        return new MyKitchenFragment();
    }

    /**
     * OnCreate. Creates our adapter and gets extra information from the activities.
     * @param savedInstanceState Bundle - saved state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch our intent parameter - IT SHOULD NEVER BE NULL.
        String urlStart = "";
        if (getArguments() != null) {
            if (getArguments().containsKey("recipe_image_url")) {
                urlStart = getArguments().getString("recipe_image_url");
            }
        }

        // Prepare our adapter:
        mAdapter = new MyKitchenAdapter(urlStart);
    }

    /**
     * OnCreateView. Inflates the layout file, sets up the listview with the adapter, and prepares
     * the searchview listener.
     * @param inflater LayoutInflater - used to inflate the view
     * @param container ViewGroup - container
     * @param savedInstanceState Bundle - saved state.
     * @return View - the created view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_kitchen, container, false);

        // Prepare our ListView:
        AbsListView mListView = (AbsListView) view.findViewById(R.id.kitchen_list);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty_kitchen));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                             @Override
                                             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                 mListener.onKitchenFragmentInteraction(mAdapter.getItem(position));
                                             }
                                         }
        );

        // Prepare our SearchView:
        SearchView mSearchView = (SearchView) view.findViewById(R.id.kitchen_search);
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (mListener != null) {
            mListener.onKitchenFragmentSelected(isVisibleToUser);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (mAdapter != null) {
            mAdapter.setFilterText(newText);
        }
        return false;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnFragmentInteractionListener {
        void onKitchenFragmentInteraction(final Ingredient i);

        void onKitchenFragmentSelected(boolean visible);
    }
}
