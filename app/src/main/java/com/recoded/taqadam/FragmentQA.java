package com.recoded.taqadam;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class FragmentQA extends WebViewerFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        webView.loadData("<h3><b>Help & FAQ</b></h3>", "text/html", "utf-8");
        //@todo link to better website this is causing high memory consumption
        //loadUrl("http://www.taqadam.io/work");
    }
}
