package com.financeiro.app.utils;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.financeiro.app.R;
import com.financeiro.app.activities.MainActivity;
import com.financeiro.app.models.Reminder;
import com.financeiro.app.receivers.AlarmReceiver;

import java.util.Calendar;

/**
 * Utilitário para gerenciar notificações e lembretes locais.
 */
public class NotificationUtils {

    public static final String CHANNEL_ID_GERAL = "financeiro_geral";
    public static final String CHANNEL_ID_ALERTAS = "financeiro_alertas";
    public static final String CHANNEL_ID_LEMBRETES = "financeiro_lembretes";

    private static final int NOTIF_BUDGET_BASE = 1000;
    private static final int NOTIF_REMINDER_BASE = 2000;

    // ======================== Canais de Notificação ========================

    /**
     * Cria os canais de notificação (necessário no Android 8+).
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            NotificationChannel channelGeral = new NotificationChannel(
                    CHANNEL_ID_GERAL,
                    "Notificações Gerais",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelGeral.setDescription("Notificações gerais do app");

            NotificationChannel channelAlertas = new NotificationChannel(
                    CHANNEL_ID_ALERTAS,
                    "Alertas de Orçamento",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelAlertas.setDescription("Alertas quando o orçamento está próximo do limite");

            NotificationChannel channelLembretes = new NotificationChannel(
                    CHANNEL_ID_LEMBRETES,
                    "Lembretes",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelLembretes.setDescription("Lembretes para registrar despesas e pagar contas");

            manager.createNotificationChannel(channelGeral);
            manager.createNotificationChannel(channelAlertas);
            manager.createNotificationChannel(channelLembretes);
        }
    }

    // ======================== Envio de Notificações ========================

    /**
     * Envia notificação de alerta de orçamento.
     * @param context contexto
     * @param category categoria do orçamento
     * @param percentUsed percentual usado (ex: 85)
     * @param isOver se verdadeiro, o limite foi ultrapassado
     */
    public static void sendBudgetAlert(Context context, String category, int percentUsed, boolean isOver) {
        String title = isOver ? "⚠️ Orçamento ultrapassado!" : "⚠️ Atenção ao orçamento!";
        String message = isOver
                ? "Você ultrapassou o limite para " + category + "."
                : "Você atingiu " + percentUsed + "% do limite de " + category + ".";

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ALERTAS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_BUDGET_BASE + category.hashCode(), builder.build());
        } catch (SecurityException e) {
            // Permissão não concedida
        }
    }

    /**
     * Envia notificação de lembrete.
     */
    public static void sendReminderNotification(Context context, String title, String message, int notifId) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notifId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_LEMBRETES)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(NOTIF_REMINDER_BASE + notifId, builder.build());
        } catch (SecurityException e) {
            // Permissão não concedida
        }
    }

    // ======================== Agendamento de Alarmes ========================

    /**
     * Agenda um lembrete usando AlarmManager.
     */
    public static void scheduleReminder(Context context, Reminder reminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        String[] parts = reminder.getTime().split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // Se o horário já passou hoje, agenda para amanhã
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("reminder_id", reminder.getId());
        intent.putExtra("title", reminder.getTitle());
        intent.putExtra("message", reminder.getMessage());
        intent.setAction("com.financeiro.ALARM_TRIGGER");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminder.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
            );
        }
    }

    /**
     * Cancela um lembrete agendado.
     */
    public static void cancelReminder(Context context, long reminderId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
