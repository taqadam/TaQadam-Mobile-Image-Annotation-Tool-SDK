package com.recoded.taqadam;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recoded.taqadam.databinding.JobItemBinding;
import com.recoded.taqadam.models.Job;
import com.recoded.taqadam.models.db.JobDbHandler;

import java.util.List;

public class JobsRecyclerAdapter extends RecyclerView.Adapter<JobsRecyclerAdapter.ViewHolder> {
    private Context ctx;
    private List<Job> dataSet;

    public JobsRecyclerAdapter(Context c) {
        this.ctx = c;
        this.dataSet = JobDbHandler.getInstance().getRecentJobs();
        JobDbHandler.getInstance().setOnJobsChangedListener(new JobDbHandler.OnJobsChangedListener() {
            @Override
            public void onJobsChanged(List<Job> jobs) {
                dataSet = jobs;
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.job_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Job job = dataSet.get(position);
        holder.binding.setJob(job);
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, JobActivity.class);
                intent.putExtra("job_id", job.getJobId());
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        JobItemBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

    }


}