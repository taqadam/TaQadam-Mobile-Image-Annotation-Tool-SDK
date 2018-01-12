package com.recoded.taqadam.JobActivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.recoded.taqadam.R;
import com.recoded.taqadam.databinding.ActivityClassificationBinding;

import java.util.ArrayList;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class PlaceholderFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
   // ActivityClassificationBinding binding;
    ArrayList<String> QuestionsList;
    ArrayList<ArrayList<String>> AnswersList;
    int[] img;

    TextView question;
    ImageView imageView;
    Spinner spinner;
    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(int sectionNumber) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        /**
         * TODO wissam
         * THIS (getArguments().getInt(ARG_SECTION_NUMBER)) IS A RANDOM NUMBER,
         * AND IT SHOULD BE THE NUMBER OF THE TASKS IN THE FIREBASE
         * AND THE (5) IS CHANGED AS WE WANT, IT IS JUST FOR
         * SHOWING THE FRAGMENTS RANDOMLY AFTER 5
         **/
        if (getArguments().getInt(ARG_SECTION_NUMBER) < 5) {
            View rootView = inflater.inflate(R.layout.activity_classification, container, false);

            question=rootView.findViewById(R.id.question);
            imageView=rootView.findViewById(R.id.images);
            spinner=rootView.findViewById(R.id.answers);

            QuestionsList = new ArrayList<>(9);
            QuestionsList.add("this is a..........");
            QuestionsList.add("what is the name of this  animal??");
            QuestionsList.add("what is the color of this  car??");
            QuestionsList.add("Ahmad");
            QuestionsList.add("Wissam");
            QuestionsList.add("Nawal");
            QuestionsList.add("Ali");
            QuestionsList.add("YOusra");
            QuestionsList.add("Zahra");

            /**
             * TODO wissam
             * THOSE ARRAYS SHOULD BE REPLACED BY THE DATABASE FIREBASE
             */
            img = new int[]{R.drawable.banner_taqadam,
                    R.drawable.car,
                    R.drawable.tiger,
                    R.drawable.cubes,
                    R.drawable.hand_dollar,
                    R.drawable.logo_taqadam,
                    R.drawable.human,
                    R.drawable.maron_logo};

            AnswersList = new ArrayList<>();

            //The Answers of The Question 1
            AnswersList.add(new ArrayList<String>());
            AnswersList.get(0).add(0, "");
            AnswersList.get(0).add(1, "human");
            AnswersList.get(0).add(2, "animal");
            AnswersList.get(0).add(3, "nothing");

            //The Answers of The Question 2
            AnswersList.add(new ArrayList<String>());
            AnswersList.get(1).add(0, "");
            AnswersList.get(1).add(1, "lion");
            AnswersList.get(1).add(2, "tiger");
            AnswersList.get(1).add(3, "cat");

            //The Answers of The Question 3
            AnswersList.add(new ArrayList<String>());
            AnswersList.get(2).add(0, "");
            AnswersList.get(2).add(1, "red");
            AnswersList.get(2).add(2, "black");
            AnswersList.get(2).add(3, "blue");

            //Array Adapter to Add The answers to the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item,
                    AnswersList.get(AnswersList.size()-1));//IT SHOULD CHANGED
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
           spinner.setAdapter(adapter);
           spinner.setOnItemSelectedListener(PlaceholderFragment.this);

            question.setText(QuestionsList.get(getArguments().getInt(ARG_SECTION_NUMBER)));
           imageView.setImageResource(img[getArguments().getInt(ARG_SECTION_NUMBER)]);

            return rootView;
        } else {
            View rootView = inflater.inflate(R.layout.activity_task, container, false);


            return rootView;
        }

    }

    /**
     *TILL NOW WE DON'T CONNECT WITH FIREBASE
     * SO IT SHOULD BE ADD THE FUNCTIONALITY WHEN
     * WE CLICK THE ITEM OF THE SPINNER.
     */

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (getArguments().getInt(ARG_SECTION_NUMBER)) {

            case 0:

                switch (i) {

                    case 1:
                        Toast.makeText(getContext(), "GREAT YOU CHECKED THE RIGHT ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(), "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case 1:

                switch (i) {
                    case 1:
                        Toast.makeText(getContext(), "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(), "GREAT YOU CHECKED THE RIGHT ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(getContext(), "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case 2:

                switch (i) {

                    case 1:
                        Toast.makeText(getContext(), "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(), "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(getContext(), "GREAT YOU CHECKED THE RIGHT ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

        }
        Log.v("INDEX", String.valueOf(getArguments().getInt(ARG_SECTION_NUMBER)));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}