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
import com.financeiro.app.utils.FormatUtils;

import java.util.Calendar;

/**
 * Formulário para criar e editar metas financeiras.
 */
public class GoalFormActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "goal_id";

    private EditText etTitle, etTarget, etCurrent, etDescription;
    private TextView tvDeadline;
    private Button btnSave;
    private AppDatabase db;
    private long deadlineTimestamp = 0;
    private long editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_form);

        db = AppDatabase.getInstance(this);
        editId = getIntent().getLongExtra(EXTRA_GOAL_ID, -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(editId != -1 ? "Editar Meta" : "Nova Meta");
        }

        etTitle       = findViewById(R.id.et_goal_title);
        etTarget      = findViewById(R.id.et_goal_target);
        etCurrent     = findViewById(R.id.et_goal_current);
        etDescription = findViewById(R.id.et_goal_description);
        tvDeadline    = findViewById(R.id.tv_goal_deadline);
        btnSave       = findViewById(R.id.btn_save_goal);

        tvDeadline.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveMeta());

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
        etCurrent.setText(String.valueOf(g.getCurrentAmount()));
        etDescription.setText(g.getDescription());
        deadlineTimestamp = g.getDeadline();
        if (deadlineTimestamp > 0) tvDeadline.setText(FormatUtils.formatDate(deadlineTimestamp));
    }

    private void saveMeta() {
        String title = etTitle.getText().toString().trim();
        String targetStr = etTarget.getText().toString().trim();
        String currentStr = etCurrent.getText().toString().trim();
        String desc = etDescription.getText().toString().trim();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Informe o título"); return; }
        if (TextUtils.isEmpty(targetStr)) { etTarget.setError("Informe o valor alvo"); return; }

        double target, current = 0;
        try {
            target = Double.parseDouble(targetStr.replace(",", "."));
        } catch (NumberFormatException e) { etTarget.setError("Valor inválido"); return; }

        if (!TextUtils.isEmpty(currentStr)) {
            try { current = Double.parseDouble(currentStr.replace(",", ".")); }
            catch (NumberFormatException e) { etCurrent.setError("Valor inválido"); return; }
        }

        Goal g = new Goal(title, target, desc, deadlineTimestamp);
        g.setCurrentAmount(current);

        if (editId != -1) {
            g.setId(editId);
            db.goalDao().update(g);
            Toast.makeText(this, "Meta atualizada!", Toast.LENGTH_SHORT).show();
        } else {
            db.goalDao().insert(g);
            Toast.makeText(this, "Meta criada!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
