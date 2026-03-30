package com.financeiro.app.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilitário para gerenciar categorias do app.
 * Fornece categorias padrão e permite categorias personalizadas (via SharedPreferences).
 */
public class CategoryUtils {

    // Categorias padrão de despesas
    public static final String[] DEFAULT_DESPESA_CATEGORIES = {
            "Alimentação",
            "Transporte",
            "Moradia",
            "Saúde",
            "Educação",
            "Lazer",
            "Vestuário",
            "Assinaturas",
            "Tecnologia",
            "Pets",
            "Presentes",
            "Outros"
    };

    // Categorias padrão de receitas
    public static final String[] DEFAULT_RECEITA_CATEGORIES = {
            "Salário",
            "Freelance",
            "Investimentos",
            "Aluguel recebido",
            "Bonificação",
            "Presente recebido",
            "Outros"
    };

    /**
     * Retorna ícone emoji para cada categoria.
     */
    public static String getCategoryEmoji(String category) {
        if (category == null) return "💰";
        switch (category) {
            case "Alimentação":   return "🍔";
            case "Transporte":    return "🚗";
            case "Moradia":       return "🏠";
            case "Saúde":         return "💊";
            case "Educação":      return "📚";
            case "Lazer":         return "🎮";
            case "Vestuário":     return "👕";
            case "Assinaturas":   return "📺";
            case "Tecnologia":    return "💻";
            case "Pets":          return "🐾";
            case "Presentes":     return "🎁";
            case "Salário":       return "💵";
            case "Freelance":     return "💼";
            case "Investimentos": return "📈";
            case "Bonificação":   return "🏆";
            default:              return "💰";
        }
    }

    /**
     * Retorna cor hexadecimal para cada categoria.
     */
    public static int getCategoryColor(String category) {
        if (category == null) return 0xFF9E9E9E;
        switch (category) {
            case "Alimentação":   return 0xFFFF5722;
            case "Transporte":    return 0xFF2196F3;
            case "Moradia":       return 0xFF795548;
            case "Saúde":         return 0xFFE91E63;
            case "Educação":      return 0xFF3F51B5;
            case "Lazer":         return 0xFF9C27B0;
            case "Vestuário":     return 0xFFFF9800;
            case "Assinaturas":   return 0xFF00BCD4;
            case "Tecnologia":    return 0xFF607D8B;
            case "Pets":          return 0xFF8BC34A;
            case "Presentes":     return 0xFFF44336;
            case "Salário":       return 0xFF4CAF50;
            case "Freelance":     return 0xFF009688;
            case "Investimentos": return 0xFF2196F3;
            case "Bonificação":   return 0xFFFFD700;
            default:              return 0xFF9E9E9E;
        }
    }

    /** Retorna todas as categorias de despesa */
    public static List<String> getDespesaCategories() {
        List<String> list = new ArrayList<>();
        for (String c : DEFAULT_DESPESA_CATEGORIES) list.add(c);
        return list;
    }

    /** Retorna todas as categorias de receita */
    public static List<String> getReceitaCategories() {
        List<String> list = new ArrayList<>();
        for (String c : DEFAULT_RECEITA_CATEGORIES) list.add(c);
        return list;
    }
}
