package com.recoded.taqadam.utils;

import java.util.Locale;

/**
 * Created by HP PC on 1/27/2018.
 */

public class Lang {

    public static Locale locale;

    public static void setLanguage(String language) {
        if (language.equals("")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(language);
        }
    }
}
