package com.financeiro.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Modelo de dados para planejamento mensal por categoria.
 * Define um limite de gasto para determinada categoria em determinado mês/ano.
 */
@Entity(tableName = "budgets")
public class Budget {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Categoria associada ao limite (ex: Alimentação) */
    private String category;

    /** Limite de gasto definido pelo usuário */
    private double limitAmount;

    /** Mês do planejamento (1-12) */
    private int month;

    /** Ano do planejamento (ex: 2024) */
    private int year;

    /** Data de criação */
    private long createdAt;

    // ======================== Constantes de status visual ========================
    public static final String STATUS_OK = "OK";           // Verde: dentro do limite
    public static final String STATUS_WARNING = "WARNING"; // Amarelo: >= 80% do limite
    public static final String STATUS_OVER = "OVER";       // Vermelho: ultrapassou

    // ======================== Construtor ========================
    public Budget() {
        this.createdAt = System.currentTimeMillis();
    }

    public Budget(String category, double limitAmount, int month, int year) {
        this.category = category;
        this.limitAmount = limitAmount;
        this.month = month;
        this.year = year;
        this.createdAt = System.currentTimeMillis();
    }

    // ======================== Métodos utilitários ========================

    /**
     * Calcula o status visual com base no valor gasto vs limite.
     * @param spent valor já gasto na categoria
     * @return STATUS_OK, STATUS_WARNING ou STATUS_OVER
     */
    public String getStatus(double spent) {
        if (limitAmount <= 0) return STATUS_OK;
        double percentage = (spent / limitAmount) * 100;
        if (percentage >= 100) return STATUS_OVER;
        if (percentage >= 80) return STATUS_WARNING;
        return STATUS_OK;
    }

    /**
     * Retorna quanto ainda resta no orçamento.
     * @param spent valor já gasto
     */
    public double getRemaining(double spent) {
        return limitAmount - spent;
    }

    /**
     * Retorna o percentual gasto (0-100+).
     * @param spent valor já gasto
     */
    public int getPercentUsed(double spent) {
        if (limitAmount <= 0) return 0;
        return (int) ((spent / limitAmount) * 100);
    }

    // ======================== Getters e Setters ========================
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(double limitAmount) { this.limitAmount = limitAmount; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
