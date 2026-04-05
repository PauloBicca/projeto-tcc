package com.financeiro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.models.FixedTransaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;

import java.util.List;

/**
 * Adapter para lista de transações fixas.
 */
public class FixedTransactionAdapter extends RecyclerView.Adapter<FixedTransactionAdapter.ViewHolder> {

    public interface OnFixedTransactionListener {
        void onDelete(FixedTransaction item);
    }

    private final List<FixedTransaction> items;
    private final OnFixedTransactionListener listener;

    public FixedTransactionAdapter(List<FixedTransaction> items, OnFixedTransactionListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fixed_transaction, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FixedTransaction ft = items.get(position);

        String emoji = CategoryUtils.getCategoryEmoji(ft.getCategory());
        holder.tvTitle.setText(emoji + " " + ft.getTitle());
        holder.tvCategory.setText(ft.getCategory());

        if (ft.isReceita()) {
            holder.tvAmount.setText("+ " + FormatUtils.formatCurrency(ft.getAmount()));
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.income_color));
            holder.tvType.setText("RECEITA");
            holder.tvType.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.income_color));
        } else {
            holder.tvAmount.setText("- " + FormatUtils.formatCurrency(ft.getAmount()));
            holder.tvAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_color));
            holder.tvType.setText("DESPESA");
            holder.tvType.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.expense_color));
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(ft));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvType;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tv_fixed_title);
            tvCategory = v.findViewById(R.id.tv_fixed_category);
            tvAmount   = v.findViewById(R.id.tv_fixed_amount);
            tvType     = v.findViewById(R.id.tv_fixed_type);
            btnDelete  = v.findViewById(R.id.btn_fixed_delete);
        }
    }
}
