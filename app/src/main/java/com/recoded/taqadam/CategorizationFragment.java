package com.recoded.taqadam;

import androidx.databinding.DataBindingUtil;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.recoded.taqadam.databinding.FragCategorizationBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Service;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class CategorizationFragment extends TaskFragment {
    private static final String TAG = CategorizationFragment.class.getSimpleName();

    private FragCategorizationBinding binding;
    private View.OnClickListener optionClickListener;
    private SparseArray<String> selectedOptions = new SparseArray<>();
    private boolean multiSelection = false;
    private Point displayDims;
    private List<TextView> viewsToBeAdded = new ArrayList<>();

    public CategorizationFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.frag_categorization, container, false);
        binding = DataBindingUtil.bind(rootView);
        //This is needed to align options
        displayDims = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displayDims);

        setupOptionClickListener();

        answer = new Answer(assignment.getId(), task.getId());
        taskImageView = binding.ivTaskImage;
        taskImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        if (task.getUrl().getScheme().equalsIgnoreCase("http") || task.getUrl().getScheme().equalsIgnoreCase("https")) {
            loadTaskImage(task.getUrl());
        }

        binding.tvInstruction.setVisibility(View.GONE);
        this.multiSelection = type == Service.Services.CLASSIFICATION;
        List<String> options = assignment.getJob().getOptions();
        for (int i = 0; i < options.size(); i++) {
            TextView option = getStyledTextView(options.get(i));
            option.setId(i);
            addOptionToGrid(option);
        }

        return rootView;
    }

    private void loadTaskImage(Uri uri) {
        Picasso.with(getContext()).load(uri).into(taskImageView, new Callback() {
            @Override
            public void onSuccess() {
                binding.imageProgressBar.setVisibility(View.GONE);
                imageLoaded = true;
            }

            @Override
            public void onError() {
                Log.d(TAG, "Error while loading image " + task.getUrl().toString());
                binding.imageProgressBar.setVisibility(View.GONE);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addOptionToGrid(TextView option) {
        option.measure(displayDims.x, displayDims.y);
        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setOrder(-1);
        lp.setFlexGrow(2);
        lp.setMargins(dpToPx(4), dpToPx(2), dpToPx(2), dpToPx(4));
        option.setLayoutParams(lp);
        binding.optionsGrid.addView(option);
    }

    private void setupOptionClickListener() {
        optionClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!v.isEnabled()) return;
                String option = ((TextView) v).getText().toString();
                if (selectedOptions.indexOfKey(v.getId()) >= 0) {
                    v.setBackgroundResource(R.drawable.options_background_normal);
                    selectedOptions.remove(v.getId());
                } else {
                    if (!multiSelection) {
                        for (int i = 0; i < selectedOptions.size(); i++) {
                            binding.optionsGrid.findViewById(selectedOptions.keyAt(i)).setBackgroundResource(R.drawable.options_background_normal);
                        }
                        selectedOptions.clear();
                    }
                    v.setBackgroundResource(R.drawable.options_background_selected);
                    selectedOptions.put(v.getId(), option);
                }
            }
        };
    }

    public TextView getStyledTextView(String text) {
        TextView tv = new TextView(getContext());
        tv.setText(text);
        tv.setTag(text);
        tv.setTextSize(16);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(getResources().getColor(R.color.colorWhite));
        tv.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        tv.setBackgroundResource(R.drawable.options_background_normal);
        tv.setClickable(true);
        tv.setOnClickListener(optionClickListener);
        return tv;
    }

    @Override
    public Answer getAnswer() {
        if (selectedOptions.size() == 0) {
            return null;
        }

        JSONObject rawAnswer = new JSONObject();
        JSONArray selections = new JSONArray();
        for (int i = 0; i < selectedOptions.size(); i++) {
            selections.put(selectedOptions.valueAt(i));
        }

        try {
            rawAnswer.put("image_name", task.getFileName());
            rawAnswer.put("selections", selections);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        answer.setData(rawAnswer.toString());
        return answer;
    }
}