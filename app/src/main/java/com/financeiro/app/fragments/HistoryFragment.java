package com.financeiro.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.activities.TransactionFormActivity;
import com.financeiro.app.adapters.TransactionAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment de histórico de transações com filtros por categoria e período.
 */
public class HistoryFragment extends Fragment implements TransactionAdapter.OnTransactionListener {

    private RecyclerView rvHistory;
    private Spinner spinnerCategory, spinnerPeriod;
    private AppDatabase db;
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        rvHistory       = view.findViewById(R.id.rv_history);
        spinnerCategory = view.findViewById(R.id.spinner_filter_category);
        spinnerPeriod   = view.findViewById(R.id.spinner_filter_period);

        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));

        setupFilters();
        loadTransactions();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTransactions();
    }

    private void setupFilters() {
        // Filtro de categoria
        List<String> cats = new ArrayList<>();
        cats.add("Todas as categorias");
        cats.addAll(CategoryUtils.getDespesaCategories());
        cats.addAll(CategoryUtils.getReceitaCategories());

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, cats);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(catAdapter);

        // Filtro de período
        String[] periods = {"Tudo", "Este mês", "Mês passado", "Últimos 3 meses", "Este ano"};
        ArrayAdapter<String> periodAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, periods);
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(periodAdapter);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { loadTransactions(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerCategory.setOnItemSelectedListener(listener);
        spinnerPeriod.setOnItemSelectedListener(listener);
    }

    private void loadTransactions() {
        long[] period = getSelectedPeriod();
        long start = period[0], end = period[1];
        String category = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString() : "Todas as categorias";

        List<Transaction> transactions;
        if ("Todas as categorias".equals(category)) {
            if (start == 0 && end == Long.MAX_VALUE) {
                transactions = db.transactionDao().getAllTransactions();
            } else {
                transactions = db.transactionDao().getTransactionsByPeriod(start, end);
            }
        } else {
            transactions = db.transactionDao().getTransactionsByCategoryAndPeriod(category, start, end);
        }

        adapter = new TransactionAdapter(transactions, this);
        rvHistory.setAdapter(adapter);
    }

    private long[] getSelectedPeriod() {
        int pos = spinnerPeriod.getSelectedItemPosition();
        java.util.Calendar cal = java.util.Calendar.getInstance();

        switch (pos) {
            case 1: // Este mês
                return new long[]{FormatUtils.getStartOfCurrentMonth(), FormatUtils.getEndOfCurrentMonth()};
            case 2: // Mês passado
                cal.add(java.util.Calendar.MONTH, -1);
                cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                long s = cal.getTimeInMillis();
                cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                cal.set(java.util.Calendar.MINUTE, 59);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 999);
                return new long[]{s, cal.getTimeInMillis()};
            case 3: // Últimos 3 meses
                cal.add(java.util.Calendar.MONTH, -3);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                return new long[]{cal.getTimeInMillis(), System.currentTimeMillis()};
            case 4: // Este ano
                cal.set(java.util.Calendar.DAY_OF_YEAR, 1);
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                return new long[]{cal.getTimeInMillis(), System.currentTimeMillis()};
            default: // Tudo
                return new long[]{0, System.currentTimeMillis()};
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
                    loadTransactions();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
