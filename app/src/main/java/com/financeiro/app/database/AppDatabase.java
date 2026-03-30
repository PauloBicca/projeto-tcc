package com.financeiro.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.financeiro.app.models.Budget;
import com.financeiro.app.models.Goal;
import com.financeiro.app.models.Reminder;
import com.financeiro.app.models.Transaction;

/**
 * Banco de dados principal do app usando Room.
 * Singleton para garantir uma única instância em todo o app.
 */
@Database(
        entities = {Transaction.class, Goal.class, Budget.class, Reminder.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "financeiro_db";
    private static volatile AppDatabase INSTANCE;

    // ======================== DAOs ========================
    public abstract TransactionDao transactionDao();
    public abstract GoalDao goalDao();
    public abstract BudgetDao budgetDao();
    public abstract ReminderDao reminderDao();

    // ======================== Singleton ========================

    /**
     * Retorna a instância única do banco de dados.
     * Thread-safe usando double-checked locking.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .fallbackToDestructiveMigration() // Recria o banco em migração sem script
                            .allowMainThreadQueries() // Para simplicidade; em produção use AsyncTask/Thread
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
