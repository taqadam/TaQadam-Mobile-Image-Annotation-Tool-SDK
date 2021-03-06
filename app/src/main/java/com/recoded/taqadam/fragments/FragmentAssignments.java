package com.recoded.taqadam.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.recoded.taqadam.adapters.AssignmentsRecyclerAdapter;
import com.recoded.taqadam.R;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.objects.Assignment;

import java.io.Serializable;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAssignments extends Fragment {
    private String TAG = FragmentAssignments.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private AssignmentsRecyclerAdapter mAdapter;
    private List<Assignment> assignments;

    public FragmentAssignments() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_jobs, container, false);

        mRecyclerView = view.findViewById(R.id.jobs_recycler_view);
        mProgressBar = view.findViewById(R.id.progress_bar);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new AssignmentsRecyclerAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        if(savedInstanceState != null && savedInstanceState.containsKey("assignments")){
            assignments = (List<Assignment>) savedInstanceState.getSerializable("assignments");
        }
        return view;
    }

    @Override
    public void onResume() {
        mProgressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "onResume");
        fetchAssignments();
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("assignments", (Serializable) assignments);
        outState.putInt("scroll", mRecyclerView.getScrollY());
    }

    public void fetchAssignments() {
        Call<List<Assignment>> call = Api.getInstance().endpoints.getAssignments();
        call.enqueue(new Callback<List<Assignment>>() {
            @Override
            public void onResponse(Call<List<Assignment>> call, Response<List<Assignment>> response) {
                assignments = response.body();
                mAdapter.setDataset(assignments);
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Assignment>> call, Throwable t) {

            }
        });
    }
}
