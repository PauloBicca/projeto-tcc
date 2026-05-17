package com.financeiro.app.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.financeiro.app.models.Budget;
import com.financeiro.app.models.FixedTransaction;
import com.financeiro.app.models.Goal;
import com.financeiro.app.models.Installment;
import com.financeiro.app.models.Reminder;
import com.financeiro.app.models.Transaction;

/**
 * Banco de dados principal do app usando Room.
 * Singleton para garantir uma única instância em todo o app.
 */
@Database(
        entities = {Transaction.class, Goal.class, Budget.class, Reminder.class, FixedTransaction.class, Installment.class},
        version = 3,
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
    public abstract FixedTransactionDao fixedTransactionDao();
    public abstract InstallmentDao installmentDao();

    // ======================== Migrações ========================

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `fixed_transactions` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT, " +
                "`amount` REAL NOT NULL, " +
                "`type` TEXT, " +
                "`category` TEXT, " +
                "`active` INTEGER NOT NULL DEFAULT 1, " +
                "`createdAt` INTEGER NOT NULL DEFAULT 0)"
            );
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `installments` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`title` TEXT, " +
                "`category` TEXT, " +
                "`installmentAmount` REAL NOT NULL DEFAULT 0, " +
                "`totalInstallments` INTEGER NOT NULL DEFAULT 0, " +
                "`paidInstallments` INTEGER NOT NULL DEFAULT 0, " +
                "`startDate` INTEGER NOT NULL DEFAULT 0, " +
                "`createdAt` INTEGER NOT NULL DEFAULT 0, " +
                "`status` TEXT)"
            );
        }
    };

    // ======================== Singleton ========================

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
