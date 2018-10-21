package com.recoded.taqadam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.recoded.taqadam.models.Label;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LabelsAdapter extends ArrayAdapter<Label> {
    List<Label> labels;
    String selectedLabel = "";
    Context context;
    OnLabelClickLister listener;
    boolean isEnabled = false;

    public LabelsAdapter(@NonNull Context context, int resource, @NonNull List<Label> objects) {
        super(context, R.layout.label_list_item, objects);
        labels = objects;
        this.context = context;
    }

    @Nullable
    @Override
    public Label getItem(int position) {
        return labels.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LabelsAdapter.ViewHolder holder;
        final int finalPos = position;

        if (null == convertView) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert vi != null;
            convertView = vi.inflate(R.layout.label_list_item, parent, false);
            holder = new LabelsAdapter.ViewHolder();
            holder.label = convertView.findViewById(R.id.label_text);
            holder.isSelected = convertView.findViewById(R.id.label_selection_chkbox);
            holder.isSelected.setClickable(false);
            holder.hasChildren = convertView.findViewById(R.id.label_has_children_indicator);
            convertView.setTag(holder);
        } else {
            holder = (LabelsAdapter.ViewHolder) convertView.getTag();
        }

        if(holder != null) {
            Label l = labels.get(position);
            holder.label.setText(l.getLabel());
            if(l.hasChildren()) {
                holder.isSelected.setVisibility(View.INVISIBLE);
                holder.hasChildren.setVisibility(View.VISIBLE);
            } else {
                holder.isSelected.setVisibility(View.VISIBLE);
                holder.hasChildren.setVisibility(View.INVISIBLE);
                if(selectedLabel.equalsIgnoreCase(l.getLabel())) {
                    holder.isSelected.setChecked(true);
                } else {
                    holder.isSelected.setChecked(false);
                }
            }
        }


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isEnabled) return;
                if(listener != null) listener.onLabelClick(labels.get(finalPos), finalPos);
                Label l = labels.get(finalPos);
                selectedLabel = l.getLabel();
                if(v.getTag() != null) {
                    ((ViewHolder) v.getTag()).isSelected.setChecked(true);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    public void setOnLabelClickLitener(OnLabelClickLister listener) {
        this.listener = listener;
    }

    public void setSelectedLabel(String selectedLabel) {
        this.selectedLabel = selectedLabel;
        notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    class ViewHolder {
        TextView label;
        RadioButton isSelected;
        ImageView hasChildren;
    }


    interface OnLabelClickLister {
        void onLabelClick(Label label, int position);
    }
}
