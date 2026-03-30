package com.financeiro.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeiro.app.models.Reminder;

import java.util.List;

/**
 * DAO para lembretes/notificações agendadas.
 */
@Dao
public interface ReminderDao {

    @Insert
    long insert(Reminder reminder);

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("DELETE FROM reminders WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    List<Reminder> getAllReminders();

    @Query("SELECT * FROM reminders WHERE active = 1 ORDER BY createdAt DESC")
    List<Reminder> getActiveReminders();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(long id);

    @Query("UPDATE reminders SET active = :active WHERE id = :id")
    void setActive(long id, boolean active);
}
