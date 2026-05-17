package com.financeiro.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.activities.InstallmentFormActivity;
import com.financeiro.app.activities.PlanningFormActivity;
import com.financeiro.app.adapters.BudgetAdapter;
import com.financeiro.app.adapters.InstallmentAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Budget;
import com.financeiro.app.models.Installment;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.FormatUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class PlanningFragment extends Fragment
        implements BudgetAdapter.OnBudgetListener, InstallmentAdapter.OnInstallmentListener {

    private RecyclerView     rvBudgets, rvInstallments;
    private TextView         tvEmpty, tvInstallmentsEmpty, tvMes;
    private LinearLayout     sectionBudgets, sectionInstallments;
    private FloatingActionButton fabAdd;
    private TabLayout        tabPlanning;
    private AppDatabase      db;
    private int              currentMonth, currentYear;
    private boolean          showingInstallments = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db           = AppDatabase.getInstance(requireContext());
        currentMonth = FormatUtils.getCurrentMonth();
        currentYear  = FormatUtils.getCurrentYear();

        rvBudgets           = view.findViewById(R.id.rv_budgets);
        rvInstallments      = view.findViewById(R.id.rv_installments);
        tvEmpty             = view.findViewById(R.id.tv_planning_empty);
        tvInstallmentsEmpty = view.findViewById(R.id.tv_installments_empty);
        tvMes               = view.findViewById(R.id.tv_planning_month);
        sectionBudgets      = view.findViewById(R.id.section_budgets);
        sectionInstallments = view.findViewById(R.id.section_installments);
        fabAdd              = view.findViewById(R.id.fab_add_budget);
        tabPlanning         = view.findViewById(R.id.tab_planning);

        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvInstallments.setLayoutManager(new LinearLayoutManager(requireContext()));

        tvMes.setText(FormatUtils.formatMonthYear(System.currentTimeMillis()));

        tabPlanning.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingInstallments = tab.getPosition() == 1;
                updateSectionVisibility();
                refresh();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        fabAdd.setOnClickListener(v -> {
            if (showingInstallments) {
                startActivity(new Intent(requireContext(), InstallmentFormActivity.class));
            } else {
                Intent intent = new Intent(requireContext(), PlanningFormActivity.class);
                intent.putExtra(PlanningFormActivity.EXTRA_MONTH, currentMonth);
                intent.putExtra(PlanningFormActivity.EXTRA_YEAR, currentYear);
                startActivity(intent);
            }
        });

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void updateSectionVisibility() {
        sectionBudgets.setVisibility(showingInstallments ? View.GONE  : View.VISIBLE);
        sectionInstallments.setVisibility(showingInstallments ? View.VISIBLE : View.GONE);
    }

    private void refresh() {
        if (showingInstallments) loadInstallments();
        else                     loadBudgets();
    }

    // ── Orçamentos ────────────────────────────────────────────────────────────

    private void loadBudgets() {
        List<Budget> budgets = db.budgetDao().getBudgetsByMonthYear(currentMonth, currentYear);
        long start = FormatUtils.getStartOfCurrentMonth();
        long end   = FormatUtils.getEndOfCurrentMonth();

        List<double[]> spentList = new ArrayList<>();
        for (Budget b : budgets) {
            double spent = db.transactionDao()
                    .getDespesasByCategoryAndPeriod(b.getCategory(), start, end);
            spentList.add(new double[]{spent});
        }

        if (budgets.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvBudgets.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvBudgets.setVisibility(View.VISIBLE);
            rvBudgets.setAdapter(new BudgetAdapter(budgets, spentList, this));
        }
    }

    // ── Parcelas ──────────────────────────────────────────────────────────────

    private void loadInstallments() {
        List<Installment> list = db.installmentDao().getAll();

        if (list.isEmpty()) {
            tvInstallmentsEmpty.setVisibility(View.VISIBLE);
            rvInstallments.setVisibility(View.GONE);
        } else {
            tvInstallmentsEmpty.setVisibility(View.GONE);
            rvInstallments.setVisibility(View.VISIBLE);
            rvInstallments.setAdapter(new InstallmentAdapter(list, this));
        }
    }

    @Override
    public void onPay(Installment installment) {
        String msg = "Registrar parcela " + (installment.getPaidInstallments() + 1)
                + "/" + installment.getTotalInstallments()
                + " de " + FormatUtils.formatCurrency(installment.getInstallmentAmount()) + "?";

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Registrar parcela")
                .setMessage(msg)
                .setPositiveButton("Confirmar", (d, w) -> {
                    // Cria a transação de despesa
                    Transaction t = new Transaction(
                            installment.getInstallmentAmount(),
                            Transaction.TYPE_DESPESA,
                            installment.getCategory(),
                            System.currentTimeMillis(),
                            "Parcela " + (installment.getPaidInstallments() + 1)
                                    + "/" + installment.getTotalInstallments()
                                    + " - " + installment.getTitle()
                    );
                    db.transactionDao().insert(t);

                    // Atualiza a parcela
                    int newPaid = installment.getPaidInstallments() + 1;
                    installment.setPaidInstallments(newPaid);
                    if (newPaid >= installment.getTotalInstallments()) {
                        installment.setStatus(Installment.STATUS_CONCLUIDA);
                    }
                    db.installmentDao().update(installment);
                    loadInstallments();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDelete(Installment installment) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir parcelamento?")
                .setMessage("Deseja remover \"" + installment.getTitle() + "\"?\n"
                        + "As transações já registradas não serão removidas.")
                .setPositiveButton("Excluir", (d, w) -> {
                    db.installmentDao().delete(installment);
                    loadInstallments();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── BudgetAdapter callbacks ───────────────────────────────────────────────

    @Override
    public void onEdit(Budget budget) {
        Intent intent = new Intent(requireContext(), PlanningFormActivity.class);
        intent.putExtra(PlanningFormActivity.EXTRA_BUDGET_ID, budget.getId());
        intent.putExtra(PlanningFormActivity.EXTRA_MONTH, currentMonth);
        intent.putExtra(PlanningFormActivity.EXTRA_YEAR, currentYear);
        startActivity(intent);
    }

    @Override
    public void onDelete(Budget budget) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir orçamento?")
                .setMessage("Deseja remover o orçamento de \"" + budget.getCategory() + "\"?")
                .setPositiveButton("Excluir", (d, w) -> {
                    db.budgetDao().delete(budget);
                    loadBudgets();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
