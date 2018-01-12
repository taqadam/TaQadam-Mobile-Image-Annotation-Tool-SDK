package com.recoded.taqadam;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class Classification extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    TextView questions;
    Spinner answers;


    ArrayList<String> QuestionsList;
    ArrayList<ArrayList<String>> AnswersList;
    ViewPager pager;
    ClissificationSliderAdapter adapter = new ClissificationSliderAdapter(this);
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);
      //  pager = findViewById(R.id.pager);
        pager.setAdapter(adapter);

        Log.v("POSISION", String.valueOf(adapter.po));

        questions = findViewById(R.id.question);
        answers = findViewById(R.id.answers);


        pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                ArrayAdapter<String> adapter = new ArrayAdapter<>(Classification.this,
                        android.R.layout.simple_spinner_item,
                        AnswersList.get(position));
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                answers.setAdapter(adapter);
                answers.setOnItemSelectedListener(Classification.this);
                questions.setText(QuestionsList.get(position));

                index = position;

            }
        });

        Log.v("index", String.valueOf(index));
        //The Questions List

        QuestionsList = new ArrayList<>();
        QuestionsList.add("this is a..........");
        QuestionsList.add("what is the name of this  animal??");
        QuestionsList.add("what is the color of this  car??");
        questions.setText(QuestionsList.get(0));
        //The List of The  Answers

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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Classification.this,
                android.R.layout.simple_spinner_item,
                AnswersList.get(0));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        answers.setAdapter(adapter);
        answers.setOnItemSelectedListener(this);

    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        switch (index) {

            case 0:

                switch (i) {

                    case 1:
                        Toast.makeText(this, "GREAT YOU CHECKED THE RIGHT ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(this, "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case 1:

                switch (i) {
                    case 1:
                        Toast.makeText(this, "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(this, "GREAT YOU CHECKED THE RIGHT ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(this, "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

            case 2:

                switch (i) {

                    case 1:
                        Toast.makeText(this, "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(this, "SORRY YOU CHECKED THE WRONG ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(this, "GREAT YOU CHECKED THE RIGHT ANSWER", Toast.LENGTH_SHORT).show();
                        break;
                }
                break;

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


}

