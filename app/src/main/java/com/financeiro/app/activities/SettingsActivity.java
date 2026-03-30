package com.financeiro.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeiro.app.R;
import com.financeiro.app.adapters.ReminderAdapter;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Reminder;
import com.financeiro.app.utils.NotificationUtils;
import com.financeiro.app.utils.PinManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Tela de configurações: tema, PIN, lembretes.
 */
public class SettingsActivity extends AppCompatActivity implements ReminderAdapter.OnReminderListener {

    private static final String PREFS_SETTINGS = "settings_prefs";
    private static final String KEY_DARK_MODE  = "dark_mode";

    private Switch switchDarkMode;
    private Button btnChangePIN;
    private RecyclerView rvReminders;
    private FloatingActionButton fabAddReminder;
    private AppDatabase db;
    private PinManager pinManager;
    private ReminderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        db         = AppDatabase.getInstance(this);
        pinManager = new PinManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Configurações");
        }

        switchDarkMode  = findViewById(R.id.switch_dark_mode);
        btnChangePIN    = findViewById(R.id.btn_change_pin);
        rvReminders     = findViewById(R.id.rv_reminders);
        fabAddReminder  = findViewById(R.id.fab_add_reminder);

        // Tema
        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        switchDarkMode.setChecked(isDark);
        switchDarkMode.setOnCheckedChangeListener((btn, checked) -> {
            prefs.edit().putBoolean(KEY_DARK_MODE, checked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    checked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Alterar PIN
        btnChangePIN.setOnClickListener(v -> {
            pinManager.removePin();
            startActivity(new Intent(this, SetupPinActivity.class));
        });

        // Lembretes
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
        loadReminders();

        fabAddReminder.setOnClickListener(v -> showAddReminderDialog());
    }

    private void loadReminders() {
        List<Reminder> reminders = db.reminderDao().getAllReminders();
        adapter = new ReminderAdapter(reminders, this);
        rvReminders.setAdapter(adapter);
    }

    private void showAddReminderDialog() {
        // Dialog simples para adicionar lembrete rápido
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_add_reminder, null);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Novo Lembrete")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    android.widget.EditText etTitle   = dialogView.findViewById(R.id.et_reminder_title);
                    android.widget.EditText etMsg     = dialogView.findViewById(R.id.et_reminder_message);
                    android.widget.EditText etTime    = dialogView.findViewById(R.id.et_reminder_time);

                    String title = etTitle.getText().toString().trim();
                    String msg   = etMsg.getText().toString().trim();
                    String time  = etTime.getText().toString().trim();

                    if (title.isEmpty() || time.isEmpty()) {
                        Toast.makeText(this, "Preencha título e horário", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!time.matches("\\d{2}:\\d{2}")) {
                        Toast.makeText(this, "Horário inválido (use HH:MM)", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Reminder r = new Reminder(title, msg.isEmpty() ? title : msg, time, 127, Reminder.TYPE_DESPESA);
                    long id = db.reminderDao().insert(r);
                    r.setId(id);
                    NotificationUtils.scheduleReminder(this, r);
                    loadReminders();
                    Toast.makeText(this, "Lembrete criado!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onToggle(Reminder reminder, boolean isActive) {
        db.reminderDao().setActive(reminder.getId(), isActive);
        if (isActive) {
            NotificationUtils.scheduleReminder(this, reminder);
        } else {
            NotificationUtils.cancelReminder(this, reminder.getId());
        }
    }

    @Override
    public void onDelete(Reminder reminder) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir lembrete?")
                .setMessage("Deseja remover \"" + reminder.getTitle() + "\"?")
                .setPositiveButton("Excluir", (d, w) -> {
                    NotificationUtils.cancelReminder(this, reminder.getId());
                    db.reminderDao().delete(reminder);
                    loadReminders();
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
