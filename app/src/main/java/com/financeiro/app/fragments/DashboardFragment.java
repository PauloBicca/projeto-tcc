package com.financeiro.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.core.content.ContextCompat;

import com.financeiro.app.R;
import com.financeiro.app.activities.SettingsActivity;
import com.financeiro.app.activities.TransactionFormActivity;
import com.financeiro.app.adapters.TransactionAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Budget;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.FormatUtils;
import com.financeiro.app.utils.NotificationUtils;
import java.util.List;

/**
 * Fragment do Dashboard - exibe resumo financeiro e últimas transações.
 */
public class DashboardFragment extends Fragment implements TransactionAdapter.OnTransactionListener {

    private TextView tvSaldo, tvReceitas, tvDespesas, tvDiferenca, tvMesAno;
    private RecyclerView rvRecent;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        tvSaldo     = view.findViewById(R.id.tv_saldo_total);
        tvReceitas  = view.findViewById(R.id.tv_receitas_mes);
        tvDespesas  = view.findViewById(R.id.tv_despesas_mes);
        tvDiferenca = view.findViewById(R.id.tv_diferenca);
        tvMesAno    = view.findViewById(R.id.tv_mes_ano);
        rvRecent    = view.findViewById(R.id.rv_recent);

        rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Botão configurações
        view.findViewById(R.id.btn_settings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        // Botão Dicas IA
        view.findViewById(R.id.card_ai_tips).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), com.financeiro.app.activities.AiTipsActivity.class)));

        loadDashboard();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDashboard();
    }

    private void loadDashboard() {
        long start = FormatUtils.getStartOfCurrentMonth();
        long end   = FormatUtils.getEndOfCurrentMonth();

        double saldoTotal = db.transactionDao().getTotalBalance();
        double receitas   = db.transactionDao().getTotalReceitasByPeriod(start, end);
        double despesas   = db.transactionDao().getTotalDespesasByPeriod(start, end);
        double diferenca  = receitas - despesas;

        tvMesAno.setText(FormatUtils.formatMonthYear(System.currentTimeMillis()));
        tvSaldo.setText(FormatUtils.formatCurrency(saldoTotal));
        tvReceitas.setText(FormatUtils.formatCurrency(receitas));
        tvDespesas.setText(FormatUtils.formatCurrency(despesas));
        tvDiferenca.setText(FormatUtils.formatCurrency(diferenca));

        // Cor da diferença
        if (diferenca >= 0) {
            tvDiferenca.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
        } else {
            tvDiferenca.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
        }

        // Cor do saldo
        if (saldoTotal >= 0) {
            tvSaldo.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
        } else {
            tvSaldo.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
        }

        // Últimas 5 transações do mês atual
        List<Transaction> recent = db.transactionDao().getTransactionsByPeriod(start, end);
        if (recent.size() > 5) recent = recent.subList(0, 5);

        TransactionAdapter adapter = new TransactionAdapter(recent, this);
        rvRecent.setAdapter(adapter);

        // Verificar alertas de orçamento
        checkBudgetAlerts(despesas, start, end);
    }

    /**
     * Verifica se algum orçamento está perto ou acima do limite e dispara alertas.
     */
    private void checkBudgetAlerts(double totalDespesas, long start, long end) {
        int month = FormatUtils.getCurrentMonth();
        int year  = FormatUtils.getCurrentYear();
        List<Budget> budgets = db.budgetDao().getBudgetsByMonthYear(month, year);

        for (Budget budget : budgets) {
            double spent = db.transactionDao().getDespesasByCategoryAndPeriod(
                    budget.getCategory(), start, end);
            int percent = budget.getPercentUsed(spent);
            String status = budget.getStatus(spent);

            if (Budget.STATUS_OVER.equals(status)) {
                NotificationUtils.sendBudgetAlert(requireContext(), budget.getCategory(), percent, true);
            } else if (Budget.STATUS_WARNING.equals(status)) {
                NotificationUtils.sendBudgetAlert(requireContext(), budget.getCategory(), percent, false);
            }
        }
    }

    @Override
    public void onEdit(Transaction transaction) {
        Intent intent = new Intent(requireContext(), TransactionFormActivity.class);
        intent.putExtra(TransactionFormActivity.EXTRA_TRANSACTION_ID, transaction.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Transaction transaction) {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir transação?")
                .setMessage("Deseja remover esta transação?")
                .setPositiveButton("Excluir", (d, w) -> {
                    db.transactionDao().delete(transaction);
                    loadDashboard();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
