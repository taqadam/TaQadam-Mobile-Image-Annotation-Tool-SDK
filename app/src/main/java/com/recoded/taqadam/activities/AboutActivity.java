package com.recoded.taqadam.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

import com.recoded.taqadam.fragments.AboutFragment;
import com.recoded.taqadam.views.LockableViewPager;
import com.recoded.taqadam.R;
import com.recoded.taqadam.fragments.WebViewerFragment;

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
