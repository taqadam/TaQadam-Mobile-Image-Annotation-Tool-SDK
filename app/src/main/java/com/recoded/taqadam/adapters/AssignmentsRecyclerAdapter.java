package com.recoded.taqadam.adapters;

import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recoded.taqadam.R;
import com.recoded.taqadam.activities.WorkActivity;
import com.recoded.taqadam.databinding.AssignmentItemBinding;
import com.recoded.taqadam.models.Assignment;

import java.util.ArrayList;
import java.util.List;

public class AssignmentsRecyclerAdapter extends RecyclerView.Adapter<AssignmentsRecyclerAdapter.ViewHolder> {
    private Context ctx;
    private List<Assignment> dataset;

    public AssignmentsRecyclerAdapter(Context c) {
        this.ctx = c;
        this.dataset = new ArrayList<>();
    }

    public void setDataset(List<Assignment> dataset) {
        this.dataset = dataset;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.assignment_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Assignment assignment = dataset.get(position);
        holder.binding.setAssignment(assignment);
        holder.binding.imagesCount.setText(assignment.getJob().getDataset().getSize().toString());
        holder.binding.jobReward.setImageResource(assignment.getJob().getService().getTypeOfService().getDrawable());
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ctx, WorkActivity.class);
                intent.putExtra("assignment", assignment);
                ctx.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        AssignmentItemBinding binding;

        ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

    }
}