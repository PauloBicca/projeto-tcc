package com.financeiro.app.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Budget;
import com.financeiro.app.models.Goal;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment de Relatório: consolida gráficos, comparativo de economia,
 * metas atingidas e planejamento do mês para autoavaliação.
 */
public class ReportFragment extends Fragment {

    private PieChart pieChart;
    private LineChart lineChart;
    private BarChart barChart;
    private TextView tvPieEmpty;
    private TextView tvReportMonth;
    private TextView tvBestMonth;
    private TextView tvVsPrevious;
    private LinearLayout llGoalsContainer;
    private LinearLayout llBudgetContainer;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = AppDatabase.getInstance(requireContext());

        pieChart         = view.findViewById(R.id.pie_chart);
        lineChart        = view.findViewById(R.id.line_chart);
        barChart         = view.findViewById(R.id.bar_chart);
        tvPieEmpty       = view.findViewById(R.id.tv_pie_empty);
        tvReportMonth    = view.findViewById(R.id.tv_report_month);
        tvBestMonth      = view.findViewById(R.id.tv_best_month);
        tvVsPrevious     = view.findViewById(R.id.tv_vs_previous);
        llGoalsContainer = view.findViewById(R.id.ll_goals_container);
        llBudgetContainer = view.findViewById(R.id.ll_budget_container);

        tvReportMonth.setText(FormatUtils.formatMonthYear(System.currentTimeMillis()));

        setupPieChart();
        setupComparativo();
        loadGoals();
        loadBudgets();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupPieChart();
        setupComparativo();
        loadGoals();
        loadBudgets();
    }

    // =====================================================================
    // SEÇÃO 1 — Gasto Mensal por Categoria (Gráfico de Pizza)
    // =====================================================================

    private void setupPieChart() {
        int textColor = ContextCompat.getColor(requireContext(), R.color.text_primary);

        long start = FormatUtils.getStartOfCurrentMonth();
        long end   = FormatUtils.getEndOfCurrentMonth();
        List<Transaction> all = db.transactionDao().getTransactionsByPeriod(start, end);

        Map<String, Float> totals = new HashMap<>();
        for (Transaction t : all) {
            if (Transaction.TYPE_DESPESA.equals(t.getType())) {
                String cat = t.getCategory();
                totals.put(cat, totals.getOrDefault(cat, 0f) + (float) t.getAmount());
            }
        }

        if (totals.isEmpty()) {
            pieChart.setVisibility(View.GONE);
            tvPieEmpty.setVisibility(View.VISIBLE);
            return;
        }

        pieChart.setVisibility(View.VISIBLE);
        tvPieEmpty.setVisibility(View.GONE);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        for (Map.Entry<String, Float> e : totals.entrySet()) {
            entries.add(new PieEntry(e.getValue(), e.getKey()));
            colors.add(CategoryUtils.getCategoryColor(e.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setSliceSpace(3f);
        dataSet.setValueFormatter(new PercentFormatter(pieChart));

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(38f);
        pieChart.setTransparentCircleRadius(43f);
        pieChart.setDescription(null);
        pieChart.setCenterText("Gastos\ndo Mês");
        pieChart.setCenterTextSize(13f);
        pieChart.setCenterTextColor(textColor);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.getLegend().setTextColor(textColor);
        pieChart.getLegend().setTextSize(11f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    // =====================================================================
    // SEÇÃO 2 — Comparativo com os Meses Anteriores
    // =====================================================================

    private void setupComparativo() {
        int textColor  = ContextCompat.getColor(requireContext(), R.color.text_primary);
        int colorGreen = ContextCompat.getColor(requireContext(), R.color.income_color);
        int colorBlue  = 0xFF2196F3;
        int colorGray  = ContextCompat.getColor(requireContext(), R.color.gray);

        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM", new Locale("pt", "BR"));
        Calendar cal = Calendar.getInstance();

        float[]  balances   = new float[6];
        int[]    counts     = new int[6];
        String[] labels     = new String[6];

        List<Entry> lineEntries = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.MONTH, -i);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);  c.set(Calendar.MINUTE, 0);  c.set(Calendar.SECOND, 0);
            long start = c.getTimeInMillis();
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            c.set(Calendar.HOUR_OF_DAY, 23); c.set(Calendar.MINUTE, 59); c.set(Calendar.SECOND, 59);
            long end = c.getTimeInMillis();

            int idx = 5 - i;
            double rec  = db.transactionDao().getTotalReceitasByPeriod(start, end);
            double desp = db.transactionDao().getTotalDespesasByPeriod(start, end);
            balances[idx] = (float) (rec - desp);
            counts[idx]   = db.transactionDao().countDespesasByPeriod(start, end);
            labels[idx]   = capitalize(sdfMonth.format(c.getTime()));
            lineEntries.add(new Entry(idx, balances[idx]));
        }

        // --- LineChart (evolução de saldo) ---
        LineDataSet lineDataSet = new LineDataSet(lineEntries, "Saldo Mensal (R$)");
        lineDataSet.setColor(colorGreen);
        lineDataSet.setCircleColor(colorGreen);
        lineDataSet.setLineWidth(2.5f);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.setValueTextColor(textColor);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(colorGreen);
        lineDataSet.setFillAlpha(50);

        lineChart.setData(new LineData(lineDataSet));
        applyChartStyling(lineChart, labels, textColor);
        lineChart.animateX(900);
        lineChart.invalidate();

        // --- Cálculo do score de economia ---
        float maxBal = balances[0], minBal = balances[0];
        int   maxCnt = counts[0],  minCnt = counts[0];
        for (int i = 1; i < 6; i++) {
            if (balances[i] > maxBal) maxBal = balances[i];
            if (balances[i] < minBal) minBal = balances[i];
            if (counts[i] > maxCnt)  maxCnt  = counts[i];
            if (counts[i] < minCnt)  minCnt  = counts[i];
        }

        float[] scores = new float[6];
        int bestIdx = 0;
        for (int i = 0; i < 6; i++) {
            float balScore = (maxBal == minBal) ? 100f
                    : ((balances[i] - minBal) / (maxBal - minBal)) * 100f;
            float cntScore = (maxCnt == minCnt) ? 100f
                    : ((maxCnt - counts[i]) / (float) (maxCnt - minCnt)) * 100f;
            scores[i] = (balScore + cntScore) / 2f;
            if (scores[i] > scores[bestIdx]) bestIdx = i;
        }

        // --- BarChart (score de economia) ---
        List<BarEntry> barEntries = new ArrayList<>();
        List<Integer>  barColors  = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            barEntries.add(new BarEntry(i, scores[i]));
            barColors.add(i == bestIdx ? colorGreen : colorBlue);
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Score (0–100)");
        barDataSet.setColors(barColors);
        barDataSet.setValueTextColor(textColor);
        barDataSet.setValueTextSize(9f);

        barChart.setData(new BarData(barDataSet));
        barChart.setFitBars(true);

        XAxis barX = barChart.getXAxis();
        barX.setValueFormatter(new IndexAxisValueFormatter(labels));
        barX.setPosition(XAxis.XAxisPosition.BOTTOM);
        barX.setGranularity(1f);
        barX.setDrawGridLines(false);
        barX.setTextColor(textColor);

        barChart.getAxisLeft().setTextColor(textColor);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(100f);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setTextColor(textColor);
        barChart.setDescription(null);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(900);
        barChart.invalidate();

        // --- Resumo textual ---
        tvBestMonth.setText("🏅 Mês mais econômico: " + labels[bestIdx]
                + " (score " + Math.round(scores[bestIdx]) + "/100)");

        // Comparação mês atual vs anterior (índices 5 e 4)
        float currentScore  = scores[5];
        float previousScore = scores[4];
        if (previousScore == 0) {
            tvVsPrevious.setText("📅 Sem dados suficientes para comparar com o mês anterior.");
        } else {
            float diff = currentScore - previousScore;
            if (diff > 5) {
                tvVsPrevious.setText(String.format(Locale.getDefault(),
                        "📈 Você foi %.0f%% mais econômico que o mês anterior!", diff));
                tvVsPrevious.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
            } else if (diff < -5) {
                tvVsPrevious.setText(String.format(Locale.getDefault(),
                        "📉 Você gastou %.0f%% mais que o mês anterior.", Math.abs(diff)));
                tvVsPrevious.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
            } else {
                tvVsPrevious.setText("➡️ Desempenho similar ao mês anterior.");
                tvVsPrevious.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            }
        }
    }

    /** Aplica estilos comuns (eixos, legenda) adaptados ao modo escuro. */
    private void applyChartStyling(LineChart chart, String[] labels, int textColor) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(textColor);

        chart.getAxisLeft().setTextColor(textColor);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setTextColor(textColor);
        chart.setDescription(null);
    }

    // =====================================================================
    // SEÇÃO 3 — Metas Atingidas
    // =====================================================================

    private void loadGoals() {
        llGoalsContainer.removeAllViews();
        List<Goal> completed = db.goalDao().getGoalsByStatus(Goal.STATUS_CONCLUIDA);

        if (completed.isEmpty()) {
            addEmptyRow(llGoalsContainer, "Nenhuma meta concluída ainda.");
            return;
        }

        for (Goal g : completed) {
            addGoalRow(g);
        }
    }

    private void addGoalRow(Goal g) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        int padV = dp(8), padH = dp(4);
        row.setPadding(padH, padV, padH, padV);

        // Linha superior: título + valor
        LinearLayout top = new LinearLayout(requireContext());
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvTitle = new TextView(requireContext());
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvTitle.setText("🏆 " + g.getTitle());
        tvTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        tvTitle.setTextSize(14f);
        tvTitle.setTypeface(null, Typeface.BOLD);

        TextView tvAmount = new TextView(requireContext());
        tvAmount.setText(FormatUtils.formatCurrency(g.getTargetAmount()));
        tvAmount.setTextColor(ContextCompat.getColor(requireContext(), R.color.income_color));
        tvAmount.setTextSize(13f);

        top.addView(tvTitle);
        top.addView(tvAmount);
        row.addView(top);

        if (g.getDescription() != null && !g.getDescription().isEmpty()) {
            TextView tvDesc = new TextView(requireContext());
            tvDesc.setText(g.getDescription());
            tvDesc.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            tvDesc.setTextSize(12f);
            row.addView(tvDesc);
        }

        addDivider(llGoalsContainer);
        llGoalsContainer.addView(row);
    }

    // =====================================================================
    // SEÇÃO 4 — Planejamento do Mês
    // =====================================================================

    private void loadBudgets() {
        llBudgetContainer.removeAllViews();
        int month = FormatUtils.getCurrentMonth();
        int year  = FormatUtils.getCurrentYear();
        List<Budget> budgets = db.budgetDao().getBudgetsByMonthYear(month, year);

        if (budgets.isEmpty()) {
            addEmptyRow(llBudgetContainer, "Nenhum planejamento configurado para este mês.");
            return;
        }

        long start = FormatUtils.getStartOfCurrentMonth();
        long end   = FormatUtils.getEndOfCurrentMonth();

        // Agrupa por status para exibição ordenada: OK → WARNING → OVER
        List<Budget> ok      = new ArrayList<>();
        List<Budget> warning = new ArrayList<>();
        List<Budget> over    = new ArrayList<>();
        Map<Long, Double> spentMap = new HashMap<>();

        for (Budget b : budgets) {
            double spent = db.transactionDao()
                    .getDespesasByCategoryAndPeriod(b.getCategory(), start, end);
            spentMap.put(b.getId(), spent);
            switch (b.getStatus(spent)) {
                case Budget.STATUS_OVER:    over.add(b);    break;
                case Budget.STATUS_WARNING: warning.add(b); break;
                default:                   ok.add(b);      break;
            }
        }

        // Excedidos primeiro (mais urgente), depois atenção, depois OK
        if (!over.isEmpty()) {
            addSectionLabel(llBudgetContainer, "❌ Limite Excedido", R.color.expense_color);
            for (Budget b : over)    addBudgetRow(b, spentMap.get(b.getId()), R.color.expense_color);
        }
        if (!warning.isEmpty()) {
            addSectionLabel(llBudgetContainer, "⚠️ Próximo do Limite", R.color.orange);
            for (Budget b : warning) addBudgetRow(b, spentMap.get(b.getId()), R.color.orange);
        }
        if (!ok.isEmpty()) {
            addSectionLabel(llBudgetContainer, "✅ Dentro do Limite", R.color.income_color);
            for (Budget b : ok)      addBudgetRow(b, spentMap.get(b.getId()), R.color.income_color);
        }
    }

    private void addSectionLabel(LinearLayout container, String text, int colorRes) {
        TextView tv = new TextView(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        lp.bottomMargin = dp(4);
        tv.setLayoutParams(lp);
        tv.setText(text);
        tv.setTextColor(ContextCompat.getColor(requireContext(), colorRes));
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        container.addView(tv);
    }

    private void addBudgetRow(Budget b, double spent, int statusColorRes) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(4), dp(6), dp(4), dp(6));

        // Linha 1: categoria + valores
        LinearLayout top = new LinearLayout(requireContext());
        top.setOrientation(LinearLayout.HORIZONTAL);

        TextView tvCat = new TextView(requireContext());
        tvCat.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvCat.setText(CategoryUtils.getCategoryEmoji(b.getCategory()) + " " + b.getCategory());
        tvCat.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        tvCat.setTextSize(13f);

        TextView tvValues = new TextView(requireContext());
        String spentStr = FormatUtils.formatCurrency(spent);
        String limitStr = FormatUtils.formatCurrency(b.getLimitAmount());
        tvValues.setText(spentStr + " / " + limitStr);
        tvValues.setTextColor(ContextCompat.getColor(requireContext(), statusColorRes));
        tvValues.setTextSize(12f);

        top.addView(tvCat);
        top.addView(tvValues);
        row.addView(top);

        // Linha 2: ProgressBar
        ProgressBar pb = new ProgressBar(requireContext(), null,
                android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams pbLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(6));
        pbLp.topMargin = dp(4);
        pb.setLayoutParams(pbLp);
        int percent = b.getPercentUsed(spent);
        pb.setMax(100);
        pb.setProgress(Math.min(percent, 100));
        pb.getProgressDrawable().setTint(
                ContextCompat.getColor(requireContext(), statusColorRes));
        row.addView(pb);

        // Linha 3: texto restante/excedido
        TextView tvRemaining = new TextView(requireContext());
        double remaining = b.getRemaining(spent);
        if (remaining >= 0) {
            tvRemaining.setText("Restam " + FormatUtils.formatCurrency(remaining));
            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        } else {
            tvRemaining.setText("Excedido em " + FormatUtils.formatCurrency(Math.abs(remaining)));
            tvRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_color));
        }
        tvRemaining.setTextSize(11f);
        LinearLayout.LayoutParams remLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        remLp.topMargin = dp(2);
        tvRemaining.setLayoutParams(remLp);
        row.addView(tvRemaining);

        addDivider(llBudgetContainer);
        llBudgetContainer.addView(row);
    }

    // =====================================================================
    // Helpers
    // =====================================================================

    private void addEmptyRow(LinearLayout container, String message) {
        TextView tv = new TextView(requireContext());
        tv.setText(message);
        tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        tv.setTextSize(13f);
        tv.setPadding(dp(4), dp(8), dp(4), dp(8));
        container.addView(tv);
    }

    private void addDivider(LinearLayout container) {
        View divider = new View(requireContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.topMargin = dp(4);
        lp.bottomMargin = dp(4);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray));
        divider.setAlpha(0.3f);
        container.addView(divider);
    }

    private int dp(int value) {
        float density = requireContext().getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
