package com.financeiro.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.financeiro.app.R;
import com.financeiro.app.utils.PinManager;

/**
 * Tela de autenticação por PIN.
 * Exibida ao abrir o app ou após timeout de sessão.
 */
public class PinActivity extends AppCompatActivity {

    private TextView tvPinDisplay;
    private TextView tvError;
    private ImageView[] ivDots;
    private StringBuilder pinInput = new StringBuilder();
    private PinManager pinManager;
    private static final int MAX_PIN_LENGTH = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pinManager = new PinManager(this);

        // Se não tem PIN configurado, vai para SetupPin
        if (!pinManager.isPinSet()) {
            startActivity(new Intent(this, SetupPinActivity.class));
            finish();
            return;
        }

        // Se sessão ainda válida, vai direto pro app
        if (pinManager.isSessionValid()) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_pin);
        initViews();
        setupNumpad();
    }

    private void initViews() {
        tvPinDisplay = findViewById(R.id.tv_pin_display);
        tvError = findViewById(R.id.tv_error);
        ivDots = new ImageView[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4)
        };
    }

    private void setupNumpad() {
        int[] btnIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
                R.id.btn8, R.id.btn9
        };
        int[] digits = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        for (int i = 0; i < btnIds.length; i++) {
            final int digit = digits[i];
            Button btn = findViewById(btnIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> addDigit(String.valueOf(digit)));
            }
        }

        Button btnDel = findViewById(R.id.btn_del);
        if (btnDel != null) {
            btnDel.setOnClickListener(v -> removeLastDigit());
        }

        Button btnOk = findViewById(R.id.btn_ok);
        if (btnOk != null) {
            btnOk.setOnClickListener(v -> validatePin());
        }
    }

    private void addDigit(String digit) {
        if (pinInput.length() < MAX_PIN_LENGTH) {
            pinInput.append(digit);
            updateDots();
            tvError.setVisibility(View.GONE);
            if (pinInput.length() == 4) {
                validatePin();
            }
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
                        i < pinInput.length()
                                ? R.drawable.dot_filled
                                : R.drawable.dot_empty
                );
            }
        }
    }

    private void validatePin() {
        if (pinInput.length() < 4) {
            tvError.setText("Digite pelo menos 4 dígitos");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        if (pinManager.validatePin(pinInput.toString())) {
            goToMain();
        } else {
            tvError.setText("PIN incorreto. Tente novamente.");
            tvError.setVisibility(View.VISIBLE);
            pinInput.setLength(0);
            updateDots();
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
