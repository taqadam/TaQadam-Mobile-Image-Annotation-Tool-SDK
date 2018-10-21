package com.recoded.taqadam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class FlagImageDialog extends DialogFragment {
    private FlagsAdapter adapter;

    public static FlagImageDialog newInstance(FlagsAdapter adapter) {
        FlagImageDialog frag = new FlagImageDialog();
        frag.setAdapter(adapter);
        return frag;
    }

    private void setAdapter(FlagsAdapter adapter) {
        this.adapter = adapter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_flag_image, container,
                false);
        ListView listView = view.findViewById(R.id.flags_lv);
        if (adapter != null)
            listView.setAdapter(adapter);
        // get the views and attach the listener

        return view;
    }

    public FlagsAdapter getAdapter() {
        return adapter;
    }
}
