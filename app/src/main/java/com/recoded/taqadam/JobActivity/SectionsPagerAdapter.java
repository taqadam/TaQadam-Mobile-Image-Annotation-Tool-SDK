package com.recoded.taqadam.JobActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.Random;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    Random FragRandomNom = new Random();
    int position;


    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }



    @Override
    public Fragment getItem(int position) {
        /**
         * THIS.POSITION IS THE RANDOM NUMBER OF
         * TASKS AFTER  CONNECTING WITH FIREBASE.
         */
        this.position = position;
        int i = 0;
        if (position != i) {
            for (i = 0; i <= getCount(); i++) {
                position =FragRandomNom.nextInt(7);
            }
        }
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return PlaceholderFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        // Show 9 total pages.
        return 9;
    }
}