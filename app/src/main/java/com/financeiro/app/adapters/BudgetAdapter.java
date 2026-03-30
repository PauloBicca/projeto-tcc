package com.financeiro.app.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.models.Budget;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;

import java.util.List;

/**
 * Adapter para lista de orçamentos mensais com feedback visual (verde/amarelo/vermelho).
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.ViewHolder> {

    public interface OnBudgetListener {
        void onEdit(Budget budget);
        void onDelete(Budget budget);
    }

    private final List<Budget>   budgets;
    private final List<double[]> spentList; // valor gasto por posição
    private final OnBudgetListener listener;

    public BudgetAdapter(List<Budget> budgets, List<double[]> spentList, OnBudgetListener listener) {
        this.budgets   = budgets;
        this.spentList = spentList;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_budget, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Budget b     = budgets.get(position);
        double spent = spentList.get(position)[0];
        String status = b.getStatus(spent);
        int percent   = Math.min(b.getPercentUsed(spent), 100);

        holder.tvCategory.setText(CategoryUtils.getCategoryEmoji(b.getCategory()) + " " + b.getCategory());
        holder.tvSpent.setText(FormatUtils.formatCurrency(spent));
        holder.tvLimit.setText("Limite: " + FormatUtils.formatCurrency(b.getLimitAmount()));

        double remaining = b.getRemaining(spent);
        if (remaining >= 0) {
            holder.tvRemaining.setText("Resta: " + FormatUtils.formatCurrency(remaining));
        } else {
            holder.tvRemaining.setText("Excedeu: " + FormatUtils.formatCurrency(Math.abs(remaining)));
        }

        holder.progressBar.setProgress(percent);

        // Feedback visual por cor
        int color;
        switch (status) {
            case Budget.STATUS_OVER:
                color = 0xFFF44336; // Vermelho
                break;
            case Budget.STATUS_WARNING:
                color = 0xFFFF9800; // Amarelo/Laranja
                break;
            default:
                color = 0xFF4CAF50; // Verde
                break;
        }
        holder.progressBar.setProgressTintList(ColorStateList.valueOf(color));
        holder.tvSpent.setTextColor(color);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(b));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(b));
    }

    @Override
    public int getItemCount() { return budgets.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvCategory, tvSpent, tvLimit, tvRemaining;
        ProgressBar progressBar;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View v) {
            super(v);
            tvCategory  = v.findViewById(R.id.tv_budget_category);
            tvSpent     = v.findViewById(R.id.tv_budget_spent);
            tvLimit     = v.findViewById(R.id.tv_budget_limit);
            tvRemaining = v.findViewById(R.id.tv_budget_remaining);
            progressBar = v.findViewById(R.id.progress_budget);
            btnEdit     = v.findViewById(R.id.btn_budget_edit);
            btnDelete   = v.findViewById(R.id.btn_budget_delete);
        }
    }
}
