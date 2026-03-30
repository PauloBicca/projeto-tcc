package com.financeiro.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeiro.app.models.Transaction;

import java.util.List;

/**
 * DAO (Data Access Object) para transações financeiras.
 * Define todas as operações de banco de dados para a tabela transactions.
 */
@Dao
public interface TransactionDao {

    // ======================== Inserção ========================

    @Insert
    long insert(Transaction transaction);

    // ======================== Atualização ========================

    @Update
    void update(Transaction transaction);

    // ======================== Exclusão ========================

    @Delete
    void delete(Transaction transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);

    // ======================== Consultas ========================

    /** Retorna todas as transações ordenadas por data (mais recente primeiro) */
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    List<Transaction> getAllTransactions();

    /** Retorna transações de um período específico */
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Transaction> getTransactionsByPeriod(long startDate, long endDate);

    /** Retorna transações de um mês/ano específico */
    @Query("SELECT * FROM transactions WHERE strftime('%m', datetime(date/1000, 'unixepoch')) = :month " +
            "AND strftime('%Y', datetime(date/1000, 'unixepoch')) = :year ORDER BY date DESC")
    List<Transaction> getTransactionsByMonthYear(String month, String year);

    /** Retorna transações por categoria */
    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY date DESC")
    List<Transaction> getTransactionsByCategory(String category);

    /** Retorna transações por tipo (RECEITA ou DESPESA) */
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    List<Transaction> getTransactionsByType(String type);

    /** Retorna transações filtradas por categoria e período */
    @Query("SELECT * FROM transactions WHERE category = :category AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Transaction> getTransactionsByCategoryAndPeriod(String category, long startDate, long endDate);

    /** Soma total de receitas de um período */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'RECEITA' AND date BETWEEN :startDate AND :endDate")
    double getTotalReceitasByPeriod(long startDate, long endDate);

    /** Soma total de despesas de um período */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'DESPESA' AND date BETWEEN :startDate AND :endDate")
    double getTotalDespesasByPeriod(long startDate, long endDate);

    /** Saldo total (receitas - despesas) */
    @Query("SELECT COALESCE(SUM(CASE WHEN type = 'RECEITA' THEN amount ELSE -amount END), 0) FROM transactions")
    double getTotalBalance();

    /** Soma de despesas por categoria em um período */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'DESPESA' AND category = :category AND date BETWEEN :startDate AND :endDate")
    double getDespesasByCategoryAndPeriod(String category, long startDate, long endDate);

    /** Retorna categorias distintas usadas */
    @Query("SELECT DISTINCT category FROM transactions ORDER BY category ASC")
    List<String> getDistinctCategories();

    /** Retorna um transação por ID */
    @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(long id);
}
