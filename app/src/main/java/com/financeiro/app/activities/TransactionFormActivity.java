package com.financeiro.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Installment;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.List;

public class TransactionFormActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "transaction_id";

    private EditText        etAmount, etDescription, etInstallmentCount;
    private RadioGroup      rgType;
    private RadioButton     rbReceita, rbDespesa;
    private Spinner         spinnerCategory;
    private TextView        tvDate, tvInstallmentSummary, tvInstallmentLast;
    private Button          btnSave;
    private CheckBox        cbInstallment;
    private LinearLayout    sectionInstallment;
    private TextInputLayout tilInstallmentCount, tilAmount;
    private MaterialCardView cardInstallmentSummary;
    private AppDatabase     db;
    private long            selectedDate;
    private long            editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_form);

        db = AppDatabase.getInstance(this);
        selectedDate = System.currentTimeMillis();

        setupToolbar();
        initViews();
        setupTypeToggle();
        setupInstallmentSection();

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
        etAmount             = findViewById(R.id.et_amount);
        etDescription        = findViewById(R.id.et_description);
        rgType               = findViewById(R.id.rg_type);
        rbReceita            = findViewById(R.id.rb_receita);
        rbDespesa            = findViewById(R.id.rb_despesa);
        spinnerCategory      = findViewById(R.id.spinner_category);
        tvDate               = findViewById(R.id.tv_date);
        btnSave              = findViewById(R.id.btn_save);
        cbInstallment        = findViewById(R.id.cb_installment);
        sectionInstallment   = findViewById(R.id.section_installment);
        tilAmount            = findViewById(R.id.til_amount);
        tilInstallmentCount  = findViewById(R.id.til_installment_count);
        etInstallmentCount   = findViewById(R.id.et_installment_count);
        cardInstallmentSummary = findViewById(R.id.card_installment_summary);
        tvInstallmentSummary = findViewById(R.id.tv_installment_summary);
        tvInstallmentLast    = findViewById(R.id.tv_installment_last);
    }

    private void setupTypeToggle() {
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isDespesa = checkedId == R.id.rb_despesa;
            updateCategorySpinner(isDespesa ? Transaction.TYPE_DESPESA : Transaction.TYPE_RECEITA);
            sectionInstallment.setVisibility(isDespesa ? View.VISIBLE : View.GONE);
            if (!isDespesa) {
                cbInstallment.setChecked(false);
            }
        });
        rbDespesa.setChecked(true);
        sectionInstallment.setVisibility(View.VISIBLE);
    }

    private void setupInstallmentSection() {
        cbInstallment.setOnCheckedChangeListener((btn, checked) -> {
            tilInstallmentCount.setVisibility(checked ? View.VISIBLE : View.GONE);
            tilAmount.setHint(checked ? "Valor total (R$)" : "Valor (R$)");
            if (!checked) cardInstallmentSummary.setVisibility(View.GONE);
            else updateInstallmentSummary();
        });

        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { updateInstallmentSummary(); }
            public void afterTextChanged(Editable s) {}
        };
        etAmount.addTextChangedListener(watcher);
        etInstallmentCount.addTextChangedListener(watcher);
    }

    private void updateInstallmentSummary() {
        if (!cbInstallment.isChecked()) return;

        String amtStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String cntStr = etInstallmentCount.getText() != null ? etInstallmentCount.getText().toString().trim() : "";

        if (amtStr.isEmpty() || cntStr.isEmpty()) {
            cardInstallmentSummary.setVisibility(View.GONE);
            return;
        }
        try {
            double amt = Double.parseDouble(amtStr.replace(".", "").replace(",", "."));
            int    cnt = Integer.parseInt(cntStr);
            if (amt <= 0 || cnt <= 1) { cardInstallmentSummary.setVisibility(View.GONE); return; }

            double perInstallment = amt / cnt;
            cardInstallmentSummary.setVisibility(View.VISIBLE);
            tvInstallmentSummary.setText("Total: " + FormatUtils.formatCurrency(amt)
                    + " em " + cnt + "x de " + FormatUtils.formatCurrency(perInstallment));

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedDate);
            cal.add(Calendar.MONTH, cnt - 1);
            tvInstallmentLast.setText("Última parcela: " + FormatUtils.formatMonthYear(cal.getTimeInMillis()));
        } catch (NumberFormatException e) {
            cardInstallmentSummary.setVisibility(View.GONE);
        }
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
            updateInstallmentSummary();
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
        String amountStr = etAmount.getText().toString().trim();
        if (TextUtils.isEmpty(amountStr)) { etAmount.setError("Informe o valor"); return; }

        double amount;
        try {
            amount = Double.parseDouble(amountStr.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            etAmount.setError("Valor inválido");
            return;
        }
        if (amount <= 0) { etAmount.setError("Valor deve ser maior que zero"); return; }

        String type        = rbReceita.isChecked() ? Transaction.TYPE_RECEITA : Transaction.TYPE_DESPESA;
        String category    = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString() : "Outros";
        String description = etDescription.getText().toString().trim();

        // Verifica se é parcelada
        boolean isInstallment = type.equals(Transaction.TYPE_DESPESA) && cbInstallment.isChecked();
        int installmentCount  = 0;

        if (isInstallment) {
            String cntStr = etInstallmentCount.getText() != null
                    ? etInstallmentCount.getText().toString().trim() : "";
            if (TextUtils.isEmpty(cntStr)) {
                etInstallmentCount.setError("Informe o número de parcelas");
                return;
            }
            try {
                installmentCount = Integer.parseInt(cntStr);
            } catch (NumberFormatException e) {
                etInstallmentCount.setError("Número inválido");
                return;
            }
            if (installmentCount < 2) {
                etInstallmentCount.setError("Mínimo 2 parcelas");
                return;
            }
        }

        // Valor por parcela = total dividido pelo número de parcelas
        double installmentAmount = isInstallment ? amount / installmentCount : amount;

        // Salva a transação (1ª parcela ou transação simples)
        String desc = isInstallment
                ? "Parcela 1/" + installmentCount + (description.isEmpty() ? "" : " - " + description)
                : description;

        Transaction t = new Transaction(installmentAmount, type, category, selectedDate, desc);

        if (editId != -1) {
            t.setId(editId);
            db.transactionDao().update(t);
            Toast.makeText(this, "Transação atualizada!", Toast.LENGTH_SHORT).show();
        } else {
            db.transactionDao().insert(t);

            // Cria registro de parcelamento com 1ª parcela já paga
            if (isInstallment) {
                String title = description.isEmpty() ? category : description;
                Installment inst = new Installment(title, category, installmentAmount, installmentCount, selectedDate);
                inst.setPaidInstallments(1);
                db.installmentDao().insert(inst);
                Toast.makeText(this, "Despesa parcelada registrada! Acompanhe em Planejamento > Parcelas.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Transação salva!", Toast.LENGTH_SHORT).show();
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
