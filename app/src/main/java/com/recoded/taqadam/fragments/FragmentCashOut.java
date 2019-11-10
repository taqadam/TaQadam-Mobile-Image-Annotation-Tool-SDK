package com.recoded.taqadam.fragments;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recoded.taqadam.R;
import com.recoded.taqadam.adapters.TransactionListAdapter;
import com.recoded.taqadam.databinding.FragWalletBinding;
import com.recoded.taqadam.models.Api.Api;
import com.recoded.taqadam.objects.WorkResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentCashOut extends Fragment {
    private FragWalletBinding binding;
    private TransactionListAdapter rvAdapter;

    public FragmentCashOut() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.frag_wallet, container, false);

        binding = DataBindingUtil.bind(root);
        getWorkResultsAndShow();

        return root;
    }

    public void getWorkResultsAndShow() {
        Call<List<WorkResult>> call = Api.getInstance().endpoints.getWorkResult();
        call.enqueue(new Callback<List<WorkResult>>() {
            @Override
            public void onResponse(Call<List<WorkResult>> call, Response<List<WorkResult>> response) {
                List<WorkResult> workResults = response.body();
                initView(workResults);
                calculateBalanceAndShow(workResults);
            }

            @Override
            public void onFailure(Call<List<WorkResult>> call, Throwable t) {

            }
        });
    }

    private void initView(List<WorkResult> workResults) {
        rvAdapter = new TransactionListAdapter(workResults);
        binding.recentTransactions.setAdapter(rvAdapter);
        binding.recentTransactions.setLayoutManager(new LinearLayoutManager(this.getContext()));
        binding.recentTransactions.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        binding.recentTransactions.setItemAnimator(new DefaultItemAnimator());
    }

    private void calculateBalanceAndShow(List<WorkResult> workResults) {
        float balance = 0;
        for (WorkResult workResult : workResults) {
            balance += workResult.getValidatedPayment();
        }
        binding.validatedBalance.setText("USD " + balance);
    }
}

