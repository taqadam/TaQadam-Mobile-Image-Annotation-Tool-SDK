package com.recoded.taqadam.adapters;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.recoded.taqadam.fragments.TaskFragment;
import com.recoded.taqadam.objects.Assignment;
import com.recoded.taqadam.models.Service;
import com.recoded.taqadam.objects.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class TasksPagerAdapter extends FragmentStatePagerAdapter {
    private List<Task> tasks;
    Service.Services type;
    Assignment assignment;
    public TaskFragment mCurrentFrag;
    public int currentPosition;

    public TasksPagerAdapter(FragmentManager fm, final List<Task> tasks, Service.Services serviceType, Assignment assignment) {
        super(fm);
        this.tasks = new ArrayList<>();
        this.tasks.addAll(tasks);
        this.type = serviceType;
        this.assignment = assignment;
    }

    public void addNewTasks(List<Task> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
        notifyDataSetChanged();
    }

    @Override
    public TaskFragment getItem(int position) {
        return TaskFragment.newTask(tasks.get(position), type, assignment);
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        currentPosition = position;
        mCurrentFrag = (TaskFragment) object;
    }

    public TaskFragment getCurrentFragment() {
        return mCurrentFrag;
    }
}