package com.example.projectdroid.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.projectdroid.models.Book;
import com.example.projectdroid.models.Loan;
import com.example.projectdroid.network.RestDbApi;
import com.example.projectdroid.utils.DateUtils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Testes das regras da biblioteca: UC4 (requisitar), UC5 (devolver), UC14 (renovar) e stock.
// O RestDB é substituído por um mock, por isso os testes não usam a internet.
public class LoanRepositoryTest {

    private static final String USER = "uid-teste";
    private static final String OTHER_USER = "outro-uid";
    private static final String BOOK_ID = "livro123";

    private RestDbApi api;
    private LoanRepository repository;
    private Book book;

    @Before
    public void setUp() {
        api = mock(RestDbApi.class);
        repository = new LoanRepository(api);
        book = new Book(BOOK_ID, "Livro de Teste", Arrays.asList("Autor A", "Autor B"),
                "9781234567890", "Descrição", "https://capa", 200);

        // Por defeito, o POST devolve o próprio empréstimo que recebeu
        when(api.createLoan(any())).thenAnswer(inv -> FakeCall.sucesso(inv.getArgument(0)));
    }

    // Cria um empréstimo ativo de um livro para um utilizador
    private Loan activeLoan(String userId, String bookId) {
        return new Loan(userId, bookId, "Título", "Autor", null, "2026-10-01", "2026-10-15");
    }

    private void restDbDevolveAtivos(List<Loan> activeLoans) {
        when(api.getLoans(anyString())).thenReturn(FakeCall.sucesso(activeLoans));
    }

    // ---------- Stock de exemplares ----------

    @Test
    public void totalDeExemplaresEstaEntre1e5() {
        String[] ids = {"a", "abc", "zyTsyAAAQBAJ", "xyz-123", "", "livro com espaços", BOOK_ID};
        for (String id : ids) {
            int total = LoanRepository.getTotalCopies(id);
            assertTrue("id " + id + " deu " + total, total >= 1 && total <= 5);
        }
    }

    @Test
    public void totalDeExemplaresEhSempreIgualParaOMesmoLivro() {
        assertEquals(LoanRepository.getTotalCopies(BOOK_ID), LoanRepository.getTotalCopies(BOOK_ID));
    }

    @Test
    public void exemplaresDisponiveisSaoTotalMenosAtivos() {
        int total = LoanRepository.getTotalCopies(BOOK_ID);
        restDbDevolveAtivos(new ArrayList<>());

        TestCallback<Integer> callback = new TestCallback<>();
        repository.getAvailableCopies(BOOK_ID, callback);

        assertEquals(Integer.valueOf(total), callback.result);
    }

    @Test
    public void exemplaresDisponiveisNuncaSaoNegativos() {
        List<Loan> muitos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            muitos.add(activeLoan("user" + i, BOOK_ID));
        }
        restDbDevolveAtivos(muitos);

        TestCallback<Integer> callback = new TestCallback<>();
        repository.getAvailableCopies(BOOK_ID, callback);

        assertEquals(Integer.valueOf(0), callback.result);
    }

    // ---------- UC4 - Requisitar ----------

    @Test
    public void requisitarComSucessoCriaEmprestimoAtivoCom14Dias() {
        restDbDevolveAtivos(new ArrayList<>());

        TestCallback<Loan> callback = new TestCallback<>();
        repository.requestLoan(book, USER, callback);

        // Verifica o que foi enviado no POST para o RestDB
        ArgumentCaptor<Loan> sent = ArgumentCaptor.forClass(Loan.class);
        verify(api).createLoan(sent.capture());
        Loan loan = sent.getValue();

        String today = DateUtils.today();
        assertEquals(USER, loan.getUserId());
        assertEquals(BOOK_ID, loan.getGoogleBookId());
        assertEquals("Autor A, Autor B", loan.getAuthors());
        assertEquals(Loan.STATUS_ACTIVE, loan.getStatus());
        assertEquals(today, loan.getLoanDate());
        assertEquals(DateUtils.addDays(today, LoanRepository.LOAN_DAYS), loan.getDueDate());
        assertTrue(callback.successCalled);
        assertNull(callback.error);
    }

    @Test
    public void naoPodeRequisitarOMesmoLivroDuasVezes() {
        restDbDevolveAtivos(Arrays.asList(activeLoan(USER, BOOK_ID)));

        TestCallback<Loan> callback = new TestCallback<>();
        repository.requestLoan(book, USER, callback);

        assertEquals("Já tens este livro requisitado.", callback.error);
        verify(api, never()).createLoan(any());
    }

    @Test
    public void naoPodeRequisitarSemExemplaresDisponiveis() {
        // Outros utilizadores têm todos os exemplares deste livro
        List<Loan> ativos = new ArrayList<>();
        for (int i = 0; i < LoanRepository.getTotalCopies(BOOK_ID); i++) {
            ativos.add(activeLoan(OTHER_USER + i, BOOK_ID));
        }
        restDbDevolveAtivos(ativos);

        TestCallback<Loan> callback = new TestCallback<>();
        repository.requestLoan(book, USER, callback);

        assertEquals("Não há exemplares disponíveis deste livro.", callback.error);
        verify(api, never()).createLoan(any());
    }

    @Test
    public void naoPodeTerMaisDe3EmprestimosAtivos() {
        restDbDevolveAtivos(Arrays.asList(
                activeLoan(USER, "livroA"),
                activeLoan(USER, "livroB"),
                activeLoan(USER, "livroC")));

        TestCallback<Loan> callback = new TestCallback<>();
        repository.requestLoan(book, USER, callback);

        assertTrue(callback.error.startsWith("Já tens 3 empréstimos ativos"));
        verify(api, never()).createLoan(any());
    }

    @Test
    public void emprestimosDeOutrosUtilizadoresNaoContamParaOLimite() {
        restDbDevolveAtivos(Arrays.asList(
                activeLoan(OTHER_USER, "livroA"),
                activeLoan(OTHER_USER, "livroB"),
                activeLoan(OTHER_USER, "livroC")));

        TestCallback<Loan> callback = new TestCallback<>();
        repository.requestLoan(book, USER, callback);

        assertTrue(callback.successCalled);
        verify(api).createLoan(any());
    }

    @Test
    public void semRedeDevolveErro() {
        when(api.getLoans(anyString())).thenReturn(FakeCall.semRede());

        TestCallback<Loan> callback = new TestCallback<>();
        repository.requestLoan(book, USER, callback);

        assertTrue(callback.error.startsWith("Erro de rede"));
        assertFalse(callback.successCalled);
    }

    // ---------- UC5 - Devolver ----------

    @Test
    public void devolverMudaEstadoParaDevolvidoComDataDeHoje() {
        Loan loan = activeLoan(USER, BOOK_ID);
        when(api.updateLoan(any(), any())).thenReturn(FakeCall.sucesso(loan));

        repository.returnLoan(loan, new TestCallback<>());

        ArgumentCaptor<Loan> changes = ArgumentCaptor.forClass(Loan.class);
        verify(api).updateLoan(any(), changes.capture());
        assertEquals(Loan.STATUS_RETURNED, changes.getValue().getStatus());
        assertEquals(DateUtils.today(), changes.getValue().getReturnDate());
    }

    // ---------- UC14 - Renovar ----------

    @Test
    public void renovarSoma14DiasADataLimite() {
        String dueDate = DateUtils.addDays(DateUtils.today(), 3);
        Loan loan = new Loan(USER, BOOK_ID, "T", "A", null, DateUtils.today(), dueDate);
        when(api.updateLoan(any(), any())).thenReturn(FakeCall.sucesso(loan));

        repository.renewLoan(loan, new TestCallback<>());

        ArgumentCaptor<Loan> changes = ArgumentCaptor.forClass(Loan.class);
        verify(api).updateLoan(any(), changes.capture());
        assertEquals(DateUtils.addDays(dueDate, LoanRepository.LOAN_DAYS), changes.getValue().getDueDate());
    }

    @Test
    public void naoPodeRenovarEmprestimoEmAtraso() {
        Loan loan = new Loan(USER, BOOK_ID, "T", "A", null, "2020-01-01", "2020-01-15");

        TestCallback<Loan> callback = new TestCallback<>();
        repository.renewLoan(loan, callback);

        assertEquals("Este empréstimo está em atraso. Tens de devolver o livro.", callback.error);
        verify(api, never()).updateLoan(any(), any());
    }

    // ---------- UC6 / UC7 - Consultar ----------

    @Test
    public void consultarEmprestimosFiltraPorUtilizadorEEstado() {
        when(api.getLoans(anyString())).thenReturn(FakeCall.sucesso(new ArrayList<>()));

        repository.getUserLoans(USER, Loan.STATUS_RETURNED, new TestCallback<>());

        verify(api).getLoans(eq("{\"userId\":\"" + USER + "\",\"status\":\"DEVOLVIDO\"}"));
    }
}
