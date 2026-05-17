package com.financeiro.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Budget;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;

import java.util.List;

/**
 * Formulário para criar/editar orçamentos mensais por categoria.
 */
public class PlanningFormActivity extends AppCompatActivity {

    public static final String EXTRA_BUDGET_ID = "budget_id";
    public static final String EXTRA_MONTH     = "month";
    public static final String EXTRA_YEAR      = "year";

    private Spinner spinnerCategory;
    private EditText etLimit;
    private Button btnSave;
    private AppDatabase db;
    private long editId = -1;
    private int month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planning_form);

        db    = AppDatabase.getInstance(this);
        editId = getIntent().getLongExtra(EXTRA_BUDGET_ID, -1);
        month  = getIntent().getIntExtra(EXTRA_MONTH, FormatUtils.getCurrentMonth());
        year   = getIntent().getIntExtra(EXTRA_YEAR,  FormatUtils.getCurrentYear());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(editId != -1 ? "Editar Orçamento" : "Novo Orçamento");
        }

        spinnerCategory = findViewById(R.id.spinner_budget_category);
        etLimit         = findViewById(R.id.et_budget_limit);
        btnSave         = findViewById(R.id.btn_save_budget);

        // Preenche spinner com categorias de despesa
        List<String> cats = CategoryUtils.getDespesaCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        if (editId != -1) loadBudget(editId);

        btnSave.setOnClickListener(v -> saveBudget());
    }

    private void loadBudget(long id) {
        Budget b = db.budgetDao().getBudgetById(id);
        if (b == null) return;
        etLimit.setText(String.valueOf(b.getLimitAmount()));
        ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(b.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void saveBudget() {
        String limitStr = etLimit.getText().toString().trim();
        if (TextUtils.isEmpty(limitStr)) { etLimit.setError("Informe o limite"); return; }

        double limit;
        try {
            limit = Double.parseDouble(limitStr.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) { etLimit.setError("Valor inválido"); return; }

        if (limit <= 0) { etLimit.setError("Limite deve ser maior que zero"); return; }

        String category = spinnerCategory.getSelectedItem().toString();
        Budget b = new Budget(category, limit, month, year);

        if (editId != -1) {
            b.setId(editId);
            db.budgetDao().update(b);
            Toast.makeText(this, "Orçamento atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            // Verifica se já existe orçamento para essa categoria/mês/ano
            Budget existing = db.budgetDao().getBudgetByCategoryAndMonthYear(category, month, year);
            if (existing != null) {
                existing.setLimitAmount(limit);
                db.budgetDao().update(existing);
                Toast.makeText(this, "Orçamento atualizado!", Toast.LENGTH_SHORT).show();
            } else {
                db.budgetDao().insert(b);
                Toast.makeText(this, "Orçamento criado!", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
