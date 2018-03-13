package com.recoded.taqadam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
        showSkipButton(false);
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("firstRun", false);
        editor.apply();
        startActivity(new Intent(this, SigninActivity.class));
        finish();
    }
}