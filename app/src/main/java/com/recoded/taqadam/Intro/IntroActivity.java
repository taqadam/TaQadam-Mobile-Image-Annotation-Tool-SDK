package com.recoded.taqadam.Intro;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro;
import com.recoded.taqadam.R;
import com.recoded.taqadam.SlideFragment;

/**
 * Created by Ahmad Siafaddin on 12/12/2017.
 */

public class IntroActivity extends AppIntro {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(SlideFragment.newInstance(R.layout.frag_intro_register, 0));
        addSlide(SlideFragment.newInstance(R.layout.frag_intro_practice, 1));
        addSlide(SlideFragment.newInstance(R.layout.frag_intro_work, 2));
        addSlide(SlideFragment.newInstance(R.layout.frag_intro_payment, 3));
       // addSlide(SlideFragment.newInstance(R.layout.payment, 4));

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

        if (((newFragment) != null ? ((SlideFragment) newFragment).getScreenId() : 0) == 0) {

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorIntroRegister));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorIntroRegister));
            setSeparatorColor(getResources().getColor(R.color.colorIntroRegister));


        }

        else if(((SlideFragment) newFragment).getScreenId() == 1){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorIntroPractice));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorIntroPractice));
            setSeparatorColor(getResources().getColor(R.color.colorIntroPractice));


        }
        else if(((SlideFragment) newFragment).getScreenId() == 2){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorIntroWork));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorIntroWork));
            setSeparatorColor(getResources().getColor(R.color.colorIntroWork));

        }

        else if(((SlideFragment) newFragment).getScreenId() == 3){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorIntroPayment));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorIntroPayment));
            setSeparatorColor(getResources().getColor(R.color.colorIntroPayment));

        }

       /* else if(((SlideMaker) newFragment).getScreenId() == 4){

            setIndicatorColor(getResources().getColor(R.color.colorWhite), getResources().getColor(R.color.colorWhite));
            setNextArrowColor(getResources().getColor(R.color.colorWhite));
            setColorDoneText(getResources().getColor(R.color.colorWhite));
            setBarColor(getResources().getColor(R.color.colorMaron));
            setSeparatorColor(getResources().getColor(R.color.colorMaron));

        }*/

    }

}