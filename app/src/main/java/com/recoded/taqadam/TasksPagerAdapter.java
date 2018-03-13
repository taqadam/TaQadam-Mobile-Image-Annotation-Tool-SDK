package com.recoded.taqadam;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.recoded.taqadam.models.Image;

import java.util.List;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class TasksPagerAdapter extends FragmentStatePagerAdapter {
    private List<Image> imagesList;
    String type;
    String jobId;
    TaskFragment mCurrentFrag;
    int currentPosition;

    public TasksPagerAdapter(FragmentManager fm, final List<Image> imagesList, String jobType, String jobId) {
        super(fm);
        this.imagesList = imagesList;
        this.type = jobType;
        this.jobId = jobId;
    }


    public void removeImage(int position) {
        imagesList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public TaskFragment getItem(int position) {
        return TaskFragment.newTask(imagesList.get(position), type, jobId);
    }

    @Override
    public int getCount() {
        return imagesList.size();
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