package com.recoded.taqadam;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by Ahmad Siafaddin on 12/12/2017.
 */

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setColorTransitionsEnabled(true);
        setFinishOnTouchOutside(false);
        setWizardMode(true);
        addSlide(IntroSlideFragment.newInstance(R.layout.frag_intro_register, 0));
        addSlide(IntroSlideFragment.newInstance(R.layout.frag_intro_practice, 1));
        addSlide(IntroSlideFragment.newInstance(R.layout.frag_intro_work, 2));
        addSlide(IntroSlideFragment.newInstance(R.layout.frag_intro_payment, 3));
        //setFlowAnimation();
        ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0).setBackgroundColor(getResources().getColor(R.color.colorWhite));
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        startActivity(new Intent(this, RegisterActivity.class));
        finish();
    }


    /*
    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);

        if(newFragment != null) {
            if (((IntroSlideFragment) newFragment).getScreenId() == 0) {
                setBarColor(getResources().getColor(R.color.colorIntroRegister));
                setSeparatorColor(getResources().getColor(R.color.colorIntroRegister));


            } else if (((IntroSlideFragment) newFragment).getScreenId() == 1) {
                setBarColor(getResources().getColor(R.color.colorIntroPractice));
                setSeparatorColor(getResources().getColor(R.color.colorIntroPractice));

            } else if (((IntroSlideFragment) newFragment).getScreenId() == 2) {
                setBarColor(getResources().getColor(R.color.colorIntroWork));
                setSeparatorColor(getResources().getColor(R.color.colorIntroWork));

            } else if (((IntroSlideFragment) newFragment).getScreenId() == 3) {
                setBarColor(getResources().getColor(R.color.colorIntroPayment));
                setSeparatorColor(getResources().getColor(R.color.colorIntroPayment));
            }
        }
    }
*/
}