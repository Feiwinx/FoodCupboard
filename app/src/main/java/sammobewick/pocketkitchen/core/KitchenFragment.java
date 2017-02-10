package sammobewick.pocketkitchen.core;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import sammobewick.pocketkitchen.R;

import sammobewick.pocketkitchen.supporting.KitchenAdapter;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KitchenFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KitchenFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KitchenFragment extends Fragment {
    // Generated argument code (unsure if to be used yet):
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";
    //private String mParam1;
    //private String mParam2;

    // ListView/ListAdapter:
    private AbsListView mListView;
    private ListAdapter mAdapter;

    private OnFragmentInteractionListener mListener;

    public KitchenFragment() { /* Empty constructor */}

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KitchenFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KitchenFragment newInstance(String param1, String param2) {
        KitchenFragment fragment = new KitchenFragment();

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
            // = getArguments().getString(ARG_PARAM2);
        }
        mAdapter = new KitchenAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_kitchen, container, false);

        // Prepare our ListView:
        mListView = (AbsListView) view.findViewById(R.id.kitchen_list);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(view.findViewById(R.id.empty_kitchen));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mListener.onKitchenFragmentInteraction(position);
                }
            }
        );

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
        void onKitchenFragmentInteraction(int kitchenID);
    }
}
