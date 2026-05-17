package com.financeiro.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeiro.app.models.Installment;

import java.util.List;

@Dao
public interface InstallmentDao {

    @Insert
    long insert(Installment installment);

    @Update
    void update(Installment installment);

    @Delete
    void delete(Installment installment);

    @Query("SELECT * FROM installments ORDER BY createdAt DESC")
    List<Installment> getAll();

    @Query("SELECT * FROM installments WHERE status = 'ATIVA' ORDER BY startDate ASC")
    List<Installment> getActive();

    @Query("SELECT * FROM installments WHERE status = 'CONCLUIDA' ORDER BY createdAt DESC")
    List<Installment> getCompleted();

    @Query("SELECT * FROM installments WHERE id = :id")
    Installment getById(long id);
}
