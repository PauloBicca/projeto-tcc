package com.financeiro.app.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.database.AppDatabase;
import com.financeiro.app.models.Transaction;
import com.financeiro.app.utils.CategoryUtils;
import com.financeiro.app.utils.FormatUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tela de gráficos: pizza (gastos por categoria) e linha (evolução do saldo).
 */
public class ChartsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private LineChart lineChart;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        db = AppDatabase.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gráficos");
        }

        pieChart  = findViewById(R.id.pie_chart);
        lineChart = findViewById(R.id.line_chart);

        setupPieChart();
        setupLineChart();
    }

    // ======================== Gráfico de Pizza ========================

    private void setupPieChart() {
        long start = FormatUtils.getStartOfCurrentMonth();
        long end   = FormatUtils.getEndOfCurrentMonth();
        List<Transaction> despesas = db.transactionDao().getTransactionsByPeriod(start, end);

        // Agrupar por categoria
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Transaction t : despesas) {
            if (Transaction.TYPE_DESPESA.equals(t.getType())) {
                String cat = t.getCategory();
                categoryTotals.put(cat, categoryTotals.getOrDefault(cat, 0f) + (float) t.getAmount());
            }
        }

        if (categoryTotals.isEmpty()) {
            pieChart.setNoDataText("Nenhuma despesa este mês");
            pieChart.invalidate();
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors  = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            colors.add(CategoryUtils.getCategoryColor(entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(40f);
        pieChart.setDescription(null);
        pieChart.getLegend().setEnabled(true);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.setCenterText("Gastos\ndo Mês");
        pieChart.setCenterTextSize(14f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    // ======================== Gráfico de Linha ========================

    private void setupLineChart() {
        // Busca os últimos 6 meses
        List<Entry> balanceEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        SimpleDateFormat sdfMonth = new SimpleDateFormat("MMM", new Locale("pt","BR"));
        Calendar cal = Calendar.getInstance();

        for (int i = 5; i >= 0; i--) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.MONTH, -i);
            c.set(Calendar.DAY_OF_MONTH, 1);
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            long start = c.getTimeInMillis();
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
            c.set(Calendar.HOUR_OF_DAY, 23);
            c.set(Calendar.MINUTE, 59);
            c.set(Calendar.SECOND, 59);
            long end = c.getTimeInMillis();

            double receitas = db.transactionDao().getTotalReceitasByPeriod(start, end);
            double despesas = db.transactionDao().getTotalDespesasByPeriod(start, end);
            float saldo = (float)(receitas - despesas);

            balanceEntries.add(new Entry(5 - i, saldo));
            labels.add(sdfMonth.format(c.getTime()));
        }

        LineDataSet dataSet = new LineDataSet(balanceEntries, "Saldo Mensal");
        dataSet.setColor(0xFF4CAF50);
        dataSet.setCircleColor(0xFF4CAF50);
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(0xFF81C784);
        dataSet.setFillAlpha(60);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);

        lineChart.getAxisRight().setEnabled(false);
        lineChart.setDescription(null);
        lineChart.getLegend().setEnabled(true);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
