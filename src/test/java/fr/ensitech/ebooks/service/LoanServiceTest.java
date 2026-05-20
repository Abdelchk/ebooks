package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.*;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.repository.ILoanRepository;
import fr.ensitech.ebooks.repository.IReservationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe LoanService
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private ILoanRepository loanRepository;

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Book book;
    private Reservation reservation;
    private Loan activeLoan;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("Alice")
                .lastname("Martin")
                .email("alice.martin@test.com")
                .password("Password123@")
                .birthdate(LocalDate.of(1990, 6, 15))
                .phoneNumber("0612345678")
                .enabled(true)
                .build();

        book = Book.builder()
                .id(10L)
                .title("Don Quichotte")
                .description("Roman de Cervantes")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Cervantes")
                .category("Classique")
                .quantity(3)
                .build();

        reservation = Reservation.builder()
                .id(50L)
                .user(user)
                .book(book)
                .loanDuration(14)
                .status(Reservation.ReservationStatus.PENDING)
                .reservationDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusHours(72))
                .build();

        activeLoan = Loan.builder()
                .id(200L)
                .user(user)
                .book(book)
                .reservation(reservation)
                .loanDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14))
                .initialDuration(14)
                .extensionCount(0)
                .status(Loan.LoanStatus.ACTIVE)
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        book = null;
        reservation = null;
        activeLoan = null;
    }

    // ============ TESTS createLoanFromReservation ============

    @Test
    void shouldCreateLoanFromReservationSuccessfully() {
        // GIVEN
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan l = invocation.getArgument(0);
            l.setId(200L);
            return l;
        });
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // WHEN
        Loan result = loanService.createLoanFromReservation(50L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getStatus()).isEqualTo(Loan.LoanStatus.ACTIVE);
        assertThat(result.getExtensionCount()).isZero();
        assertThat(result.getDueDate()).isAfter(LocalDateTime.now());

        verify(loanRepository).save(any(Loan.class));
        verify(reservationRepository).save(reservation);
        assertThat(reservation.getStatus()).isEqualTo(Reservation.ReservationStatus.CONVERTED);
    }

    @Test
    void shouldThrowWhenReservationNotFoundForLoan() {
        // GIVEN
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.createLoanFromReservation(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Réservation non trouvée");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenReservationIsNotPending() {
        // GIVEN - réservation déjà convertie
        reservation.setStatus(Reservation.ReservationStatus.CONVERTED);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(reservation));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.createLoanFromReservation(50L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cette réservation ne peut pas être convertie en emprunt");

        verify(loanRepository, never()).save(any());
    }

    // ============ TESTS extendLoan ============

    @Test
    void shouldExtendLoanSuccessfully() {
        // GIVEN
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(activeLoan);

        LocalDateTime dueDateBefore = activeLoan.getDueDate();

        // WHEN
        Loan result = loanService.extendLoan(200L, 1L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getExtensionCount()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(Loan.LoanStatus.EXTENDED);
        assertThat(result.getDueDate()).isEqualTo(dueDateBefore.plusDays(7));
        verify(loanRepository).save(activeLoan);
    }

    @Test
    void shouldThrowWhenLoanNotFoundForExtend() {
        // GIVEN
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.extendLoan(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Emprunt non trouvé");
    }

    @Test
    void shouldThrowWhenUserNotAuthorizedToExtendLoan() {
        // GIVEN - le prêt appartient à userId=1, userId=5 tente de prolonger
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.extendLoan(200L, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non autorisé");
    }

    @Test
    void shouldThrowWhenLoanAlreadyReturned() {
        // GIVEN
        activeLoan.setStatus(Loan.LoanStatus.RETURNED);
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.extendLoan(200L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cet emprunt est déjà terminé");
    }

    @Test
    void shouldThrowWhenMaxExtensionsReached() {
        // GIVEN - déjà 2 prolongations (max)
        activeLoan.setExtensionCount(2);
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.extendLoan(200L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nombre maximum de prolongations atteint");
    }

    @Test
    void shouldThrowWhenExtendingOverdueLoan() {
        // GIVEN - emprunt en retard (dueDate dans le passé, pas de returnDate)
        activeLoan.setDueDate(LocalDateTime.now().minusDays(3));
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.extendLoan(200L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Impossible de prolonger un emprunt en retard");
    }

    // ============ TESTS returnLoan ============

    @Test
    void shouldReturnLoanSuccessfully() {
        // GIVEN
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(loanRepository.save(any(Loan.class))).thenReturn(activeLoan);

        int quantityBefore = book.getQuantity();

        // WHEN
        Loan result = loanService.returnLoan(200L, 1L);

        // THEN
        assertThat(result.getStatus()).isEqualTo(Loan.LoanStatus.RETURNED);
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(book.getQuantity()).isEqualTo(quantityBefore + 1); // stock réincrémenté
        verify(bookRepository).save(book);
        verify(loanRepository).save(activeLoan);
    }

    @Test
    void shouldThrowWhenLoanNotFoundForReturn() {
        // GIVEN
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.returnLoan(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Emprunt non trouvé");
    }

    @Test
    void shouldThrowWhenUserNotAuthorizedToReturnLoan() {
        // GIVEN - prêt appartient à userId=1, userId=5 tente de rendre
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.returnLoan(200L, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non autorisé");
    }

    @Test
    void shouldThrowWhenLoanAlreadyReturnedOnReturn() {
        // GIVEN
        activeLoan.setStatus(Loan.LoanStatus.RETURNED);
        when(loanRepository.findById(200L)).thenReturn(Optional.of(activeLoan));

        // WHEN / THEN
        assertThatThrownBy(() -> loanService.returnLoan(200L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ce livre a déjà été rendu");
    }

    // ============ TESTS getUserLoans ============

    @Test
    void shouldReturnUserLoansList() {
        // GIVEN
        when(loanRepository.findByUserId(1L)).thenReturn(List.of(activeLoan));

        // WHEN
        List<Loan> result = loanService.getUserLoans(1L);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(1L);
        verify(loanRepository).findByUserId(1L);
    }

    // ============ TESTS getActiveLoans ============

    @Test
    void shouldReturnOnlyActiveLoansWithoutReturnDate() {
        // GIVEN
        Loan returnedLoan = Loan.builder()
                .id(201L)
                .user(user)
                .book(book)
                .loanDate(LocalDateTime.now().minusDays(20))
                .dueDate(LocalDateTime.now().minusDays(6))
                .returnDate(LocalDateTime.now().minusDays(7)) // déjà rendu
                .initialDuration(14)
                .extensionCount(0)
                .status(Loan.LoanStatus.ACTIVE) // mal marqué, mais returnDate présente
                .build();

        when(loanRepository.findByUserIdAndStatus(1L, Loan.LoanStatus.ACTIVE))
                .thenReturn(List.of(activeLoan, returnedLoan));

        // WHEN
        List<Loan> result = loanService.getActiveLoans(1L);

        // THEN - seul le prêt sans returnDate doit être retourné
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(200L);
    }
}

