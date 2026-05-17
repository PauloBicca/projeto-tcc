package com.financeiro.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.models.Installment;
import com.financeiro.app.utils.FormatUtils;

import java.util.List;

public class InstallmentAdapter extends RecyclerView.Adapter<InstallmentAdapter.ViewHolder> {

    public interface OnInstallmentListener {
        void onPay(Installment installment);
        void onDelete(Installment installment);
    }

    private final List<Installment> items;
    private final OnInstallmentListener listener;

    public InstallmentAdapter(List<Installment> items, OnInstallmentListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_installment, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Installment inst = items.get(position);

        h.tvTitle.setText(inst.getTitle());
        h.tvCategory.setText(inst.getCategory());

        // Ex: "3 / 12 parcelas"
        h.tvCount.setText(inst.getPaidInstallments() + " / "
                + inst.getTotalInstallments() + " parcelas");

        // Progresso
        int pct = (int) ((inst.getPaidInstallments() / (float) inst.getTotalInstallments()) * 100);
        h.progress.setProgress(pct);

        // Valor por parcela
        h.tvAmount.setText(FormatUtils.formatCurrency(inst.getInstallmentAmount()) + " / parcela");

        // Próximo vencimento
        if (!inst.isCompleted()) {
            h.tvNext.setText("Próx: " + FormatUtils.formatDate(inst.getNextDueDate()));
        } else {
            h.tvNext.setText("Concluída");
        }

        // Total restante
        h.tvRemaining.setText("Restante: " + FormatUtils.formatCurrency(inst.getRemainingAmount())
                + " (" + inst.getRemainingInstallments() + "x)");

        // Botão pagar
        boolean canPay = !inst.isCompleted();
        h.btnPay.setEnabled(canPay);
        h.btnPay.setAlpha(canPay ? 1f : 0.4f);
        h.btnPay.setOnClickListener(v -> listener.onPay(inst));

        h.btnDelete.setOnClickListener(v -> listener.onDelete(inst));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvTitle, tvCategory, tvCount, tvAmount, tvNext, tvRemaining;
        ProgressBar progress;
        Button      btnPay;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvTitle    = v.findViewById(R.id.tv_installment_title);
            tvCategory = v.findViewById(R.id.tv_installment_category);
            tvCount    = v.findViewById(R.id.tv_installment_count);
            tvAmount   = v.findViewById(R.id.tv_installment_amount);
            tvNext     = v.findViewById(R.id.tv_installment_next);
            tvRemaining= v.findViewById(R.id.tv_installment_remaining);
            progress   = v.findViewById(R.id.progress_installment);
            btnPay     = v.findViewById(R.id.btn_pay_installment);
            btnDelete  = v.findViewById(R.id.btn_delete_installment);
        }
    }
}
