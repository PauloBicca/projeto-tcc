package com.financeiro.app;

import android.app.Application;

import com.financeiro.app.utils.NotificationUtils;

/**
 * Classe Application principal do app.
 * Inicializa recursos globais como canais de notificação.
 */
public class FinanceiroApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Criar canais de notificação (necessário no Android 8+)
        NotificationUtils.createNotificationChannels(this);
    }
}
