package com.recoded.taqadam.utils;

import com.recoded.taqadam.R;

/**
 * Created by HP PC on 1/27/2018.
 */

public class Theme {

    public static int theme;
    private final static int THEME_LIGHT = 1;
    private final static int THEME_DARK = 2;

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
