package com.financeiro.app.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utilitário para formatação de moeda e datas.
 */
public class FormatUtils {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(LOCALE_BR);

    // ======================== Moeda ========================

    /**
     * Formata um valor como moeda brasileira (R$ 1.234,56)
     */
    public static String formatCurrency(double value) {
        return CURRENCY_FORMAT.format(value);
    }

    /**
     * Formata valor com sinal de positivo/negativo
     */
    public static String formatCurrencyWithSign(double value) {
        if (value >= 0) {
            return "+ " + formatCurrency(value);
        } else {
            return "- " + formatCurrency(Math.abs(value));
        }
    }

    // ======================== Datas ========================

    /**
     * Formata timestamp para dd/MM/yyyy
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR);
        return sdf.format(new Date(timestamp));
    }

    /**
     * Formata timestamp para dd/MM/yyyy HH:mm
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", LOCALE_BR);
        return sdf.format(new Date(timestamp));
    }

    /**
     * Formata timestamp para "Janeiro 2024"
     */
    public static String formatMonthYear(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", LOCALE_BR);
        String result = sdf.format(new Date(timestamp));
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    /**
     * Retorna timestamp do início do mês atual
     */
    public static long getStartOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Retorna timestamp do fim do mês atual
     */
    public static long getEndOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /**
     * Retorna mês atual (1-12)
     */
    public static int getCurrentMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    /**
     * Retorna ano atual
     */
    public static int getCurrentYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Converte string "dd/MM/yyyy" para timestamp
     */
    public static long parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR);
            Date date = sdf.parse(dateStr);
            return date != null ? date.getTime() : System.currentTimeMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
}
