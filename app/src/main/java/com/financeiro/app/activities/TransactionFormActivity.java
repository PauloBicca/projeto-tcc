package com.financeiro.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Tela de cadastro e edição de transações financeiras.
 */
public class TransactionFormActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "transaction_id";

    private EditText etAmount, etDescription;
    private RadioGroup rgType;
    private RadioButton rbReceita, rbDespesa;
    private Spinner spinnerCategory;
    private TextView tvDate;
    private Button btnSave;
    private AppDatabase db;
    private long selectedDate;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_form);

        db = AppDatabase.getInstance(this);
        selectedDate = System.currentTimeMillis();

        setupToolbar();
        initViews();
        setupTypeToggle();

        // Modo edição
        editId = getIntent().getLongExtra(EXTRA_TRANSACTION_ID, -1);
        if (editId != -1) {
            loadTransaction(editId);
        } else {
            updateCategorySpinner(Transaction.TYPE_DESPESA);
        }

        tvDate.setText(FormatUtils.formatDate(selectedDate));
        tvDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(editId != -1 ? "Editar Transação" : "Nova Transação");
        }
    }

    private void initViews() {
        etAmount      = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        rgType        = findViewById(R.id.rg_type);
        rbReceita     = findViewById(R.id.rb_receita);
        rbDespesa     = findViewById(R.id.rb_despesa);
        spinnerCategory = findViewById(R.id.spinner_category);
        tvDate        = findViewById(R.id.tv_date);
        btnSave       = findViewById(R.id.btn_save);
    }

    private void setupTypeToggle() {
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_receita) {
                updateCategorySpinner(Transaction.TYPE_RECEITA);
            } else {
                updateCategorySpinner(Transaction.TYPE_DESPESA);
            }
        });
        rbDespesa.setChecked(true);
    }

    private void updateCategorySpinner(String type) {
        List<String> categories = type.equals(Transaction.TYPE_RECEITA)
                ? CategoryUtils.getReceitaCategories()
                : CategoryUtils.getDespesaCategories();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDate);
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            selectedDate = cal.getTimeInMillis();
            tvDate.setText(FormatUtils.formatDate(selectedDate));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadTransaction(long id) {
        Transaction t = db.transactionDao().getTransactionById(id);
        if (t == null) return;

        etAmount.setText(String.valueOf(t.getAmount()));
        etDescription.setText(t.getDescription());
        selectedDate = t.getDate();
        tvDate.setText(FormatUtils.formatDate(selectedDate));

        if (Transaction.TYPE_RECEITA.equals(t.getType())) {
            rbReceita.setChecked(true);
            updateCategorySpinner(Transaction.TYPE_RECEITA);
        } else {
            rbDespesa.setChecked(true);
            updateCategorySpinner(Transaction.TYPE_DESPESA);
        }

        // Selecionar a categoria correta no spinner
        ArrayAdapter adapter = (ArrayAdapter) spinnerCategory.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(t.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveTransaction() {
        // Validação
        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) {
            etAmount.setError("Informe o valor");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.replace(",", "."));
        } catch (NumberFormatException e) {
            etAmount.setError("Valor inválido");
            return;
        }

        if (amount <= 0) {
            etAmount.setError("Valor deve ser maior que zero");
            return;
        }

        String type = rbReceita.isChecked() ? Transaction.TYPE_RECEITA : Transaction.TYPE_DESPESA;
        String category = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString() : "Outros";
        String description = etDescription.getText().toString().trim();

        Transaction t = new Transaction(amount, type, category, selectedDate, description);

        if (editId != -1) {
            t.setId(editId);
            db.transactionDao().update(t);
            Toast.makeText(this, "Transação atualizada!", Toast.LENGTH_SHORT).show();
        } else {
            db.transactionDao().insert(t);
            Toast.makeText(this, "Transação salva!", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
