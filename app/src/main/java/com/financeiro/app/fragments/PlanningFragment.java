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

import com.financeiro.app.R;
import com.financeiro.app.activities.PlanningFormActivity;
import com.financeiro.app.adapters.BudgetAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Budget;
import com.financeiro.app.utils.FormatUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment de planejamento mensal - exibe orçamentos por categoria com feedback visual.
 */
public class PlanningFragment extends Fragment implements BudgetAdapter.OnBudgetListener {

    private RecyclerView rvBudgets;
    private TextView tvEmpty, tvMes;
    private FloatingActionButton fabAddBudget;
    private AppDatabase db;
    private int currentMonth, currentYear;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_planning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db           = AppDatabase.getInstance(requireContext());
        currentMonth = FormatUtils.getCurrentMonth();
        currentYear  = FormatUtils.getCurrentYear();

        rvBudgets    = view.findViewById(R.id.rv_budgets);
        tvEmpty      = view.findViewById(R.id.tv_planning_empty);
        tvMes        = view.findViewById(R.id.tv_planning_month);
        fabAddBudget = view.findViewById(R.id.fab_add_budget);

        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        tvMes.setText(FormatUtils.formatMonthYear(System.currentTimeMillis()));

        fabAddBudget.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), PlanningFormActivity.class);
            intent.putExtra(PlanningFormActivity.EXTRA_MONTH, currentMonth);
            intent.putExtra(PlanningFormActivity.EXTRA_YEAR, currentYear);
            startActivity(intent);
        });

        loadBudgets();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBudgets();
    }

    private void loadBudgets() {
        List<Budget> budgets = db.budgetDao().getBudgetsByMonthYear(currentMonth, currentYear);
        long start = FormatUtils.getStartOfCurrentMonth();
        long end   = FormatUtils.getEndOfCurrentMonth();

        // Para cada budget, calcula o valor gasto no período
        List<double[]> spentList = new ArrayList<>();
        for (Budget b : budgets) {
            double spent = db.transactionDao().getDespesasByCategoryAndPeriod(b.getCategory(), start, end);
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
