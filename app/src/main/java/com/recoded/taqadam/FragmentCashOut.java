package com.recoded.taqadam;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recoded.taqadam.databinding.FragWalletBinding;
import com.recoded.taqadam.databinding.TransactionItemBinding;
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

class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.ViewHolder> {
    List<Transaction> transactions;
    TransactionClickListener listener;

    public TransactionListAdapter(List<Transaction> data) {
        this.transactions = data;
    }

    @NonNull
    @Override
    public TransactionListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item, parent, false);
        final ViewHolder mViewHolder = new ViewHolder(mView);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onItemClick(v, mViewHolder.getAdapterPosition());
            }
        });
        return mViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionListAdapter.ViewHolder holder, int position) {
        holder.setItem(transactions.get(position));
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TransactionItemBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }

        public void setItem(Transaction t) {
            binding.setTransact(t);
        }
    }

    public interface TransactionClickListener {
        void onItemClick(View v, int position);
    }
}
