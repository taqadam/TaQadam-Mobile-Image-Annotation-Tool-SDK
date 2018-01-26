package com.recoded.taqadam;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.recoded.taqadam.models.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class TasksPagerAdapter extends FragmentPagerAdapter {
    private List<Task> tasksList;

    public TasksPagerAdapter(FragmentManager fm) {
        super(fm);
        tasksList = new ArrayList<>();
    }

    public void addNewTasks(Task... t) {
        tasksList.addAll(Arrays.asList(t));
        notifyDataSetChanged();
    }


    @Override
    public TaskFragment getItem(int position) {
        Task t = tasksList.get(position);
        return TaskFragment.newTask(t.getTaskId());
    }

    @Override
    public int getCount() {
        return tasksList.size();
    }
}