package com.financeiro.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.adapters.FixedTransactionAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.FixedTransaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Tela para gerenciar receitas e despesas fixas mensais.
 * Ex: Salário, Aluguel, Netflix, etc.
 */
public class FixedTransactionsActivity extends AppCompatActivity
        implements FixedTransactionAdapter.OnFixedTransactionListener {

    private RecyclerView rvFixed;
    private TextView tvEmpty;
    private TextView tvTotalReceitas, tvTotalDespesas, tvSaldoFixo;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixed_transactions);

        db = AppDatabase.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Fixos Mensais");
        }

        rvFixed          = findViewById(R.id.rv_fixed_transactions);
        tvEmpty          = findViewById(R.id.tv_fixed_empty);
        tvTotalReceitas  = findViewById(R.id.tv_total_receitas_fixas);
        tvTotalDespesas  = findViewById(R.id.tv_total_despesas_fixas);
        tvSaldoFixo      = findViewById(R.id.tv_saldo_fixo);

        rvFixed.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_add_fixed);
        fab.setOnClickListener(v -> showAddDialog());

        loadList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList();
    }

    private void loadList() {
        List<FixedTransaction> list = db.fixedTransactionDao().getAll();

        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvFixed.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvFixed.setVisibility(View.VISIBLE);
            rvFixed.setAdapter(new FixedTransactionAdapter(list, this));
        }

        // Atualiza resumo
        double totalReceitas = db.fixedTransactionDao().getTotalFixedReceitas();
        double totalDespesas = db.fixedTransactionDao().getTotalFixedDespesas();
        double saldo = totalReceitas - totalDespesas;

        tvTotalReceitas.setText(FormatUtils.formatCurrency(totalReceitas));
        tvTotalDespesas.setText(FormatUtils.formatCurrency(totalDespesas));
        tvSaldoFixo.setText(FormatUtils.formatCurrency(saldo));

        if (saldo >= 0) {
            tvSaldoFixo.setTextColor(ContextCompat.getColor(this, R.color.income_color));
        } else {
            tvSaldoFixo.setTextColor(ContextCompat.getColor(this, R.color.expense_color));
        }
    }

    private void showAddDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_fixed_transaction, null);

        RadioGroup rgType     = dialogView.findViewById(R.id.rg_fixed_type);
        EditText   etTitle    = dialogView.findViewById(R.id.et_fixed_title);
        EditText   etAmount   = dialogView.findViewById(R.id.et_fixed_amount);
        Spinner    spCategory = dialogView.findViewById(R.id.sp_fixed_category);

        // Começa como RECEITA
        updateCategorySpinner(spCategory, true);

        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isReceita = (checkedId == R.id.rb_fixed_receita);
            updateCategorySpinner(spCategory, isReceita);
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nova Transação Fixa")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String title  = etTitle.getText().toString().trim();
                    String amtStr = etAmount.getText().toString().trim().replace(",", ".");

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Informe o nome", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (amtStr.isEmpty()) {
                        Toast.makeText(this, "Informe o valor", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double amount;
                    try {
                        amount = Double.parseDouble(amtStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean isReceita = (rgType.getCheckedRadioButtonId() == R.id.rb_fixed_receita);
                    String type = isReceita ? FixedTransaction.TYPE_RECEITA : FixedTransaction.TYPE_DESPESA;
                    String category = spCategory.getSelectedItem().toString();

                    FixedTransaction ft = new FixedTransaction(title, amount, type, category);
                    db.fixedTransactionDao().insert(ft);

                    // Invalida o cache mensal para que os fixos sejam reaplicados
                    resetMonthlyAppliedFlag();

                    loadList();
                    Toast.makeText(this, "Transação fixa adicionada!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateCategorySpinner(Spinner spinner, boolean isReceita) {
        List<String> categories = isReceita
                ? CategoryUtils.getReceitaCategories()
                : CategoryUtils.getDespesaCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Reseta o flag do mês atual para que os fixos sejam reaplicados
     * na próxima abertura do app.
     */
    private void resetMonthlyAppliedFlag() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int month = cal.get(java.util.Calendar.MONTH) + 1;
        int year  = cal.get(java.util.Calendar.YEAR);
        String key = "fixed_applied_" + year + "_" + month;
        getSharedPreferences("settings_prefs", MODE_PRIVATE)
                .edit().remove(key).apply();
    }

    @Override
    public void onDelete(FixedTransaction item) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir fixo?")
                .setMessage("Remover \"" + item.getTitle() + "\" dos fixos mensais?")
                .setPositiveButton("Excluir", (d, w) -> {
                    db.fixedTransactionDao().delete(item);
                    resetMonthlyAppliedFlag();
                    loadList();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
