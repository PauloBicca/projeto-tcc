package com.financeiro.app.ai;

import android.content.Context;

import com.financeiro.app.BuildConfig;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Budget;
import com.financeiro.app.models.Goal;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.FormatUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiService {

    private static final String API_KEY = BuildConfig.GROQ_API_KEY;
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";
    private static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public interface TipsCallback {
        void onSuccess(String tips);
        void onError(String error);
    }

    private final Context context;
    private final OkHttpClient httpClient;

    public GeminiService(Context context) {
        this.context = context.getApplicationContext();
        this.httpClient = new OkHttpClient();
    }

    public void generateTips(TipsCallback callback) {
        String prompt = buildPrompt();

        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject body = new JSONObject();
            body.put("model", MODEL);
            body.put("messages", messages);

            RequestBody requestBody = RequestBody.create(body.toString(), JSON_TYPE);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(requestBody)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Sem conexão com a internet.");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);

                        if (!response.isSuccessful()) {
                            String msg = json.optJSONObject("error") != null
                                    ? json.getJSONObject("error").optString("message", "Erro desconhecido")
                                    : "Erro " + response.code();
                            callback.onError("Erro da API: " + msg);
                            return;
                        }

                        String text = json
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        callback.onSuccess(text);
                    } catch (Exception e) {
                        callback.onError("Erro ao processar resposta da IA.");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Erro ao montar requisição.");
        }
    }

    private String buildPrompt() {
        AppDatabase db = AppDatabase.getInstance(context);

        long start = FormatUtils.getStartOfCurrentMonth();
        long end = FormatUtils.getEndOfCurrentMonth();
        int month = FormatUtils.getCurrentMonth();
        int year = FormatUtils.getCurrentYear();
        String monthStr = String.format("%02d", month);
        String yearStr = String.valueOf(year);

        double totalBalance = db.transactionDao().getTotalBalance();
        double monthlyIncome = db.transactionDao().getTotalReceitasByPeriod(start, end);
        double monthlyExpenses = db.transactionDao().getTotalDespesasByPeriod(start, end);

        // Despesas agrupadas por categoria
        List<Transaction> monthTransactions = db.transactionDao().getTransactionsByMonthYear(monthStr, yearStr);
        Map<String, Double> categoryExpenses = new HashMap<>();
        for (Transaction t : monthTransactions) {
            if (t.isDespesa()) {
                String cat = t.getCategory() != null ? t.getCategory() : "Outros";
                categoryExpenses.put(cat, categoryExpenses.getOrDefault(cat, 0.0) + t.getAmount());
            }
        }

        List<Goal> goals = db.goalDao().getAllGoals();
        List<Budget> budgets = db.budgetDao().getBudgetsByMonthYear(month, year);

        StringBuilder sb = new StringBuilder();
        sb.append("Você é um assistente financeiro pessoal. Analise os dados abaixo e forneça ")
          .append("exatamente 5 dicas práticas e personalizadas para melhorar a saúde financeira ")
          .append("desta pessoa. Baseie cada dica nos dados reais. Seja direto e use linguagem simples. ")
          .append("Responda em português do Brasil.\n\n");

        sb.append("=== DADOS DO MÊS ATUAL ===\n");
        sb.append(String.format("Saldo total acumulado: R$ %.2f\n", totalBalance));
        sb.append(String.format("Receitas do mês: R$ %.2f\n", monthlyIncome));
        sb.append(String.format("Despesas do mês: R$ %.2f\n", monthlyExpenses));
        sb.append(String.format("Resultado do mês: R$ %.2f\n\n", monthlyIncome - monthlyExpenses));

        if (!categoryExpenses.isEmpty()) {
            sb.append("Despesas por categoria:\n");
            for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
                sb.append(String.format("  - %s: R$ %.2f\n", entry.getKey(), entry.getValue()));
            }
            sb.append("\n");
        } else {
            sb.append("Nenhuma despesa registrada este mês.\n\n");
        }

        if (!goals.isEmpty()) {
            sb.append("Metas financeiras ativas:\n");
            for (Goal g : goals) {
                sb.append(String.format("  - %s: R$ %.2f acumulado de R$ %.2f (%.0f%%)\n",
                        g.getTitle(), g.getCurrentAmount(), g.getTargetAmount(),
                        g.getTargetAmount() > 0 ? (g.getCurrentAmount() / g.getTargetAmount()) * 100 : 0));
            }
            sb.append("\n");
        }

        if (!budgets.isEmpty()) {
            sb.append("Orçamentos definidos:\n");
            for (Budget b : budgets) {
                double spent = db.transactionDao().getDespesasByCategoryAndPeriod(b.getCategory(), start, end);
                sb.append(String.format("  - %s: gasto R$ %.2f de R$ %.2f permitidos\n",
                        b.getCategory(), spent, b.getLimitAmount()));
            }
            sb.append("\n");
        }

        sb.append("Forneça as 5 dicas numeradas (1. 2. 3. 4. 5.) com base nesses dados.");
        return sb.toString();
    }
}
