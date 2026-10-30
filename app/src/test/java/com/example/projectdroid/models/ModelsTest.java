package com.example.projectdroid.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

// Testes dos modelos Book, Loan e Favorite
public class ModelsTest {

    @Test
    public void autoresSeparadosPorVirgula() {
        Book book = new Book("id", "T", Arrays.asList("Ana", "Bruno", "Carla"), null, null, null, null);
        assertEquals("Ana, Bruno, Carla", book.getAuthorsAsText());
    }

    @Test
    public void livroSemAutores() {
        assertEquals("Sem Autor", new Book("id", "T", null, null, null, null, null).getAuthorsAsText());
        assertEquals("Sem Autor", new Book("id", "T", new ArrayList<>(), null, null, null, null).getAuthorsAsText());
    }

    @Test
    public void emprestimoNovoFicaAtivo() {
        Loan loan = new Loan("uid", "livro", "T", "A", null, "2026-10-30", "2026-11-13");
        assertEquals(Loan.STATUS_ACTIVE, loan.getStatus());
        assertTrue(loan.isActive());
    }

    @Test
    public void emprestimoDevolvidoNaoEstaAtivo() {
        Loan loan = new Loan("uid", "livro", "T", "A", null, "2026-10-30", "2026-11-13");
        loan.setStatus(Loan.STATUS_RETURNED);
        assertFalse(loan.isActive());
    }

    @Test
    public void favoritoConvertidoEmBook() {
        Favorite favorite = new Favorite("uid", "livro9", "Título", "Ana, Bruno", "https://capa");
        Book book = favorite.toBook();

        assertEquals("livro9", book.getBookId());
        assertEquals("Título", book.getTitle());
        assertEquals(Arrays.asList("Ana", "Bruno"), book.getAuthors());
        assertEquals("https://capa", book.getCoverUrl());
        // O favorito não guarda a descrição: os detalhes vão buscá-la à Google
        assertNull(book.getDescription());
    }
}
