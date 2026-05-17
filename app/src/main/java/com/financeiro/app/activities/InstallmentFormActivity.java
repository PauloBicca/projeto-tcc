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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Installment;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.List;

public class InstallmentFormActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etAmount, etTotal;
    private Spinner spinnerCategory;
    private TextView tvStartDate, tvSummaryTotal, tvSummaryLast;
    private MaterialCardView cardSummary;
    private Button btnSave;
    private AppDatabase db;
    private long startDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installment_form);

        db        = AppDatabase.getInstance(this);
        startDate = System.currentTimeMillis();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Nova Compra Parcelada");
        }

        etTitle         = findViewById(R.id.et_installment_title);
        etAmount        = findViewById(R.id.et_installment_amount);
        etTotal         = findViewById(R.id.et_installment_total);
        spinnerCategory = findViewById(R.id.spinner_installment_category);
        tvStartDate     = findViewById(R.id.tv_installment_start_date);
        tvSummaryTotal  = findViewById(R.id.tv_summary_total);
        tvSummaryLast   = findViewById(R.id.tv_summary_last);
        cardSummary     = findViewById(R.id.card_summary);
        btnSave         = findViewById(R.id.btn_save_installment);

        // Spinner de categorias
        List<String> cats = CategoryUtils.getDespesaCategories();
        spinnerCategory.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cats));

        tvStartDate.setText(FormatUtils.formatDate(startDate));
        tvStartDate.setOnClickListener(v -> showDatePicker());

        // Atualiza resumo conforme digita
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int b, int c) { updateSummary(); }
            public void afterTextChanged(Editable s) {}
        };
        etAmount.addTextChangedListener(watcher);
        etTotal.addTextChangedListener(watcher);

        btnSave.setOnClickListener(v -> save());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(startDate);
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            startDate = cal.getTimeInMillis();
            tvStartDate.setText(FormatUtils.formatDate(startDate));
            updateSummary();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateSummary() {
        String amtStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String totStr = etTotal.getText()  != null ? etTotal.getText().toString().trim()  : "";

        if (amtStr.isEmpty() || totStr.isEmpty()) {
            cardSummary.setVisibility(View.GONE);
            return;
        }

        try {
            double amt = Double.parseDouble(amtStr.replace(".", "").replace(",", "."));
            int    tot = Integer.parseInt(totStr);
            if (amt <= 0 || tot <= 0) { cardSummary.setVisibility(View.GONE); return; }

            cardSummary.setVisibility(View.VISIBLE);
            tvSummaryTotal.setText("Total: " + FormatUtils.formatCurrency(amt * tot)
                    + " em " + tot + "x de " + FormatUtils.formatCurrency(amt));

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(startDate);
            cal.add(Calendar.MONTH, tot - 1);
            tvSummaryLast.setText("Última parcela: " + FormatUtils.formatMonthYear(cal.getTimeInMillis()));
        } catch (NumberFormatException e) {
            cardSummary.setVisibility(View.GONE);
        }
    }

    private void save() {
        String title  = etTitle.getText()  != null ? etTitle.getText().toString().trim()  : "";
        String amtStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String totStr = etTotal.getText()  != null ? etTotal.getText().toString().trim()  : "";

        if (TextUtils.isEmpty(title))  { etTitle.setError("Informe o nome");    return; }
        if (TextUtils.isEmpty(amtStr)) { etAmount.setError("Informe o valor");  return; }
        if (TextUtils.isEmpty(totStr)) { etTotal.setError("Informe as parcelas"); return; }

        double amount;
        int    total;
        try {
            amount = Double.parseDouble(amtStr.replace(".", "").replace(",", "."));
            total  = Integer.parseInt(totStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Valor inválido");
            return;
        }

        if (amount <= 0) { etAmount.setError("Valor deve ser maior que zero"); return; }
        if (total  <= 0) { etTotal.setError("Deve ter ao menos 1 parcela");    return; }

        String category = spinnerCategory.getSelectedItem().toString();
        Installment inst = new Installment(title, category, amount, total, startDate);
        db.installmentDao().insert(inst);

        Toast.makeText(this, "Compra parcelada registrada!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
