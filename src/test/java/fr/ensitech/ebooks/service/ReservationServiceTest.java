package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.*;
import fr.ensitech.ebooks.repository.*;
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
 * Tests unitaires pour la classe ReservationService
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private ICartItemRepository cartItemRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private ILoanRepository loanRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private Book book;
    private CartItem cartItem;
    private Reservation pendingReservation;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("Paul")
                .lastname("Dubois")
                .email("paul.dubois@test.com")
                .password("Password123@")
                .birthdate(LocalDate.of(1988, 12, 10))
                .phoneNumber("0698765432")
                .enabled(true)
                .build();

        book = Book.builder()
                .id(10L)
                .title("L'Étranger")
                .description("Roman d'Albert Camus")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Albert Camus")
                .category("Philosophie")
                .quantity(2)
                .build();

        cartItem = CartItem.builder()
                .id(100L)
                .user(user)
                .book(book)
                .loanDuration(14)
                .build();

        pendingReservation = Reservation.builder()
                .id(50L)
                .user(user)
                .book(book)
                .loanDuration(14)
                .status(Reservation.ReservationStatus.PENDING)
                .reservationDate(LocalDateTime.now())
                .expirationDate(LocalDateTime.now().plusHours(72))
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        book = null;
        cartItem = null;
        pendingReservation = null;
    }

    // ============ TESTS createReservationsFromCart ============

    @Test
    void shouldCreateReservationsFromCartSuccessfully() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(50L);
            // Simuler @PrePersist : expirationDate est requis par formatReservations()
            if (r.getReservationDate() == null) r.setReservationDate(LocalDateTime.now());
            if (r.getExpirationDate() == null) r.setExpirationDate(LocalDateTime.now().plusHours(72));
            return r;
        });
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        doNothing().when(cartItemRepository).deleteByUserId(1L);

        // WHEN
        List<Reservation> result = reservationService.createReservationsFromCart(1L);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser()).isEqualTo(user);
        assertThat(result.get(0).getBook()).isEqualTo(book);
        assertThat(result.get(0).getStatus()).isEqualTo(Reservation.ReservationStatus.PENDING);
        assertThat(book.getQuantity()).isEqualTo(1); // stock décrémenté
        verify(cartItemRepository).deleteByUserId(1L);
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenUserNotFoundForReservation() {
        // GIVEN
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.createReservationsFromCart(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");

        verify(cartItemRepository, never()).findByUserId(anyLong());
    }

    @Test
    void shouldThrowWhenCartIsEmpty() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of());

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.createReservationsFromCart(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Le panier est vide");
    }

    @Test
    void shouldThrowAndSkipUnavailableBooksFromCart() {
        // GIVEN - le livre est en rupture de stock
        book.setQuantity(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        // WHEN / THEN - doit lancer une exception avec les livres indisponibles
        assertThatThrownBy(() -> reservationService.createReservationsFromCart(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Livres non disponibles")
                .hasMessageContaining("L'Étranger");

        verify(reservationRepository, never()).save(any());
    }

    // ============ TESTS cancelReservation (avec userId) ============

    @Test
    void shouldCancelReservationByUserSuccessfully() {
        // GIVEN
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        int quantityBefore = book.getQuantity();

        // WHEN
        Reservation result = reservationService.cancelReservation(50L, 1L);

        // THEN
        assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.CANCELLED);
        assertThat(result.getCancelledAt()).isNotNull();
        assertThat(book.getQuantity()).isEqualTo(quantityBefore + 1); // stock réincrémenté
        verify(bookRepository).save(book);
        verify(reservationRepository).save(pendingReservation);
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenReservationNotFoundForCancelByUser() {
        // GIVEN
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.cancelReservation(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Réservation non trouvée");
    }

    @Test
    void shouldThrowWhenUserNotAuthorizedToCancelReservation() {
        // GIVEN - réservation appartient à userId=1, userId=5 tente d'annuler
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.cancelReservation(50L, 5L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non autorisé");
    }

    @Test
    void shouldThrowWhenCancellingNonPendingReservation() {
        // GIVEN - réservation déjà convertie
        pendingReservation.setStatus(Reservation.ReservationStatus.CONVERTED);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.cancelReservation(50L, 1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Seules les réservations en attente peuvent être annulées");
    }

    // ============ TESTS validateReservation (bibliothécaire) ============

    @Test
    void shouldValidateReservationAndCreateLoanSuccessfully() {
        // GIVEN
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        // WHEN
        Reservation result = reservationService.validateReservation(50L, 99L);

        // THEN
        assertThat(result.getStatus()).isEqualTo(Reservation.ReservationStatus.CONVERTED);
        assertThat(result.getValidatedBy()).isEqualTo(99L);
        assertThat(result.getValidatedAt()).isNotNull();
        assertThat(result.getConvertedToLoanAt()).isNotNull();
        verify(loanRepository).save(any(Loan.class));
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }

    @Test
    void shouldThrowWhenValidatingNonPendingReservation() {
        // GIVEN - réservation déjà annulée
        pendingReservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.validateReservation(50L, 99L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cette réservation ne peut pas être validée");

        verify(loanRepository, never()).save(any());
    }

    // ============ TESTS cancelReservation (bibliothécaire, sans userId) ============

    @Test
    void shouldCancelReservationByLibrarianSuccessfully() {
        // GIVEN
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        int quantityBefore = book.getQuantity();

        // WHEN
        reservationService.cancelReservation(50L);

        // THEN
        assertThat(pendingReservation.getStatus()).isEqualTo(Reservation.ReservationStatus.CANCELLED);
        assertThat(book.getQuantity()).isEqualTo(quantityBefore + 1);
        verify(reservationRepository).save(pendingReservation);
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledReservation() {
        // GIVEN
        pendingReservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.cancelReservation(50L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cette réservation ne peut pas être annulée");
    }

    @Test
    void shouldThrowWhenCancellingAlreadyConvertedReservation() {
        // GIVEN
        pendingReservation.setStatus(Reservation.ReservationStatus.CONVERTED);
        when(reservationRepository.findById(50L)).thenReturn(Optional.of(pendingReservation));

        // WHEN / THEN
        assertThatThrownBy(() -> reservationService.cancelReservation(50L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cette réservation ne peut pas être annulée");
    }

    // ============ TESTS findPendingReservations ============

    @Test
    void shouldReturnPendingReservations() {
        // GIVEN
        when(reservationRepository.findByStatus(Reservation.ReservationStatus.PENDING))
                .thenReturn(List.of(pendingReservation));

        // WHEN
        List<Reservation> result = reservationService.findPendingReservations();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(Reservation.ReservationStatus.PENDING);
        verify(reservationRepository).findByStatus(Reservation.ReservationStatus.PENDING);
    }

    // ============ TESTS getUserReservations ============

    @Test
    void shouldReturnUserReservations() {
        // GIVEN
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of(pendingReservation));

        // WHEN
        List<Reservation> result = reservationService.getUserReservations(1L);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUser().getId()).isEqualTo(1L);
        verify(reservationRepository).findByUserId(1L);
    }

    // ============ TESTS checkAndExpireReservations ============

    @Test
    void shouldExpireExpiredReservationsAndRestockBook() {
        // GIVEN
        pendingReservation.setExpirationDate(LocalDateTime.now().minusHours(1)); // déjà expirée
        when(reservationRepository.findByStatusAndExpirationDateBefore(
                eq(Reservation.ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingReservation));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(pendingReservation);

        int quantityBefore = book.getQuantity();

        // WHEN
        reservationService.checkAndExpireReservations();

        // THEN
        assertThat(pendingReservation.getStatus()).isEqualTo(Reservation.ReservationStatus.EXPIRED);
        assertThat(book.getQuantity()).isEqualTo(quantityBefore + 1); // stock réincrémenté
        verify(bookRepository).save(book);
        verify(reservationRepository).save(pendingReservation);
        verify(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());
    }
}


