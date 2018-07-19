package com.recoded.taqadam;

import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;

import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.models.Assignment;
import com.recoded.taqadam.models.Service;
import com.recoded.taqadam.models.Task;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class TaskFragment extends Fragment {
    private static final String TAG = TaskFragment.class.getSimpleName();

    protected Task task;
    protected ImageViewTouch taskImageView;
    protected Assignment assignment;
    protected Answer answer;
    protected Service.Services type;
    protected boolean imageLoaded = false;

    public static TaskFragment newTask(Task task, Service.Services type, Assignment assignment) {
        if (type == Service.Services.BBOX) {
            TaskFragment frag = new BoundingBoxFragment();
            frag.setTask(task);
            frag.setAssignment(assignment);
            frag.type = type;
            return frag;
        } else if (type == Service.Services.CLASSIFICATION || type == Service.Services.CATEGORIZATION) {
            TaskFragment frag = new CategorizationFragment();
            frag.setTask(task);
            frag.setAssignment(assignment);
            frag.type = type;
            return frag;
        } else if (type == Service.Services.SEGMENTATION) {
            TaskFragment frag = new SegmentationFragment();
            frag.setTask(task);
            frag.setAssignment(assignment);
            frag.type = type;
            return frag;
        } else {
            return null;
        }
    }

    public TaskFragment() {
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
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
}