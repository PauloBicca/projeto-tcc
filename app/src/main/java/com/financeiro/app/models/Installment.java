package com.financeiro.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Modelo de compra parcelada.
 * Cada parcela paga gera automaticamente uma transação de DESPESA.
 */
@Entity(tableName = "installments")
public class Installment {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String category;
    private double installmentAmount;  // valor de cada parcela
    private int totalInstallments;     // total de parcelas
    private int paidInstallments;      // parcelas já pagas
    private long startDate;            // data da primeira parcela
    private long createdAt;
    private String status;             // ATIVA ou CONCLUIDA

    public static final String STATUS_ATIVA    = "ATIVA";
    public static final String STATUS_CONCLUIDA = "CONCLUIDA";

    public Installment() {
        this.createdAt       = System.currentTimeMillis();
        this.paidInstallments = 0;
        this.status          = STATUS_ATIVA;
    }

    public Installment(String title, String category, double installmentAmount,
                       int totalInstallments, long startDate) {
        this.title              = title;
        this.category           = category;
        this.installmentAmount  = installmentAmount;
        this.totalInstallments  = totalInstallments;
        this.startDate          = startDate;
        this.paidInstallments   = 0;
        this.status             = STATUS_ATIVA;
        this.createdAt          = System.currentTimeMillis();
    }

    // ── Utilitários ──────────────────────────────────────────────────────────

    public double getTotalAmount()     { return installmentAmount * totalInstallments; }
    public double getRemainingAmount() { return installmentAmount * getRemainingInstallments(); }
    public int    getRemainingInstallments() { return totalInstallments - paidInstallments; }
    public boolean isCompleted()       { return STATUS_CONCLUIDA.equals(status); }

    /** Retorna o timestamp estimado da próxima parcela a pagar. */
    public long getNextDueDate() {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(startDate);
        c.add(java.util.Calendar.MONTH, paidInstallments);
        return c.getTimeInMillis();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public long   getId()                 { return id; }
    public void   setId(long id)          { this.id = id; }

    public String getTitle()              { return title; }
    public void   setTitle(String t)      { this.title = t; }

    public String getCategory()           { return category; }
    public void   setCategory(String c)   { this.category = c; }

    public double getInstallmentAmount()              { return installmentAmount; }
    public void   setInstallmentAmount(double a)      { this.installmentAmount = a; }

    public int  getTotalInstallments()                { return totalInstallments; }
    public void setTotalInstallments(int n)           { this.totalInstallments = n; }

    public int  getPaidInstallments()                 { return paidInstallments; }
    public void setPaidInstallments(int n)            { this.paidInstallments = n; }

    public long getLong_startDate()  { return startDate; }   // evita conflito com Room
    public long getStartDate()       { return startDate; }
    public void setStartDate(long d) { this.startDate = d; }

    public long getCreatedAt()       { return createdAt; }
    public void setCreatedAt(long t) { this.createdAt = t; }

    public String getStatus()        { return status; }
    public void   setStatus(String s){ this.status = s; }
}
