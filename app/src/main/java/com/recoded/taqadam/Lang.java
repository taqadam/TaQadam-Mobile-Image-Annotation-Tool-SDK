package com.recoded.taqadam;

import java.util.Locale;

/**
 * Created by HP PC on 1/27/2018.
 */

class Lang {

    static Locale locale;

    static void setLanguage(String language) {
        if (language.equals("")) {
            locale = Locale.getDefault();
        } else {
            locale = new Locale(language);
        }
    }
}
