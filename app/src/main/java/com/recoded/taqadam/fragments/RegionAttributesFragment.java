package com.recoded.taqadam.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;

import com.recoded.taqadam.R;
import com.recoded.taqadam.databinding.FragRegionAttributesBinding;
import com.recoded.taqadam.models.Region;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

/**
 * Created by wisam on Jan 20 18.
 */

public class RegionAttributesFragment extends DialogFragment {

    private Region region;
    private FragRegionAttributesBinding binding;


    public static RegionAttributesFragment getInstance(Region region) {
        RegionAttributesFragment frag = new RegionAttributesFragment();
        frag.setRegion(region);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_region_attributes, container, false);
        binding = DataBindingUtil.bind(rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
        } else {
            binding.fragHeader.setVisibility(View.GONE);
            getDialog().setTitle(R.string.region_attributes);
            binding.buttonAddExtra.setVisibility(View.VISIBLE);
        }
        populateTable();
        createClickListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
        getDialog().getWindow().setLayout(width, height);
        super.onResume();
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    private void createClickListeners() {
        //Ok
        binding.buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //For the label
                region.getRegionAttributes().clear();

                //other attributes
                for (int i = 0; i < binding.tableAttributes.getChildCount(); i++) {
                    TableRow row = (TableRow) binding.tableAttributes.getChildAt(i);
                    String key = ((EditText) row.getChildAt(0)).getText().toString().trim();
                    String val = ((EditText) row.getChildAt(1)).getText().toString().trim();

                    if (!key.isEmpty() && !val.isEmpty() && (!key.equals("label"))) {
                        region.addRegionAttribute(key, val);
                    }

                }

                RegionAttributesFragment.this.dismiss();
            }
        });

        //Cancel
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegionAttributesFragment.this.dismiss();
            }
        });

        //Add
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow row = getRow("", "");
                binding.tableAttributes.addView(row);
                row.getChildAt(0).requestFocusFromTouch();
            }
        };
        binding.buttonAdd.setOnClickListener(listener);
        binding.buttonAddExtra.setOnClickListener(listener);
    }

    private void populateTable() {
        if(region == null) return;
        String keys[] = region.getRegionAttributes().keySet().toArray(new String[0]);
        String values[] = region.getRegionAttributes().values().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            binding.tableAttributes.addView(getRow(keys[i], values[i]));
        }
    }

    private TableRow getRow(String key, String val) {
        LayoutInflater inflater = getLayoutInflater();
        TableRow row = (TableRow) inflater.inflate(
                R.layout.attrib_table_row,
                binding.tableAttributes,
                false);
        //To not loose data when activity lose focus
        int i = binding.tableAttributes.getChildCount();
        row.getChildAt(0).setId(i * 2);
        row.getChildAt(1).setId(i * 2 + 1);

        ((EditText) row.getChildAt(0)).setText(key);
        ((EditText) row.getChildAt(1)).setText(val);
        row.getChildAt(2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewGroup) v.getParent().getParent()).removeView((View) v.getParent());
            }
        });

        return row;
    }
}

