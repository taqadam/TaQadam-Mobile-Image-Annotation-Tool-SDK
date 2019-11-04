package com.recoded.taqadam.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.DisplayMetrics;

import com.recoded.taqadam.models.Answer;
import com.recoded.taqadam.objects.Assignment;
import com.recoded.taqadam.models.Service;
import com.recoded.taqadam.objects.Task;

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
    public boolean imageLoaded = false;

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("assignment", assignment);
        outState.putSerializable("task", task);
        outState.putString("type", type.name());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("assignment")) {
            this.assignment = (Assignment) savedInstanceState.getSerializable("assignment");
            this.task = (Task) savedInstanceState.getSerializable("task");
            String type = savedInstanceState.getString("type");
            this.type = Service.Services.valueOf(type);
        }
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