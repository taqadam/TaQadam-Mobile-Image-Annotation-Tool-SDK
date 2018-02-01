package com.recoded.taqadam;

import android.app.Activity;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Created by HP PC on 1/27/2018.
 */

public class Lang {

    public static String language;

    public static void changeLang(Activity activity, String lang) {

        Configuration config = activity.getBaseContext().getResources().getConfiguration();

        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            Locale locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            activity.getBaseContext().getResources().updateConfiguration(config, activity.getBaseContext().getResources().getDisplayMetrics());

        }
    }

}
