package com.recoded.taqadam;

import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.recoded.taqadam.databinding.FragCategorizationBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.db.TaskDbHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private boolean multiSelection = true;
    private Point displayDims;
    private boolean hasPendingAnswer = false;

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

        taskImageView = binding.ivTaskImage;
        taskImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_IF_BIGGER);
        Picasso.with(getContext()).load(mTask.getTaskImage()).into(taskImageView, new Callback() {
            @Override
            public void onSuccess() {
                binding.imageProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError() {
                Log.d(TAG, "Error while loading image " + mTask.getTaskImage().toString());
                binding.imageProgressBar.setVisibility(View.GONE);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });

        binding.tvInstruction.setText(mTask.getDescription());
        List<String> options = mTask.getOptions();
        for (int i = 0; i < options.size(); i++) {
            TextView option = getStyledTextView(options.get(i));
            option.setId(i);
            addOptionToGrid(option);
        }

        if (hasPendingAnswer) {
            notifyFragmentForAnswer();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        getAnswer();
        TaskDbHandler.getInstance().attemptTask(mTask.getTaskId());
        super.onDestroy();
    }

    private void addOptionToGrid(TextView option) {
        option.measure(displayDims.x, displayDims.y);
        int gridTotalWidth = binding.optionsGrid.getWidth();
        int gridTotalHeight = binding.optionsGrid.getHeight();

        //Do better Aligning
        binding.optionsGrid.addView(option, option.getMeasuredWidth(), option.getMeasuredHeight());
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
        tv.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
        tv.setBackgroundResource(R.drawable.options_background_normal);
        tv.setClickable(true);
        tv.setOnClickListener(optionClickListener);
        return tv;
    }

    @Override
    public Answer getAnswer() {
        if (selectedOptions.size() == 0) {
            mTask.answer.setRawAnswerData(null);
            return null;
        }

        if (mTask.answer.isCompleted()) {
            return mTask.answer;
        }

        JSONObject rawAnswer = new JSONObject();
        JSONArray categories = new JSONArray();
        for (int i = 0; i < selectedOptions.size(); i++) {
            categories.put(selectedOptions.valueAt(i));
        }

        try {
            rawAnswer.put("categories", categories);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTask.answer.setRawAnswerData(rawAnswer.toString());
        return mTask.answer;
    }

    @Override
    protected void notifyFragmentForAnswer() {
        if (binding == null) {
            hasPendingAnswer = true;
            return;
        }
        if (mTask.answer.isCompleted()) {
            binding.completedTaskOverlay.setVisibility(View.VISIBLE);
            for (int i = 0; i < binding.optionsGrid.getChildCount(); i++) {
                binding.optionsGrid.getChildAt(i).setEnabled(false);
            }
        }

        String rawData = mTask.answer.getRawAnswerData();
        if (rawData == null || rawData.isEmpty()) return;

        for (int i = 0; i < selectedOptions.size(); i++) {
            binding.optionsGrid.findViewById(selectedOptions.keyAt(i)).setBackgroundResource(R.drawable.options_background_normal);
        }
        selectedOptions.clear();

        try {
            JSONObject rawAnswer = new JSONObject(mTask.answer.getRawAnswerData());
            JSONArray categories = rawAnswer.getJSONArray("categories");
            for (int i = 0; i < categories.length(); i++) {
                String cat = categories.getString(i);
                View v = binding.optionsGrid.findViewWithTag(cat);
                if (v != null) {
                    selectedOptions.put(v.getId(), cat);
                    v.setBackgroundResource(R.drawable.options_background_selected);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}