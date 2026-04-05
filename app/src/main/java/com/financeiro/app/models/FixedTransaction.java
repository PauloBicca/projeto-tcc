package com.financeiro.app.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Modelo para transações fixas (receitas/despesas recorrentes mensais).
 * Ex: Salário, Aluguel, Netflix.
 */
@Entity(tableName = "fixed_transactions")
public class FixedTransaction {

    @PrimaryKey(autoGenerate = true)
    private long id;

    /** Nome da transação fixa (ex: "Salário", "Aluguel") */
    private String title;

    /** Valor mensal fixo */
    private double amount;

    /** Tipo: "RECEITA" ou "DESPESA" */
    private String type;

    /** Categoria */
    private String category;

    /** Se está ativa (será aplicada mensalmente) */
    private boolean active;

    /** Data de criação */
    private long createdAt;

    public static final String TYPE_RECEITA = "RECEITA";
    public static final String TYPE_DESPESA = "DESPESA";

    public FixedTransaction() {
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    @Ignore
    public FixedTransaction(String title, double amount, String type, String category) {
        this.title = title;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.createdAt = System.currentTimeMillis();
        this.active = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isReceita() { return TYPE_RECEITA.equals(type); }
    public boolean isDespesa() { return TYPE_DESPESA.equals(type); }
}
