package com.financeiro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;

import java.util.List;

/**
 * Adapter para lista de transações financeiras.
 */
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    public interface OnTransactionListener {
        void onEdit(Transaction transaction);
        void onDelete(Transaction transaction);
    }

    private final List<Transaction> transactions;
    private final OnTransactionListener listener;

    public TransactionAdapter(List<Transaction> transactions, OnTransactionListener listener) {
        this.transactions = transactions;
        this.listener     = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction t = transactions.get(position);

        // Emoji + categoria
        holder.tvCategory.setText(
                CategoryUtils.getCategoryEmoji(t.getCategory()) + " " + t.getCategory()
        );

        // Descrição
        if (t.getDescription() != null && !t.getDescription().isEmpty()) {
            holder.tvDescription.setText(t.getDescription());
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setVisibility(View.GONE);
        }

        // Data
        holder.tvDate.setText(FormatUtils.formatDate(t.getDate()));

        // Valor com cor e sinal
        if (Transaction.TYPE_RECEITA.equals(t.getType())) {
            holder.tvAmount.setText("+ " + FormatUtils.formatCurrency(t.getAmount()));
            holder.tvAmount.setTextColor(0xFF4CAF50); // Verde
        } else {
            holder.tvAmount.setText("- " + FormatUtils.formatCurrency(t.getAmount()));
            holder.tvAmount.setTextColor(0xFFF44336); // Vermelho
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(t));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(t));
    }

    @Override
    public int getItemCount() { return transactions.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDescription, tvDate, tvAmount;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View view) {
            super(view);
            tvCategory   = view.findViewById(R.id.tv_item_category);
            tvDescription = view.findViewById(R.id.tv_item_description);
            tvDate       = view.findViewById(R.id.tv_item_date);
            tvAmount     = view.findViewById(R.id.tv_item_amount);
            btnEdit      = view.findViewById(R.id.btn_item_edit);
            btnDelete    = view.findViewById(R.id.btn_item_delete);
        }
    }
}
