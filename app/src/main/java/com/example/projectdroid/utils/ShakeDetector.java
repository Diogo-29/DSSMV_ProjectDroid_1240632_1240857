package com.example.projectdroid.utils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

// Deteta quando o telemóvel é abanado, a partir do acelerómetro.
//
// O acelerómetro dá a aceleração nos 3 eixos (x, y, z) em m/s².
// Parado, o telemóvel mede só a gravidade (cerca de 9.8 m/s², ou seja 1 g).
// Calculamos a força total em "g": sqrt(x² + y² + z²) / 9.8
// Se passar de SHAKE_THRESHOLD_G, consideramos que houve um abanão.
public class ShakeDetector implements SensorEventListener {

    // 2.7 g: um movimento brusco, mas que não dispara só por andar com o telemóvel
    private static final float SHAKE_THRESHOLD_G = 2.7f;

    // Tempo mínimo entre dois abanões, para um só gesto não contar várias vezes
    private static final long MIN_TIME_BETWEEN_SHAKES_MS = 1000;

    // Forma de avisar o ecrã de que houve um abanão
    public interface OnShakeListener {
        void onShake();
    }

    private final OnShakeListener listener;
    private long lastShakeTime = 0;

    public ShakeDetector(OnShakeListener listener) {
        this.listener = listener;
    }

    // Chamado pelo Android sempre que o acelerómetro tem novos valores
    @Override
    public void onSensorChanged(SensorEvent event) {
        processar(event.values[0], event.values[1], event.values[2], System.currentTimeMillis());
    }

    // A lógica está separada do onSensorChanged para poder ser testada sem um sensor real
    // (nos testes chamamos este método com valores inventados)
    void processar(float x, float y, float z, long now) {
        if (calcularForcaG(x, y, z) > SHAKE_THRESHOLD_G) {
            if (now - lastShakeTime > MIN_TIME_BETWEEN_SHAKES_MS) {
                lastShakeTime = now;
                listener.onShake();
            }
        }
    }

    // Força total em "g": sqrt(x² + y² + z²) / 9.8
    static double calcularForcaG(float x, float y, float z) {
        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;
        return Math.sqrt(gX * gX + gY * gY + gZ * gZ);
    }

    // Não precisamos de saber quando a precisão do sensor muda
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
