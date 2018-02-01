package com.recoded.taqadam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentJobs extends Fragment {

    private RecyclerView mRecyclerView;
    private JobsRecyclerAdapter mAdapter;

    public FragmentJobs() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_jobs, container, false);

        mRecyclerView = view.findViewById(R.id.jobs_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new JobsRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        return view;
    }
}
