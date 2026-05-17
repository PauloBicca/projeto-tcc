package com.financeiro.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Goal;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.FormatUtils;

import java.util.Calendar;

/**
 * Formulário para criar e editar metas financeiras.
 *
 * Ao adicionar ou aumentar o valor guardado em uma meta, a diferença é
 * debitada automaticamente do saldo como uma DESPESA (categoria "Poupança").
 * Se o valor for reduzido, a diferença volta ao saldo como RECEITA.
 */
public class GoalFormActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "goal_id";

    private EditText etTitle, etTarget, etCurrent, etDescription;
    private TextView tvDeadline, tvAvailableBalance;
    private Button btnSave;
    private AppDatabase db;
    private long deadlineTimestamp = 0;
    private long editId = -1;

    /** Valor guardado na meta antes da edição (para calcular o delta) */
    private double previousCurrentAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_form);

        db     = AppDatabase.getInstance(this);
        editId = getIntent().getLongExtra(EXTRA_GOAL_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(editId != -1 ? "Editar Meta" : "Nova Meta");
        }

        etTitle            = findViewById(R.id.et_goal_title);
        etTarget           = findViewById(R.id.et_goal_target);
        etCurrent          = findViewById(R.id.et_goal_current);
        etDescription      = findViewById(R.id.et_goal_description);
        tvDeadline         = findViewById(R.id.tv_goal_deadline);
        tvAvailableBalance = findViewById(R.id.tv_available_balance);
        btnSave            = findViewById(R.id.btn_save_goal);

        tvDeadline.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveMeta());

        // Exibe saldo disponível
        double balance = db.transactionDao().getTotalBalance();
        tvAvailableBalance.setText(FormatUtils.formatCurrency(balance));
        if (balance < 0) {
            tvAvailableBalance.setTextColor(
                    androidx.core.content.ContextCompat.getColor(this, R.color.expense_color));
        }

        if (editId != -1) loadGoal(editId);
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            cal.set(year, month, day);
            deadlineTimestamp = cal.getTimeInMillis();
            tvDeadline.setText(FormatUtils.formatDate(deadlineTimestamp));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadGoal(long id) {
        Goal g = db.goalDao().getGoalById(id);
        if (g == null) return;
        etTitle.setText(g.getTitle());
        etTarget.setText(String.valueOf(g.getTargetAmount()));
        // etCurrent representa quanto adicionar agora; deixa em branco no modo edição
        etDescription.setText(g.getDescription());
        deadlineTimestamp = g.getDeadline();
        previousCurrentAmount = g.getCurrentAmount();
        if (deadlineTimestamp > 0) tvDeadline.setText(FormatUtils.formatDate(deadlineTimestamp));
    }

    private void saveMeta() {
        String title      = etTitle.getText().toString().trim();
        String targetStr  = etTarget.getText().toString().trim();
        String currentStr = etCurrent.getText().toString().trim();
        String desc       = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title))     { etTitle.setError("Informe o título"); return; }
        if (TextUtils.isEmpty(targetStr)) { etTarget.setError("Informe o valor alvo"); return; }

        double target, delta = 0;
        try {
            target = Double.parseDouble(targetStr.replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) { etTarget.setError("Valor inválido"); return; }

        if (!TextUtils.isEmpty(currentStr)) {
            try { delta = Double.parseDouble(currentStr.replace(".", "").replace(",", ".")); }
            catch (NumberFormatException e) { etCurrent.setError("Valor inválido"); return; }
            if (delta < 0) { etCurrent.setError("Valor deve ser positivo"); return; }
        }

        // Valida se há saldo suficiente para adicionar à meta
        if (delta > 0.001) {
            double balance = db.transactionDao().getTotalBalance();
            if (delta > balance) {
                etCurrent.setError("Saldo insuficiente. Disponível: " + FormatUtils.formatCurrency(balance));
                return;
            }
        }

        double newCurrentAmount = previousCurrentAmount + delta;

        Goal g = new Goal(title, target, desc, deadlineTimestamp);
        g.setCurrentAmount(newCurrentAmount);
        if (newCurrentAmount >= target) g.setStatus(Goal.STATUS_CONCLUIDA);

        if (editId != -1) {
            g.setId(editId);
            db.goalDao().update(g);
        } else {
            db.goalDao().insert(g);
        }

        // Debita do saldo se o usuário guardou algum valor
        if (delta > 0.001) {
            registrarTransacaoMeta(title, delta);
        }

        String msg = editId != -1 ? "Meta atualizada!" : "Meta criada!";
        if (delta > 0.001) {
            msg += "\n" + FormatUtils.formatCurrency(delta) + " debitados do saldo.";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Registra a movimentação do valor da meta como transação real.
     *
     * @param goalTitle nome da meta
     * @param delta     positivo = guardou dinheiro (DESPESA no saldo)
     *                  negativo = retirou dinheiro (RECEITA no saldo)
     */
    private void registrarTransacaoMeta(String goalTitle, double delta) {
        String type, descPrefix;
        if (delta > 0) {
            type       = Transaction.TYPE_DESPESA;
            descPrefix = "Guardado para meta: ";
        } else {
            type       = Transaction.TYPE_RECEITA;
            descPrefix = "Retirado da meta: ";
        }

        Transaction t = new Transaction(
                Math.abs(delta),
                type,
                "Poupança",
                System.currentTimeMillis(),
                descPrefix + goalTitle
        );
        db.transactionDao().insert(t);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
