package com.financeiro.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.financeiro.app.R;
import com.financeiro.app.ai.GeminiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AiTipsActivity extends AppCompatActivity {

    private MaterialButton btnGenerate;
    private View layoutLoading;
    private MaterialCardView cardTips;
    private TextView tvTips;
    private GeminiService geminiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_tips);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        btnGenerate   = findViewById(R.id.btn_generate);
        layoutLoading = findViewById(R.id.layout_loading);
        cardTips      = findViewById(R.id.card_tips);
        tvTips        = findViewById(R.id.tv_tips);

        geminiService = new GeminiService(this);

        btnGenerate.setOnClickListener(v -> generateTips());
    }

    private void generateTips() {
        btnGenerate.setEnabled(false);
        layoutLoading.setVisibility(View.VISIBLE);
        cardTips.setVisibility(View.GONE);

        geminiService.generateTips(new GeminiService.TipsCallback() {
            @Override
            public void onSuccess(String tips) {
                runOnUiThread(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    cardTips.setVisibility(View.VISIBLE);
                    tvTips.setText(tips);
                    btnGenerate.setEnabled(true);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    layoutLoading.setVisibility(View.GONE);
                    btnGenerate.setEnabled(true);
                    Toast.makeText(AiTipsActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
