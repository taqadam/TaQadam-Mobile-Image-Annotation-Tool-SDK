package com.recoded.taqadam;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentTasks.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentTasks#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentTasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FragmentTasks() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentTasks.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentTasks newInstance(String param1, String param2) {
        FragmentTasks fragment = new FragmentTasks();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_tasks, container, false);

        ListView listView = (ListView) view.findViewById(R.id.list_view);
        ArrayList<Task> array = new ArrayList<Task>();
        //we have many task types based on user performance (Tutorial,training,qualifier,locked)

        array.add(new Task("Tutorial", "Bounding Box", "Learn how to draw boxes around objects"));
        array.add(new Task("Tutorial", "Labeling", "Learn how to draw boxes around objects"));
        array.add(new Task("Tutorial", "categorization", "Learn how to categorize objects "));
        array.add(new Task("Tutorial", "Image validation", "Learn how to validate images"));
        array.add(new Task("Qualifier", "Bounding Box", "test your bounding boxes skills for identifying items within an image"));
        array.add(new Task("Qualifier", "Labeling", "put your skills in work to label objects"));
        array.add(new Task("Qualifier", "categorization", "categorize objects in the given image"));
        array.add(new Task("Qualifier", "Image validation", "choose the valid image from the given images "));
        TaskAdapter taskAdapter = new TaskAdapter(getActivity(), R.layout.task, array);
        listView.setAdapter(taskAdapter);

        return view;


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
