package com.recoded.taqadam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.recoded.taqadam.models.Region;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RegionsAdapter extends ArrayAdapter<Region> {
    private List<Region> regions;
    private Context ctx;

    public RegionsAdapter(@NonNull Context context, int resource, @NonNull List<Region> objects) {
        super(context, resource, objects);
        ctx = context;
        regions = objects;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return regions.size() + 1;
    }

    @Nullable
    @Override
    public Region getItem(int position) {
        if (position == 0) {
            return null;
        } else {
            return regions.get(position - 1);
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RegionsAdapter.ViewHolder holder;

        if (null == convertView) {
            LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert vi != null;
            convertView = vi.inflate(R.layout.regions_list_item, parent, false);
            holder = new RegionsAdapter.ViewHolder();
            holder.text = convertView.findViewById(R.id.region_label);
            holder.icon = convertView.findViewById(R.id.region_shape);
            convertView.setTag(holder);
        } else {
            holder = (RegionsAdapter.ViewHolder) convertView.getTag();
        }
        if (null != holder) {
            if(position == 0) {
                holder.text.setText("Select Region");
                holder.icon.setImageResource(R.drawable.ic_polygon_new);
            } else {
                holder.text.setText(regions.get(position-1).getTitle());
                holder.icon.setImageResource(regions.get(position-1).getIcon());
            }
        }
        return convertView;
    }

    private class ViewHolder {
        TextView text;
        ImageView icon;
    }
}
