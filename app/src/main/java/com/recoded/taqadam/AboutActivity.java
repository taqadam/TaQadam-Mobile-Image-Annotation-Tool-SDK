package com.recoded.taqadam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

public class AboutActivity extends AppCompatActivity implements AboutFragment.OnButtonClickListener {

    LockableViewPager viewpager;
    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.about));

        viewpager = findViewById(R.id.view_pager);
        viewpager.setLocked(true);

        viewpager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    AboutFragment f = new AboutFragment();
                    f.setButtonsListener(AboutActivity.this);
                    return f;
                } else
                    return new WebViewerFragment();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onButtonClicked(String url) {
        viewpager.setCurrentItem(1, true);
        loadPage(url);
    }

    private void loadPage(String url) {
        String tag = "android:switcher:" + viewpager.getId() + ":" + 1;
        WebViewerFragment frag = (WebViewerFragment) getSupportFragmentManager().findFragmentByTag(tag);
        frag.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (viewpager.getCurrentItem() == 1) {
            viewpager.setCurrentItem(0, true);
            loadPage("about:blank");
            return;
        }
        super.onBackPressed();
        finish();
    }
}
