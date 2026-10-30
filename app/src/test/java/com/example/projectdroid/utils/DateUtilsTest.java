package com.example.projectdroid.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

// Testes das datas usadas nos empréstimos (prazos, renovações e atrasos)
public class DateUtilsTest {

    @Test
    public void hojeEstaNoFormatoAnoMesDia() {
        assertTrue(DateUtils.today().matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    public void somar14Dias() {
        assertEquals("2026-11-11", DateUtils.addDays("2026-10-28", 14));
    }

    @Test
    public void somarDiasMudaDeAno() {
        assertEquals("2027-01-08", DateUtils.addDays("2026-12-25", 14));
    }

    @Test
    public void somarDiasEmAnoBissexto() {
        assertEquals("2028-02-29", DateUtils.addDays("2028-02-28", 1));
    }

    @Test
    public void dataInvalidaContaAPartirDeHoje() {
        assertEquals(DateUtils.today(), DateUtils.addDays("data errada", 0));
    }

    @Test
    public void dataNoPassadoEstaAntesDeHoje() {
        assertTrue(DateUtils.isBeforeToday("2020-01-01"));
    }

    @Test
    public void hojeEDatasFuturasNaoEstaoEmAtraso() {
        assertFalse(DateUtils.isBeforeToday(DateUtils.today()));
        assertFalse(DateUtils.isBeforeToday("2999-01-01"));
    }

    @Test
    public void dataNulaNaoEstaEmAtraso() {
        assertFalse(DateUtils.isBeforeToday(null));
    }
}
