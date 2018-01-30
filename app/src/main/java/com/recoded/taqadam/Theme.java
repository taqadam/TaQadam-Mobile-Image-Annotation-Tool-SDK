package com.recoded.taqadam;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by HP PC on 1/27/2018.
 */

public class Theme {

    private static int cTheme;
    public final static int light = 1;

    public final static int dark = 2;


    public static void changeToTheme(Activity activity, int theme)

    {

        cTheme = theme;

        activity.finish();


        activity.startActivity(new Intent(activity, activity.getClass()));


    }

    public static void onActivityCreateSetTheme(Activity activity, int theme)

    {

        switch (theme)

        {

            default:

            case light:

                activity.setTheme(R.style.LightCustom);

                break;

            case dark:

                activity.setTheme(R.style.DarkCustom);

                break;

        }

    }
}
