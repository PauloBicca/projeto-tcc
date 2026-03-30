package com.financeiro.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeiro.app.models.Budget;

import java.util.List;

/**
 * DAO para planejamento mensal (orçamentos por categoria).
 */
@Dao
public interface BudgetDao {

    @Insert
    long insert(Budget budget);

    @Update
    void update(Budget budget);

    @Delete
    void delete(Budget budget);

    @Query("DELETE FROM budgets WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM budgets ORDER BY year DESC, month DESC")
    List<Budget> getAllBudgets();

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY category ASC")
    List<Budget> getBudgetsByMonthYear(int month, int year);

    @Query("SELECT * FROM budgets WHERE category = :category AND month = :month AND year = :year LIMIT 1")
    Budget getBudgetByCategoryAndMonthYear(String category, int month, int year);

    @Query("SELECT * FROM budgets WHERE id = :id")
    Budget getBudgetById(long id);
}
