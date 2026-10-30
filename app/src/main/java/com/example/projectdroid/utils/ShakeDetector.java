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
        float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
        float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
        float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

        double gForce = Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD_G) {
            long now = System.currentTimeMillis();
            if (now - lastShakeTime > MIN_TIME_BETWEEN_SHAKES_MS) {
                lastShakeTime = now;
                listener.onShake();
            }
        }
    }

    // Não precisamos de saber quando a precisão do sensor muda
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
