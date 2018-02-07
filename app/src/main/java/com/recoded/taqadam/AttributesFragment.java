package com.recoded.taqadam;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.recoded.taqadam.databinding.FragAttributesBinding;
import com.recoded.taqadam.models.Region;

import java.util.List;

/**
 * Created by wisam on Jan 20 18.
 */

public class AttributesFragment extends DialogFragment {

    private Region region;
    private List<String> options;
    private LabelChangeListener listener;
    private FragAttributesBinding binding;
    private boolean noLabel;


    public static AttributesFragment getInstance(Region region, List<String> options) {
        AttributesFragment frag = new AttributesFragment();
        frag.setRegion(region);
        frag.setOptions(options);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_attributes, container, false);
        binding = DataBindingUtil.bind(rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
        } else {
            binding.fragHeader.setVisibility(View.GONE);
            getDialog().setTitle(R.string.region_attributes);
        }
        if (options.size() != 0) {
            initSpinner();
        } else {
            noLabel = true;
        }
        populateTable();
        createClickListeners();

        return rootView;
    }

    @Override
    public void onResume() {
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.70);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.70);
        getDialog().getWindow().setLayout(width, height);
        super.onResume();
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    private void initSpinner() {
        HintedArrayAdapter adapter =
                new HintedArrayAdapter(getContext(),
                        android.R.layout.simple_spinner_dropdown_item,
                        options);

        binding.spinnerOptions.setAdapter(adapter);
        binding.spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.tvError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                binding.tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void createClickListeners() {
        //Ok
        binding.buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //For the label
                region.getRegionAttributes().clear();
                if (binding.spinnerOptions.getSelectedItemPosition() != 0) {
                    binding.tvError.setVisibility(View.INVISIBLE);
                    region.addRegionAttribute("label", binding.spinnerOptions.getSelectedItem().toString());
                } else {
                    if (!noLabel) {
                        binding.tvError.setVisibility(View.VISIBLE);
                        return;
                    }
                }

                //other attributes
                for (int i = 0; i < binding.tableAttributes.getChildCount(); i++) {
                    TableRow row = (TableRow) binding.tableAttributes.getChildAt(i);
                    String key = ((EditText) row.getChildAt(0)).getText().toString();
                    String val = ((EditText) row.getChildAt(1)).getText().toString();

                    if (!key.isEmpty() && !val.isEmpty() && (noLabel || !key.equals("label"))) {
                        region.addRegionAttribute(key, val);
                    }

                }
                if (!noLabel)
                    listener.onLabelSelected(binding.spinnerOptions.getSelectedItem().toString());
                AttributesFragment.this.dismiss();
            }
        });

        //Cancel
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AttributesFragment.this.dismiss();
            }
        });

        //Add
        binding.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow row = getRow("", "");
                binding.tableAttributes.addView(row);
                row.getChildAt(0).requestFocusFromTouch();
            }
        });
    }

    private void populateTable() {
        String keys[] = region.getRegionAttributes().keySet().toArray(new String[0]);
        String values[] = region.getRegionAttributes().values().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
            if (!noLabel && keys[i].equals("label")) {
                binding.spinnerOptions.setSelection(options.indexOf(values[i]) + 1);
            } else {
                binding.tableAttributes.addView(getRow(keys[i], values[i]));
            }
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

    public void setLabelChangeListener(LabelChangeListener listener) {
        this.listener = listener;
    }

    class HintedArrayAdapter extends ArrayAdapter<String> {

        private List<String> options;

        public HintedArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            options = objects;
        }

        @Nullable
        @Override
        public String getItem(int position) {
            if (position == 0) {
                return getContext().getString(R.string.choose_label);
            } else {
                return options.get(position - 1);
            }
        }

        @Override
        public int getCount() {
            return options.size() + 1;
        }

        @Override
        public boolean isEnabled(int position) {
            return position != 0;
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            TextView tv = (TextView) view;
            if (position == 0) {
                // Set the hint text color gray
                tv.setTextColor(Color.GRAY);
            }
            return view;
        }
    }

    interface LabelChangeListener {
        void onLabelSelected(String label);
    }

}

