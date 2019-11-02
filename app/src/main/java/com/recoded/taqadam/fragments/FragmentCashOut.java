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
import com.recoded.taqadam.models.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        initView();

        return root;
    }

    private void initView() {
        //TODO: Temporary List
        rvAdapter = new TransactionListAdapter(getDummyList());
        binding.recentTransactions.setAdapter(rvAdapter);
        binding.recentTransactions.setLayoutManager(new LinearLayoutManager(this.getContext()));
        binding.recentTransactions.addItemDecoration(new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        binding.recentTransactions.setItemAnimator(new DefaultItemAnimator());
    }

    public List<Transaction> getDummyList() {
        List<Transaction> dummyList = new ArrayList<>(10);
        float[] a = {-35f, 1.8f, 4.3f, 5.6f, -20f, 5f, 0.86f, 0.64f, 8.84f, 3.7f};
        int h = 3600, d = h * 24, w = d * 7, m = (int) (d * 30.44f), y = (int) (d * 365.24f);
        long refTime = System.currentTimeMillis() / 1000 - d * 4;
        long[] ts;
        for (int i = 0; i < 10; i++) {
            float amount = a[i];
            Date date = new Date((refTime - i * h - d * i * i) * 1000);
            Transaction transact = new Transaction(date, amount);
            String desc = "";
            if (amount < 0) {
                desc = "OMT Branch " + (Math.round(Math.random() * i) + Math.round(Math.random() * 10));
            } else {
                desc = Math.round(amount / 0.04) + " images: Job " + (Math.round(Math.random() * i) + Math.round(Math.random() * 10));
            }
            transact.setTransactionDescription(desc);
            dummyList.add(transact);
        }
        return dummyList;
    }

}

