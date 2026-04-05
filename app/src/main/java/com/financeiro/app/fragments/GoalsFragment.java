package com.financeiro.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.activities.GoalFormActivity;
import com.financeiro.app.adapters.GoalAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Goal;
import com.financeiro.app.utils.FormatUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Fragment de metas financeiras.
 * Exibe o saldo atual e uma estimativa diária de poupança por meta.
 */
public class GoalsFragment extends Fragment implements GoalAdapter.OnGoalListener {

    private RecyclerView rvGoals;
    private TextView tvEmpty;
    private TextView tvSaldo;
    private TextView tvSaldoStatus;
    private FloatingActionButton fabAddGoal;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_goals, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        rvGoals      = view.findViewById(R.id.rv_goals);
        tvEmpty      = view.findViewById(R.id.tv_goals_empty);
        tvSaldo      = view.findViewById(R.id.tv_goals_saldo);
        tvSaldoStatus = view.findViewById(R.id.tv_goals_saldo_status);
        fabAddGoal   = view.findViewById(R.id.fab_add_goal);

        rvGoals.setLayoutManager(new LinearLayoutManager(requireContext()));
        fabAddGoal.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), GoalFormActivity.class)));

        loadGoals();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoals();
    }

    private void loadGoals() {
        // Saldo atual
        double saldoTotal = db.transactionDao().getTotalBalance();
        tvSaldo.setText(FormatUtils.formatCurrency(saldoTotal));

        if (saldoTotal >= 0) {
            tvSaldo.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
            tvSaldoStatus.setText("✅");
        } else {
            tvSaldo.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
            tvSaldoStatus.setText("⚠️");
        }

        // Lista de metas
        List<Goal> goals = db.goalDao().getAllGoals();
        if (goals.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvGoals.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvGoals.setVisibility(View.VISIBLE);
            rvGoals.setAdapter(new GoalAdapter(goals, this));
        }
    }

    @Override
    public void onEdit(Goal goal) {
        Intent intent = new Intent(requireContext(), GoalFormActivity.class);
        intent.putExtra(GoalFormActivity.EXTRA_GOAL_ID, goal.getId());
        startActivity(intent);
    }

    @Override
    public void onDelete(Goal goal) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Excluir meta?")
                .setMessage("Deseja remover a meta \"" + goal.getTitle() + "\"?")
                .setPositiveButton("Excluir", (d, w) -> {
                    db.goalDao().delete(goal);
                    loadGoals();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
