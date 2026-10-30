package com.example.projectdroid.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.GoogleBooksResponse;
import com.example.projectdroid.network.GoogleBooksApi;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Testes da conversão das respostas da Google Books (UC1, UC2, UC3) e do livro do dia (UC8).
// A Google Books é substituída por um mock, por isso os testes não usam a internet.
public class BookRepositoryTest {

    private GoogleBooksApi api;
    private BookRepository repository;

    @Before
    public void setUp() {
        api = mock(GoogleBooksApi.class);
        repository = new BookRepository(api);
    }

    // Cria um resultado da Google como o que vem no JSON
    private GoogleBooksResponse.Item item(String id, String title, List<String> authors) {
        GoogleBooksResponse.Item item = new GoogleBooksResponse.Item();
        item.id = id;
        item.volumeInfo = new GoogleBooksResponse.VolumeInfo();
        item.volumeInfo.title = title;
        item.volumeInfo.authors = authors;
        return item;
    }

    private GoogleBooksResponse response(GoogleBooksResponse.Item... items) {
        GoogleBooksResponse response = new GoogleBooksResponse();
        response.items = new ArrayList<>(Arrays.asList(items));
        return response;
    }

    private GoogleBooksResponse.IndustryIdentifier identifier(String type, String value) {
        GoogleBooksResponse.IndustryIdentifier id = new GoogleBooksResponse.IndustryIdentifier();
        id.type = type;
        id.identifier = value;
        return id;
    }

    // ---------- UC1 / UC3 - Pesquisa ----------

    @Test
    public void pesquisaConverteResultadosEmBooks() {
        GoogleBooksResponse.Item item = item("id1", "Os Maias", Arrays.asList("Eça de Queirós"));
        item.volumeInfo.pageCount = 600;
        item.volumeInfo.description = "Romance";
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(response(item)));

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("maias", 0, callback);

        assertEquals(1, callback.result.size());
        Book book = callback.result.get(0);
        assertEquals("id1", book.getBookId());
        assertEquals("Os Maias", book.getTitle());
        assertEquals("Eça de Queirós", book.getAuthorsAsText());
        assertEquals(Integer.valueOf(600), book.getNumberOfPages());
        assertEquals("Romance", book.getDescription());
    }

    @Test
    public void semResultadosDevolveListaVazia() {
        // Quando não há resultados, a Google não envia a lista "items"
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(new GoogleBooksResponse()));

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("xqzxqzxq", 0, callback);

        assertNotNull(callback.result);
        assertTrue(callback.result.isEmpty());
    }

    @Test
    public void livroSemAutoresFicaComAutorDesconhecido() {
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(response(item("id1", "Livro", null))));

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("livro", 0, callback);

        assertEquals("Autor Desconhecido", callback.result.get(0).getAuthorsAsText());
    }

    @Test
    public void capaEmHttpPassaAHttps() {
        GoogleBooksResponse.Item item = item("id1", "Livro", null);
        item.volumeInfo.imageLinks = new GoogleBooksResponse.ImageLinks();
        item.volumeInfo.imageLinks.thumbnail = "http://books.google.com/capa.jpg";
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(response(item)));

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("livro", 0, callback);

        assertEquals("https://books.google.com/capa.jpg", callback.result.get(0).getCoverUrl());
    }

    @Test
    public void preferIsbn13AoIsbn10() {
        GoogleBooksResponse.Item item = item("id1", "Livro", null);
        item.volumeInfo.industryIdentifiers = Arrays.asList(
                identifier("ISBN_10", "1234567890"),
                identifier("ISBN_13", "9781234567897"));
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(response(item)));

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("livro", 0, callback);

        assertEquals("9781234567897", callback.result.get(0).getIsbn());
    }

    @Test
    public void usaIsbn10QuandoNaoHaIsbn13() {
        GoogleBooksResponse.Item item = item("id1", "Livro", null);
        item.volumeInfo.industryIdentifiers = Arrays.asList(identifier("ISBN_10", "1234567890"));
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(response(item)));

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("livro", 0, callback);

        assertEquals("1234567890", callback.result.get(0).getIsbn());
    }

    @Test
    public void paginasSeguintesPedemOStartIndexCerto() {
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.sucesso(response()));

        repository.searchBooks("harry", 0, new TestCallback<>());
        repository.searchBooks("harry", BookRepository.PAGE_SIZE, new TestCallback<>());

        verify(api).searchBooks(eq("harry"), eq(0), eq(BookRepository.PAGE_SIZE));
        verify(api).searchBooks(eq("harry"), eq(BookRepository.PAGE_SIZE), eq(BookRepository.PAGE_SIZE));
    }

    @Test
    public void semRedeDevolveErro() {
        when(api.searchBooks(anyString(), anyInt(), anyInt())).thenReturn(FakeCall.semRede());

        TestCallback<List<Book>> callback = new TestCallback<>();
        repository.searchBooks("livro", 0, callback);

        assertTrue(callback.error.startsWith("Erro de rede"));
        assertNull(callback.result);
    }

    // ---------- UC2 - Livro pelo id ----------

    @Test
    public void getBookByIdDevolveOLivroCompleto() {
        GoogleBooksResponse.Item item = item("id9", "Livro Completo", Arrays.asList("Autor"));
        item.volumeInfo.description = "Descrição completa";
        when(api.getBook("id9")).thenReturn(FakeCall.sucesso(item));

        TestCallback<Book> callback = new TestCallback<>();
        repository.getBookById("id9", callback);

        assertEquals("Livro Completo", callback.result.getTitle());
        assertEquals("Descrição completa", callback.result.getDescription());
    }

    // ---------- UC8 - Livro do dia ----------

    @Test
    public void livroDoDiaEhOMesmoEmVariosPedidosNoMesmoDia() {
        GoogleBooksResponse.Item[] items = new GoogleBooksResponse.Item[20];
        for (int i = 0; i < items.length; i++) {
            items[i] = item("id" + i, "Livro " + i, null);
        }
        when(api.searchBooks(anyString(), anyInt())).thenReturn(FakeCall.sucesso(response(items)));

        TestCallback<Book> first = new TestCallback<>();
        TestCallback<Book> second = new TestCallback<>();
        repository.getDailyBook(first);
        new BookRepository(api).getDailyBook(second); // outro "telemóvel"

        assertEquals(first.result.getBookId(), second.result.getBookId());

        // Os dois pedidos foram iguais: mesmo tema e 20 resultados
        ArgumentCaptor<String> queries = ArgumentCaptor.forClass(String.class);
        verify(api, times(2)).searchBooks(queries.capture(), anyInt());
        assertEquals(queries.getAllValues().get(0), queries.getAllValues().get(1));
        assertTrue(queries.getValue().startsWith("subject:"));
    }

    @Test
    public void livroDoDiaFuncionaComPoucosResultados() {
        // Se a Google devolver só 1 livro, o % evita sair da lista
        when(api.searchBooks(anyString(), anyInt()))
                .thenReturn(FakeCall.sucesso(response(item("unico", "Único", null))));

        TestCallback<Book> callback = new TestCallback<>();
        repository.getDailyBook(callback);

        assertEquals("unico", callback.result.getBookId());
    }

    @Test
    public void livroDoDiaSemResultadosDevolveErro() {
        when(api.searchBooks(anyString(), anyInt())).thenReturn(FakeCall.sucesso(new GoogleBooksResponse()));

        TestCallback<Book> callback = new TestCallback<>();
        repository.getDailyBook(callback);

        assertEquals("Não foi possível encontrar o livro do dia.", callback.error);
    }
}
