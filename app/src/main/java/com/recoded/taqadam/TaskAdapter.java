package com.recoded.taqadam;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.recoded.taqadam.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {
    private Context ctx;
    private List<Task> dataSet;

    public TaskAdapter(Context c, List<Task> list) {
        this.ctx = c;
        this.dataSet = list;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.task_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = dataSet.get(position);
        holder.taskType.setText(task.getType());
        holder.taskDesc.setText(task.getDescription());
        holder.taskTitle.setText(task.getTitle());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskType, taskTitle, taskDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            taskType = itemView.findViewById(R.id.task_type);
            taskTitle = itemView.findViewById(R.id.task_title);
            taskDesc = itemView.findViewById(R.id.task_disc);
        }

    }


}