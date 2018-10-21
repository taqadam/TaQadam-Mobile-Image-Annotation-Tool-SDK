package com.recoded.taqadam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.recoded.taqadam.models.ImageFlag;
import com.recoded.taqadam.models.Label;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FlagsAdapter extends ArrayAdapter<ImageFlag> {
    List<ImageFlag> flags;
    Context context;
    OnFlagClickListener listener;

    public FlagsAdapter(@NonNull Context context, int resource, @NonNull List<ImageFlag> objects) {
        super(context, R.layout.flag_item, objects);
        flags = objects;
        this.context = context;
    }

    @Nullable
    @Override
    public ImageFlag getItem(int position) {
        return flags.get(position);
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
        FlagsAdapter.ViewHolder holder;

        if (null == convertView) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert vi != null;
            convertView = vi.inflate(R.layout.flag_item, parent, false);
            holder = new FlagsAdapter.ViewHolder();
            holder.flagButton = convertView.findViewById(R.id.flag_text);
            convertView.setTag(holder);
        } else {
            holder = (FlagsAdapter.ViewHolder) convertView.getTag();
        }

        if (holder != null) {
            final String flag = flags.get(position).getLabel();
            final boolean isSelected = flags.get(position).isSelected();

            holder.flagButton.setText(flag);
            holder.flagButton.setChecked(isSelected);
            holder.flagButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (listener != null)
                        listener.onFlagClick(flag, isChecked);
                }
            });
        }

        return convertView;
    }

    public void setOnFlagClickListener(OnFlagClickListener listener) {
        this.listener = listener;
    }

    class ViewHolder {
        CheckBox flagButton;
    }

    public void setFlags(List<ImageFlag> flags) {
        this.flags = flags;
        notifyDataSetChanged();
    }

    interface OnFlagClickListener {
        void onFlagClick(String flag, boolean isChecked);
    }
}
