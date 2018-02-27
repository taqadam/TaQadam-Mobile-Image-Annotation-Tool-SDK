package com.recoded.taqadam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Image;
import com.recoded.taqadam.models.Job;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class TaskFragment extends Fragment {
    private static final String TAG = TaskFragment.class.getSimpleName();

    protected Image mImage;
    protected ImageViewTouch taskImageView;
    protected String jobId;
    protected Answer answer;
    protected boolean imageLoaded = false;

    public static TaskFragment newTask(Image img, String type, String jobId) {
        if (type.equals(Job.BBOX)) {
            TaskFragment frag = new BoundingBoxFragment();
            frag.setImage(img);
            frag.setJobId(jobId);
            return frag;
        } else {
            TaskFragment frag = new CategorizationFragment();
            frag.setImage(img);
            return frag;
        }
    }

    public TaskFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable("image", mImage);
        outState.putString("job_id", jobId);
        super.onSaveInstanceState(outState);
    }

    public void setImage(Image img) {
        mImage = img;
    }

    protected int pxToDp(int px) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    protected int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public Answer getAnswer() {
        return null;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}