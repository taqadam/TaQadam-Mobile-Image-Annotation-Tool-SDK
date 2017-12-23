package com.recoded.taqadam;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;

/**
 * Created by Ahmad Siafaddin on 12/12/2017.
 */

public class SlideFragment extends Fragment implements ISlideBackgroundColorHolder {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;
    private int screenId;

    public static SlideFragment newInstance(int layoutResId, int id) {
        SlideFragment sampleSlide = new SlideFragment();
        sampleSlide.setScreenId(id);
        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layoutResId, container, false);
    }

    public int getScreenId() {
        return this.screenId;
    }

    public void setScreenId(int screenId) {
        this.screenId = screenId;
    }

    //Todo-wisam: implement color transition
    @Override
    public int getDefaultBackgroundColor() {
        switch (screenId) {
            case 0:
                return getContext().getResources().getColor(R.color.colorIntroRegister);
            case 1:
                return getContext().getResources().getColor(R.color.colorIntroPractice);
            case 2:
                return getContext().getResources().getColor(R.color.colorIntroWork);
            default:
                return getContext().getResources().getColor(R.color.colorIntroPayment);
        }
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        getView().setBackgroundColor(backgroundColor);
    }
}
