package com.recoded.taqadam.Intro;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.recoded.taqadam.R;

/**
 * Created by Ahmad Siafaddin on 12/12/2017.
 */

public class IntroActivity extends AppIntro {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(SlideMaker.newInstance(R.layout.intro_logo, 0));
        addSlide(SlideMaker.newInstance(R.layout.registeration, 1));
        addSlide(SlideMaker.newInstance(R.layout.practice, 2));
        addSlide(SlideMaker.newInstance(R.layout.work, 3));
        addSlide(SlideMaker.newInstance(R.layout.payment, 4));

        showSkipButton(false);
        setFlowAnimation();

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();

    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

        if (((newFragment) != null ? ((SlideMaker) newFragment).getScreenId() : 0) == 0) {

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorMaron));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorMaron));
            setSeparatorColor(getResources().getColor(R.color.colorMaron));


        }

        else if(((SlideMaker) newFragment).getScreenId() == 1){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorSlide2));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorSlide2));
            setSeparatorColor(getResources().getColor(R.color.colorSlide2));


        }
        else if(((SlideMaker) newFragment).getScreenId() == 2){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorSlide3));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorSlide3));
            setSeparatorColor(getResources().getColor(R.color.colorSlide3));

        }

        else if(((SlideMaker) newFragment).getScreenId() == 3){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorSlide4));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorSlide4));
            setSeparatorColor(getResources().getColor(R.color.colorSlide4));

        }

        else if(((SlideMaker) newFragment).getScreenId() == 4){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorWhite));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorMaron));
            setSeparatorColor(getResources().getColor(R.color.colorMaron));

        }

    }

}