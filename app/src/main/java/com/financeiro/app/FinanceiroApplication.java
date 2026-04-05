package com.financeiro.app;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.FixedTransaction;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.NotificationUtils;

import java.util.Calendar;
import java.util.List;

/**
 * Classe Application principal do app.
 * Inicializa recursos globais como canais de notificação e modo escuro.
 */
public class FinanceiroApplication extends Application {

    private static final String PREFS_SETTINGS = "settings_prefs";
    private static final String KEY_DARK_MODE  = "dark_mode";

    @Override
    public void onCreate() {
        super.onCreate();

        // Aplicar modo escuro salvo (deve ser chamado antes de qualquer Activity)
        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK_MODE, false);
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Criar canais de notificação (necessário no Android 8+)
        NotificationUtils.createNotificationChannels(this);

        // Aplicar transações fixas do mês atual, se ainda não aplicadas
        applyFixedTransactionsIfNeeded();
    }

    /**
     * Insere as transações fixas ativas como transações reais no dia 1°
     * do mês corrente, mas apenas uma vez por mês.
     */
    private void applyFixedTransactionsIfNeeded() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year  = cal.get(Calendar.YEAR);
        String key = "fixed_applied_" + year + "_" + month;

        SharedPreferences prefs = getSharedPreferences(PREFS_SETTINGS, MODE_PRIVATE);
        if (prefs.getBoolean(key, false)) return; // Já aplicado neste mês

        AppDatabase db = AppDatabase.getInstance(this);
        List<FixedTransaction> fixedList = db.fixedTransactionDao().getAllActive();

        if (fixedList.isEmpty()) {
            // Marca como aplicado mesmo sem nada, para não checar de novo
            prefs.edit().putBoolean(key, true).apply();
            return;
        }

        // Define data como dia 1 do mês atual, meia-noite
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long date = cal.getTimeInMillis();

        for (FixedTransaction ft : fixedList) {
            String category = ft.getCategory() != null ? ft.getCategory()
                    : (ft.isReceita() ? "Salário" : "Outros");
            String desc = ft.getTitle() + " (fixo)";
            Transaction t = new Transaction(ft.getAmount(), ft.getType(), category, date, desc);
            db.transactionDao().insert(t);
        }

        prefs.edit().putBoolean(key, true).apply();
    }
}
