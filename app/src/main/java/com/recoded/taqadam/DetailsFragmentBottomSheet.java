package com.recoded.taqadam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.recoded.taqadam.databinding.FragWorkDetailsBinding;
import com.recoded.taqadam.models.ProgressDetails;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

public class DetailsFragmentBottomSheet extends BottomSheetDialogFragment {
    private ProgressDetails progressDetails;

    public static DetailsFragmentBottomSheet newInstance() {
        return new DetailsFragmentBottomSheet();
    }

    public FragWorkDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.frag_work_details, container,
                false);

        binding = DataBindingUtil.bind(view);

        // get the views and attach the listener

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (progressDetails != null)
            binding.setDetails(progressDetails);
    }

    public ProgressDetails getProgressDetails() {
        return progressDetails;
    }

    public void setProgressDetails(ProgressDetails progressDetails) {
        this.progressDetails = progressDetails;
    }
}
