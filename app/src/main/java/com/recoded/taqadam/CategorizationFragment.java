package com.recoded.taqadam;

import android.databinding.DataBindingUtil;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.recoded.taqadam.databinding.FragCategorizationBinding;
import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.db.JobDbHandler;
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

        if (savedInstanceState != null) {
            if (mImage == null && savedInstanceState.containsKey("image")) {
                mImage = savedInstanceState.getParcelable("image");
            }
            if (jobId == null && savedInstanceState.containsKey("job_id")) {
                jobId = savedInstanceState.getString("job_id");
            }
        }
        answer = new Answer(jobId, mImage.id);
        taskImageView = binding.ivTaskImage;
        taskImageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
        if (mImage.path.getScheme().equalsIgnoreCase("http") || mImage.path.getScheme().equalsIgnoreCase("https")) {
            loadTaskImage(mImage.path);
        } else if (mImage.path.getScheme().equalsIgnoreCase("gs")) {
            int indexOfSlash = mImage.path.toString().indexOf('/', 5);
            String bucket = mImage.path.toString().substring(0, indexOfSlash);
            String ref = mImage.path.toString().substring(indexOfSlash + 1);
            FirebaseStorage.getInstance(bucket).getReference(ref).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    loadTaskImage(uri);
                }
            });
        }

        binding.tvInstruction.setVisibility(View.GONE);
        List<String> options = JobDbHandler.getInstance().getJob(jobId).getOptions();
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
                Log.d(TAG, "Error while loading image " + mImage.path.toString());
                binding.imageProgressBar.setVisibility(View.GONE);
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
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
        JSONArray categories = new JSONArray();
        for (int i = 0; i < selectedOptions.size(); i++) {
            categories.put(selectedOptions.valueAt(i));
        }

        try {
            rawAnswer.put("categories", categories);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        answer.setRawAnswerData(rawAnswer.toString());
        return answer;
    }
}