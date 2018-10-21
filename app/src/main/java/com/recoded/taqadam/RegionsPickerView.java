package com.recoded.taqadam;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ramotion.directselect.DSAbstractPickerBox;
import com.recoded.taqadam.models.Region;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RegionsPickerView extends DSAbstractPickerBox<Region> {
    private TextView text;
    private ImageView icon;
    private View cellRoot;

    public RegionsPickerView(@NonNull Context context) {
        this(context, null);
    }

    public RegionsPickerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RegionsPickerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert mInflater != null;
        mInflater.inflate(R.layout.region_pickerbox, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.text = findViewById(R.id.region_label);
        this.icon = findViewById(R.id.region_shape);
        this.cellRoot = findViewById(R.id.region_cell_root);
    }

    @Override
    public void onSelect(Region selectedItem, int selectedIndex) {
        if(selectedItem != null) {
            this.text.setText(selectedItem.getTitle());
            this.icon.setImageResource(selectedItem.getIcon());
        }
        else {
            this.text.setText("Select Region");
            this.icon.setImageResource(R.drawable.ic_polygon_new);
        }
    }

    @Override
    public View getCellRoot() {
        return this.cellRoot;
    }
}