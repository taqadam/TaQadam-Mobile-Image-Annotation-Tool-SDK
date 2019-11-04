package com.recoded.taqadam.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.recoded.taqadam.utils.Lang;
import com.recoded.taqadam.R;
import com.recoded.taqadam.utils.Theme;

public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getResources().getString(R.string.action_settings));
        getFragmentManager().beginTransaction()
                .replace(R.id.content, new SettingsFragment())
                .commit();

        this.getSharedPreferences("config", MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("language")) {
            Lang.setLanguage(sharedPreferences.getString(key, ""));
            recreate();
        }
        if (key.equals("theme")) {
            int themeId = Integer.parseInt(sharedPreferences.getString(key, "1"));
            Theme.setTheme(themeId);
            recreate();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
