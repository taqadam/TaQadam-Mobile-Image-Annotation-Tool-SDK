package com.recoded.taqadam.fragments;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recoded.taqadam.R;
import com.recoded.taqadam.activities.AppAboutActivity;
import com.recoded.taqadam.databinding.FragAboutBinding;

import java.util.Calendar;

/**
 * Created by wisam on Feb 1 18.
 */

public class AboutFragment extends Fragment {
    private FragAboutBinding binding;
    private OnButtonClickListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.frag_about, container, false);
        binding = DataBindingUtil.bind(root);
        String currentYear = Calendar.getInstance().get(Calendar.YEAR) + "";
        binding.tvCopyright.setText(String.format(getString(R.string.copyright), currentYear));

        binding.bPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClicked("https://www.taqadam.io/docs/PrivacyPolicy");
            }
        });
        binding.bTaqadam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClicked("https://www.taqadam.io");
            }
        });
        binding.bTos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onButtonClicked("https://www.taqadam.io/docs/TrainerAgreement");
            }
        });
        binding.bUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AppAboutActivity.class));
            }
        });

        return root;
    }

    public void setButtonsListener(OnButtonClickListener listener) {
        this.listener = listener;
    }

    public interface OnButtonClickListener {
        void onButtonClicked(String url);
    }

}
