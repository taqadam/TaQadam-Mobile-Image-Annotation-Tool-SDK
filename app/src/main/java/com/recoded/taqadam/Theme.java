package com.recoded.taqadam;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by HP PC on 1/27/2018.
 */

public class Theme {

    public static int theme;
    public final static int THEME_LIGHT = 1;
    public final static int THEME_DARK = 2;

    public static void changeToTheme(Activity activity, int sTheme) {
        theme = sTheme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static void setTheme(int themeId) {
        switch (themeId) {
            default:
            case THEME_LIGHT:
                theme = R.style.LightCustom;
                break;
            case THEME_DARK:
                theme = R.style.DarkCustom;
                break;
        }
    }
}
