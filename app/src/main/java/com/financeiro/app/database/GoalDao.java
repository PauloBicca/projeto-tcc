package com.financeiro.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeiro.app.models.Goal;

import java.util.List;

/**
 * DAO para metas financeiras.
 */
@Dao
public interface GoalDao {

    @Insert
    long insert(Goal goal);

    @Update
    void update(Goal goal);

    @Delete
    void delete(Goal goal);

    @Query("DELETE FROM goals WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM goals ORDER BY createdAt DESC")
    List<Goal> getAllGoals();

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY createdAt DESC")
    List<Goal> getGoalsByStatus(String status);

    @Query("SELECT * FROM goals WHERE id = :id")
    Goal getGoalById(long id);
}
