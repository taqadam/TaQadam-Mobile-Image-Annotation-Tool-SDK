package com.recoded.taqadam;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ahmad Siafaddin on 12/26/2017.
 */

public class MainPagerAdapter extends FragmentPagerAdapter {
    private Fragment assignments, qa, wallet, discussion;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
        assignments = new FragmentAssignments();
        qa = new FragmentQA();
        wallet = new FragmentCashOut();
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return assignments;
            case 1:
                return qa;
            case 2:
                return wallet;
            default:
                return assignments;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}