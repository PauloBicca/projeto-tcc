package com.financeiro.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Modelo de dados para uma meta financeira.
 * Ex: "Economizar R$ 1000 para viagem"
 */
@Entity(tableName = "goals")
public class Goal {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Nome/título da meta */
    private String title;

    /** Valor alvo da meta */
    private double targetAmount;

    /** Valor atual acumulado para a meta */
    private double currentAmount;

    /** Descrição opcional */
    private String description;

    /** Data limite (timestamp) - pode ser 0 se não tiver prazo */
    private long deadline;

    /** Data de criação */
    private long createdAt;

    /** Status: "ATIVA" ou "CONCLUIDA" */
    private String status;

    public static final String STATUS_ATIVA = "ATIVA";
    public static final String STATUS_CONCLUIDA = "CONCLUIDA";

    // ======================== Construtor ========================
    public Goal() {
        this.createdAt = System.currentTimeMillis();
        this.status = STATUS_ATIVA;
        this.currentAmount = 0;
    }

    public Goal(String title, double targetAmount, String description, long deadline) {
        this.title = title;
        this.targetAmount = targetAmount;
        this.description = description;
        this.deadline = deadline;
        this.createdAt = System.currentTimeMillis();
        this.status = STATUS_ATIVA;
        this.currentAmount = 0;
    }

    // ======================== Métodos utilitários ========================

    /** Retorna o percentual de progresso (0 a 100) */
    public int getProgressPercent() {
        if (targetAmount <= 0) return 0;
        int percent = (int) ((currentAmount / targetAmount) * 100);
        return Math.min(percent, 100);
    }

    /** Retorna quanto falta para atingir a meta */
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - currentAmount);
    }

    /** Retorna true se a meta foi concluída */
    public boolean isCompleted() {
        return currentAmount >= targetAmount;
    }

    // ======================== Getters e Setters ========================
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }

    public double getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
