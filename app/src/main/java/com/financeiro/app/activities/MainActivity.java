package com.financeiro.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.financeiro.app.R;
import com.financeiro.app.fragments.DashboardFragment;
import com.financeiro.app.fragments.GoalsFragment;
import com.financeiro.app.fragments.HistoryFragment;
import com.financeiro.app.fragments.PlanningFragment;
import com.financeiro.app.fragments.ReportFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Activity principal com navegação por Bottom Navigation.
 * Contém: Dashboard, Histórico, Metas, Planejamento.
 */
public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        fabAdd    = findViewById(R.id.fab_add);

        // Carregar fragment inicial (Dashboard)
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                loadFragment(new DashboardFragment());
                return true;
            } else if (id == R.id.nav_history) {
                loadFragment(new HistoryFragment());
                return true;
            } else if (id == R.id.nav_goals) {
                loadFragment(new GoalsFragment());
                return true;
            } else if (id == R.id.nav_planning) {
                loadFragment(new PlanningFragment());
                return true;
            } else if (id == R.id.nav_report) {
                loadFragment(new ReportFragment());
                return true;
            }
            return false;
        });

        // FAB para adicionar transação
        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(this, TransactionFormActivity.class))
        );
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualizar o fragment ativo ao retornar de outra activity
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (current != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .detach(current)
                    .attach(current)
                    .commit();
        }
    }
}
