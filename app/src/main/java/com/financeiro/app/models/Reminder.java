package com.financeiro.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Modelo de dados para lembretes locais.
 * Representa notificações agendadas pelo usuário.
 */
@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Título do lembrete */
    private String title;

    /** Mensagem do lembrete */
    private String message;

    /** Hora agendada em formato HH:mm */
    private String time;

    /** Dias da semana (bitmask): 1=Dom, 2=Seg, 4=Ter, 8=Qua, 16=Qui, 32=Sex, 64=Sab */
    private int daysOfWeek;

    /** Se o lembrete está ativo */
    private boolean active;

    /** Tipo: "DESPESA" ou "CONTA" */
    private String type;

    /** Data de criação */
    private long createdAt;

    public static final String TYPE_DESPESA = "DESPESA";
    public static final String TYPE_CONTA = "CONTA";

    // ======================== Construtor ========================
    public Reminder() {
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public Reminder(String title, String message, String time, int daysOfWeek, String type) {
        this.title = title;
        this.message = message;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
        this.type = type;
        this.active = true;
        this.createdAt = System.currentTimeMillis();
    }

    // ======================== Getters e Setters ========================
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(int daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
