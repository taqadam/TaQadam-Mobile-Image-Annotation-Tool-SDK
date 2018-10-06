package com.recoded.taqadam;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.models.Assignment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAssignments extends Fragment {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private AssignmentsRecyclerAdapter mAdapter;

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
        fetchAssignments();
        return view;
    }

    public void fetchAssignments() {
        Call<List<Assignment>> call = Api.getInstance().endpoints.getAssignments();
        call.enqueue(new Callback<List<Assignment>>() {
            @Override
            public void onResponse(Call<List<Assignment>> call, Response<List<Assignment>> response) {
                mAdapter.setDataset(response.body());
                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Assignment>> call, Throwable t) {

            }
        });
    }
}
