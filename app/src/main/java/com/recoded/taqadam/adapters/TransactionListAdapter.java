package com.recoded.taqadam.adapters;

import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.recoded.taqadam.R;
import com.recoded.taqadam.databinding.TransactionItemBinding;
import com.recoded.taqadam.objects.WorkResult;

import java.util.List;

public class TransactionListAdapter extends RecyclerView.Adapter<TransactionListAdapter.ViewHolder> {
    List<WorkResult> transactions;
    TransactionClickListener listener;

    public TransactionListAdapter(List<WorkResult> data) {
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
        final WorkResult workResult = transactions.get(position);
        holder.binding.projectName.setText(workResult.getProjectName());
        holder.binding.estimatedPayment.setText("Not validated payment:" + workResult.getEstimatedPayment());
        holder.binding.validatedPayment.setText("Validated Payment:" + workResult.getValidatedPayment());
        holder.binding.totalLayer.setText("New layer:" + workResult.getTotalNewLayer());
        holder.binding.totalValidatedLayer.setText("Validated layer:" + workResult.getTotalValidatedLayer());
        holder.binding.totalObj.setText("New object:" + workResult.getTotalnewObject());
        holder.binding.totalValidatedObj.setText("Validated object:" + workResult.getTotalValidatedObject());
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
    }

    public interface TransactionClickListener {
        void onItemClick(View v, int position);
    }
}
