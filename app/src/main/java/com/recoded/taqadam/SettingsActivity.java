package com.recoded.taqadam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Locale;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    String lang;
    String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        theme = sharedPreferences.getString("theme", "1");
        lang = sharedPreferences.getString("language", "");
        Theme.onActivityCreateSetTheme(this, Integer.parseInt(theme));
        addPreferencesFromResource(R.xml.preferences);
        Lang.changeLang(this, lang);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("language")) {
            String defLanguage = Locale.getDefault().getDisplayLanguage();
            Lang.changeLang(this, sharedPreferences.getString(key, defLanguage));
            recreate();
        }
        if (key.equals("theme")) {
            String theme = sharedPreferences.getString(key, "1");
            Theme.changeToTheme(this, Integer.parseInt(theme));

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
