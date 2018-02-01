package com.recoded.taqadam;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    private int inflatedTheme;
    private String loadedLang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflatedTheme = Theme.theme;
        loadedLang = Lang.language;
        setTheme(inflatedTheme);
        setLanguage();
    }

    private void setLanguage() {
        if (loadedLang.equals("")) return;

        Configuration config = new Configuration(getResources().getConfiguration());
        if (!config.locale.getLanguage().equals(loadedLang)) {
            config.locale = new Locale(loadedLang);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        }
    }

    @Override
    protected void onResume() {
        if (inflatedTheme != Theme.theme || !loadedLang.equals(Lang.language)) {
            recreate();
        }
        super.onResume();
    }
}
