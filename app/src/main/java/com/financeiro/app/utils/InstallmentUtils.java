package com.financeiro.app.utils;

import android.content.Context;

import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Installment;
import com.financeiro.app.models.Transaction;

import java.util.List;

/**
 * Verifica parcelas vencidas e gera as transações de despesa automaticamente.
 * Deve ser chamado ao abrir o app (MainActivity.onCreate).
 */
public class InstallmentUtils {

    public static void autoChargeDueInstallments(Context context) {
        AppDatabase db   = AppDatabase.getInstance(context);
        List<Installment> active = db.installmentDao().getActive();
        long now = System.currentTimeMillis();

        for (Installment inst : active) {
            // Cobra todas as parcelas cujo vencimento já passou
            while (!inst.isCompleted() && inst.getNextDueDate() <= now) {
                int parcelaAtual = inst.getPaidInstallments() + 1;

                Transaction t = new Transaction(
                        inst.getInstallmentAmount(),
                        Transaction.TYPE_DESPESA,
                        inst.getCategory(),
                        inst.getNextDueDate(),
                        "Parcela " + parcelaAtual + "/" + inst.getTotalInstallments()
                                + " - " + inst.getTitle()
                );
                db.transactionDao().insert(t);

                int newPaid = inst.getPaidInstallments() + 1;
                inst.setPaidInstallments(newPaid);
                if (newPaid >= inst.getTotalInstallments()) {
                    inst.setStatus(Installment.STATUS_CONCLUIDA);
                }
                db.installmentDao().update(inst);
            }
        }
    }
}
