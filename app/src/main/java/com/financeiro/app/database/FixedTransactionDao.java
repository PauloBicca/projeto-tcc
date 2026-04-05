package com.financeiro.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeiro.app.models.FixedTransaction;

import java.util.List;

/**
 * DAO para transações fixas mensais.
 */
@Dao
public interface FixedTransactionDao {

    @Insert
    long insert(FixedTransaction t);

    @Update
    void update(FixedTransaction t);

    @Delete
    void delete(FixedTransaction t);

    @Query("SELECT * FROM fixed_transactions ORDER BY type ASC, title ASC")
    List<FixedTransaction> getAll();

    @Query("SELECT * FROM fixed_transactions WHERE active = 1 ORDER BY type ASC, title ASC")
    List<FixedTransaction> getAllActive();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM fixed_transactions WHERE type = 'RECEITA' AND active = 1")
    double getTotalFixedReceitas();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM fixed_transactions WHERE type = 'DESPESA' AND active = 1")
    double getTotalFixedDespesas();
}
