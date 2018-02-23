package com.recoded.taqadam;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.recoded.taqadam.models.Task;
import com.recoded.taqadam.models.db.TaskDbHandler;

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
        TaskDbHandler.getInstance().setImpressionsListener(new TaskDbHandler.OnImpressionsReachedListener() {
            @Override
            public void onImpressionsReached(String taskId) {
                for (Task t : tasksList) {
                    if (t.getTaskId().equals(taskId)) {
                        tasksList.remove(t);
                        notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public void addNewTasks(Task... t) {
        tasksList.addAll(Arrays.asList(t));
        notifyDataSetChanged();
    }

    public void removeTask(int position) {
        tasksList.remove(position);
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