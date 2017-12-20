package com.recoded.taqadam;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by HP PC on 12/17/2017.
 */

public class TaskAdapter extends ArrayAdapter<Task> {
    private Context context;
    private int resource;
    private List<Task> task;


    public TaskAdapter(@NonNull Context context, int resource, @NonNull List<Task> task) {
        super(context, resource, task);
        this.context = context;
        this.resource = resource;
        this.task = task;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.task, parent, false);

        }
        TextView type = (TextView) convertView.findViewById(R.id.task_type);
        TextView title = (TextView) convertView.findViewById(R.id.task_title);
        TextView desc = (TextView) convertView.findViewById(R.id.task_disc);

        Task taskPosition = task.get(position);

        type.setText(taskPosition.getType());
        title.setText(taskPosition.getTitle());
        desc.setText(taskPosition.getDescription());

        return convertView;
    }
}
