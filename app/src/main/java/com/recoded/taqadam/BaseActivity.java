package com.recoded.taqadam;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    private int inflatedTheme;
    Locale locale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        inflatedTheme = Theme.theme;
        locale = Lang.locale;
        setTheme(inflatedTheme);
        if (locale != null)
            setLanguage();
        super.onCreate(savedInstanceState);
    }

    private void setLanguage() {
        Configuration config = new Configuration(getResources().getConfiguration());
        config.locale = locale;
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
        if (locale.getLanguage().contains("ar")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }
    }

    @Override
    protected void onResume() {
        if (inflatedTheme != Theme.theme || (Lang.locale != null && !locale.equals(Lang.locale))) {
            recreate();
        }
        super.onResume();
    }

    public String getTimestamp(long ts) {
        Date now = new Date();
        Date postDate = new Date(ts);
        long diff = now.getTime() / 1000 - ts / 1000;
        if (diff < 60) {
            return getString(R.string.moment_ago);
        } else if (diff < 3600) {
            long minutes = diff / 60;
            return minutes == 1 ? getString(R.string.minute_ago) : String.format(getString(R.string.minuts_ago), minutes);
        } else if (diff < 86400) {
            long hours = diff / 3600;
            return hours == 1 ? getString(R.string.hour_ago) : String.format(getString(R.string.hours_ago), hours);
        } else if (diff < 172800) {
            return String.format(getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm aa", locale).format(postDate));
        } else {
            return new SimpleDateFormat("dd/M/yyyy", locale).format(postDate);
        }
    }
}
