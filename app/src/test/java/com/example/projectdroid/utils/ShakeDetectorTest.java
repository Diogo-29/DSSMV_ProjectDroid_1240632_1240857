package com.example.projectdroid.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

// Testes da deteção do abanão (UC8), com valores do acelerómetro inventados.
// Valores em m/s²; parado, o telemóvel mede só a gravidade (cerca de 9.8 no eixo z).
public class ShakeDetectorTest {

    private int shakes;
    private ShakeDetector detector;

    @Before
    public void setUp() {
        shakes = 0;
        detector = new ShakeDetector(() -> shakes++);
    }

    @Test
    public void telemovelParadoMedeCercaDe1g() {
        assertEquals(1.0, ShakeDetector.calcularForcaG(0f, 0f, 9.81f), 0.01);
    }

    @Test
    public void telemovelParadoNaoContaComoAbanao() {
        detector.processar(0f, 0f, 9.81f, 10_000);
        assertEquals(0, shakes);
    }

    @Test
    public void movimentoNormalNaoContaComoAbanao() {
        // cerca de 1.8 g, por exemplo ao andar com o telemóvel na mão
        detector.processar(10f, 10f, 10f, 10_000);
        assertEquals(0, shakes);
    }

    @Test
    public void movimentoBruscoContaComoAbanao() {
        // cerca de 3.3 g
        detector.processar(20f, 20f, 15f, 10_000);
        assertEquals(1, shakes);
    }

    @Test
    public void doisAbanoesSeguidosContamSoUmaVez() {
        detector.processar(20f, 20f, 15f, 10_000);
        detector.processar(20f, 20f, 15f, 10_500); // só 0.5 s depois
        assertEquals(1, shakes);
    }

    @Test
    public void abanoesComMaisDe1SegundoContamOsDois() {
        detector.processar(20f, 20f, 15f, 10_000);
        detector.processar(20f, 20f, 15f, 11_500); // 1.5 s depois
        assertEquals(2, shakes);
    }
}
