package com.financeiro.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Gerenciador de PIN para autenticação local.
 * Armazena o PIN de forma segura usando hash SHA-256.
 */
public class PinManager {

    private static final String PREFS_NAME = "financeiro_security";
    private static final String KEY_PIN_HASH = "pin_hash";
    private static final String KEY_PIN_SET = "pin_set";
    private static final String KEY_LAST_UNLOCK = "last_unlock";
    private static final long SESSION_TIMEOUT_MS = 5 * 60 * 1000L; // 5 minutos

    private final SharedPreferences prefs;

    public PinManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ======================== Configuração de PIN ========================

    /**
     * Salva o PIN de forma segura (hash SHA-256).
     * @param pin PIN numérico de 4-6 dígitos
     */
    public void setPin(String pin) {
        String hash = hashPin(pin);
        prefs.edit()
                .putString(KEY_PIN_HASH, hash)
                .putBoolean(KEY_PIN_SET, true)
                .apply();
    }

    /**
     * Verifica se um PIN está configurado.
     */
    public boolean isPinSet() {
        return prefs.getBoolean(KEY_PIN_SET, false);
    }

    /**
     * Remove o PIN configurado.
     */
    public void removePin() {
        prefs.edit()
                .remove(KEY_PIN_HASH)
                .putBoolean(KEY_PIN_SET, false)
                .apply();
    }

    // ======================== Validação ========================

    /**
     * Valida o PIN informado contra o hash armazenado.
     * @param pin PIN a verificar
     * @return true se o PIN estiver correto
     */
    public boolean validatePin(String pin) {
        String storedHash = prefs.getString(KEY_PIN_HASH, "");
        String inputHash = hashPin(pin);
        boolean valid = storedHash.equals(inputHash);
        if (valid) {
            updateLastUnlock();
        }
        return valid;
    }

    // ======================== Sessão ========================

    /**
     * Atualiza o timestamp do último desbloqueio.
     */
    public void updateLastUnlock() {
        prefs.edit().putLong(KEY_LAST_UNLOCK, System.currentTimeMillis()).apply();
    }

    /**
     * Verifica se a sessão ainda está válida (dentro do timeout).
     * @return true se o usuário ainda está autenticado
     */
    public boolean isSessionValid() {
        long lastUnlock = prefs.getLong(KEY_LAST_UNLOCK, 0);
        return (System.currentTimeMillis() - lastUnlock) < SESSION_TIMEOUT_MS;
    }

    /**
     * Invalida a sessão (força novo login).
     */
    public void invalidateSession() {
        prefs.edit().putLong(KEY_LAST_UNLOCK, 0).apply();
    }

    // ======================== Hash ========================

    /**
     * Gera hash SHA-256 do PIN.
     */
    private String hashPin(String pin) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pin.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fallback simples se SHA-256 não estiver disponível
            return String.valueOf(pin.hashCode());
        }
    }
}
