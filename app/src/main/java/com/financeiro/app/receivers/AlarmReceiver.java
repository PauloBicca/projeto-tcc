package com.financeiro.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Reminder;
import com.financeiro.app.utils.NotificationUtils;

import java.util.List;

/**
 * BroadcastReceiver para receber alarmes agendados e disparos no boot.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Reagendar todos os lembretes ativos após reinicialização do dispositivo
            reagendarLembretes(context);
        } else if ("com.financeiro.ALARM_TRIGGER".equals(action)) {
            // Disparar notificação do lembrete
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");
            int notifId = (int) intent.getLongExtra("reminder_id", 0);
            NotificationUtils.sendReminderNotification(context, title, message, notifId);
        }
    }

    /**
     * Reagenda todos os lembretes ativos após boot do dispositivo.
     */
    private void reagendarLembretes(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        List<Reminder> ativos = db.reminderDao().getActiveReminders();
        for (Reminder r : ativos) {
            NotificationUtils.scheduleReminder(context, r);
        }
    }
}
