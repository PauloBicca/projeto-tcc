package com.financeiro.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.financeiro.app.R;
import com.financeiro.app.utils.PinManager;

/**
 * Tela de configuração inicial do PIN.
 * Usuário digita o PIN duas vezes para confirmar.
 */
public class SetupPinActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubtitle, tvError;
    private ImageView[] ivDots;
    private StringBuilder pinInput = new StringBuilder();
    private String firstPin = null;
    private PinManager pinManager;
    private static final int PIN_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_pin);
        pinManager = new PinManager(this);
        initViews();
        setupNumpad();
        updateUI();
    }

    private void initViews() {
        tvTitle    = findViewById(R.id.tv_setup_title);
        tvSubtitle = findViewById(R.id.tv_setup_subtitle);
        tvError    = findViewById(R.id.tv_setup_error);
        ivDots = new ImageView[]{
                findViewById(R.id.dot1), findViewById(R.id.dot2),
                findViewById(R.id.dot3), findViewById(R.id.dot4),
        };
    }

    private void setupNumpad() {
        int[] btnIds = {R.id.btn0,R.id.btn1,R.id.btn2,R.id.btn3,
                R.id.btn4,R.id.btn5,R.id.btn6,R.id.btn7,R.id.btn8,R.id.btn9};
        for (int i = 0; i < btnIds.length; i++) {
            final String digit = String.valueOf(i);
            Button btn = findViewById(btnIds[i]);
            if (btn != null) btn.setOnClickListener(v -> addDigit(digit));
        }
        Button btnDel = findViewById(R.id.btn_del);
        if (btnDel != null) btnDel.setOnClickListener(v -> removeLastDigit());
        Button btnOk  = findViewById(R.id.btn_ok);
        if (btnOk != null)  btnOk.setOnClickListener(v -> confirmStep());
    }

    private void addDigit(String digit) {
        if (pinInput.length() < PIN_LENGTH) {
            pinInput.append(digit);
            updateDots();
            tvError.setVisibility(View.GONE);
            if (pinInput.length() == PIN_LENGTH) confirmStep();
        }
    }

    private void removeLastDigit() {
        if (pinInput.length() > 0) {
            pinInput.deleteCharAt(pinInput.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < ivDots.length; i++) {
            if (ivDots[i] != null) {
                ivDots[i].setImageResource(
                        i < pinInput.length() ? R.drawable.dot_filled : R.drawable.dot_empty
                );
            }
        }
    }

    private void updateUI() {
        if (firstPin == null) {
            tvTitle.setText("Criar PIN de Acesso");
            tvSubtitle.setText("Digite um PIN de 4 dígitos");
        } else {
            tvTitle.setText("Confirmar PIN");
            tvSubtitle.setText("Digite novamente para confirmar");
        }
    }

    private void confirmStep() {
        if (pinInput.length() < PIN_LENGTH) {
            tvError.setText("Digite " + PIN_LENGTH + " dígitos");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (firstPin == null) {
            // Primeira entrada: guardar e pedir confirmação
            firstPin = pinInput.toString();
            pinInput.setLength(0);
            updateDots();
            updateUI();
        } else {
            // Segunda entrada: confirmar
            if (firstPin.equals(pinInput.toString())) {
                pinManager.setPin(firstPin);
                pinManager.updateLastUnlock();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                tvError.setText("PINs não coincidem. Tente novamente.");
                tvError.setVisibility(View.VISIBLE);
                firstPin = null;
                pinInput.setLength(0);
                updateDots();
                updateUI();
            }
        }
    }
}
