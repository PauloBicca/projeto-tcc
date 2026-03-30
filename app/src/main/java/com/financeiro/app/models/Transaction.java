package com.financeiro.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Modelo de dados para uma transação financeira.
 * Representa receitas e despesas do usuário.
 */
@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Valor da transação (positivo = receita, negativo = despesa) */
    private double amount;

    /** Tipo: "RECEITA" ou "DESPESA" */
    private String type;

    /** Categoria da transação (ex: Alimentação, Transporte) */
    private String category;

    /** Data da transação em milissegundos (timestamp) */
    private long date;

    /** Descrição opcional da transação */
    private String description;

    /** Data de criação do registro */
    private long createdAt;

    // ======================== Constantes de tipo ========================
    public static final String TYPE_RECEITA = "RECEITA";
    public static final String TYPE_DESPESA = "DESPESA";

    // ======================== Construtor ========================
    public Transaction() {
        this.createdAt = System.currentTimeMillis();
    }

    public Transaction(double amount, String type, String category, long date, String description) {
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.date = date;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    // ======================== Getters e Setters ========================
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    /** Retorna true se for despesa */
    public boolean isDespesa() {
        return TYPE_DESPESA.equals(this.type);
    }

    /** Retorna true se for receita */
    public boolean isReceita() {
        return TYPE_RECEITA.equals(this.type);
    }
}
