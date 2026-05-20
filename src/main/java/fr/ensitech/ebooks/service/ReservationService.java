package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.email.*;
import fr.ensitech.ebooks.entity.*;
import fr.ensitech.ebooks.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService implements IReservationService {

    private static final String RESERVATION_NOT_FOUND = "Réservation non trouvée";

    private final IReservationRepository reservationRepository;
    private final ICartItemRepository cartItemRepository;
    private final IBookRepository bookRepository;
    private final IUserRepository userRepository;
    private final ILoanRepository loanRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public List<Reservation> createReservationsFromCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Le panier est vide");
        }

        List<Reservation> reservations = new ArrayList<>();
        List<String> unavailableBooks = new ArrayList<>();

        for (CartItem item : cartItems) {
            Book book = item.getBook();

            // Vérifier la disponibilité
            if (book.getQuantity() <= 0) {
                unavailableBooks.add(book.getTitle());
                continue;
            }

            // Créer la réservation
            Reservation reservation = Reservation.builder()
                    .user(user)
                    .book(book)
                    .loanDuration(item.getLoanDuration())
                    .status(Reservation.ReservationStatus.PENDING)
                    .build();

            reservations.add(reservationRepository.save(reservation));

            // Décrémenter le stock
            book.setQuantity(book.getQuantity() - 1);
            bookRepository.save(book);
        }

        if (!unavailableBooks.isEmpty()) {
            throw new IllegalStateException("Livres non disponibles : " + String.join(", ", unavailableBooks));
        }

        // Vider le panier
        cartItemRepository.deleteByUserId(userId);

        // Envoyer email avec le pattern Strategy
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new ReservationConfirmationEmailStrategy(emailService));
        String reservationsInfo = ReservationConfirmationEmailStrategy.formatReservations(reservations);
        emailContext.executeStrategy(user.getEmail(), user.getFirstname(), reservationsInfo);

        return reservations;
    }

    @Override
    @Transactional
    public Reservation cancelReservation(Long reservationId, Long userId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Non autorisé");
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new IllegalStateException("Seules les réservations en attente peuvent être annulées");
        }

        // Mettre à jour le statut
        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        // Réincrémenter le stock
        Book book = reservation.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        // Envoyer email avec le pattern Strategy
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new ReservationCancellationEmailStrategy(emailService));
        emailContext.executeStrategy(
                reservation.getUser().getEmail(),
                reservation.getUser().getFirstname(),
                reservation.getBook().getTitle()
        );

        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(Long userId) {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void checkAndExpireReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository
                .findByStatusAndExpirationDateBefore(Reservation.ReservationStatus.PENDING, now);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(Reservation.ReservationStatus.EXPIRED);

            // Réincrémenter le stock
            Book book = reservation.getBook();
            book.setQuantity(book.getQuantity() + 1);
            bookRepository.save(book);

            reservationRepository.save(reservation);

            // Envoyer email avec le pattern Strategy
            EmailContext emailContext = new EmailContext();
            emailContext.setStrategy(new ReservationExpiredEmailStrategy(emailService));
            emailContext.executeStrategy(
                    reservation.getUser().getEmail(),
                    reservation.getUser().getFirstname(),
                    reservation.getBook().getTitle()
            );
        }
    }

    @Override
    @Transactional
    public Reservation convertToLoan(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != Reservation.ReservationStatus.VALIDATED &&
            reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new IllegalStateException("Cette réservation ne peut pas être convertie");
        }

        reservation.setStatus(Reservation.ReservationStatus.CONVERTED);
        reservation.setConvertedToLoanAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }

    // Nouvelles méthodes pour le bibliothécaire

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findPendingReservations() {
        return reservationRepository.findByStatus(Reservation.ReservationStatus.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByStatus(Reservation.ReservationStatus status) {
        return reservationRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    @Override
    @Transactional
    public Reservation validateReservation(Long reservationId, Long librarianId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new IllegalStateException("Cette réservation ne peut pas être validée");
        }

        LocalDateTime now = LocalDateTime.now();

        // Créer l'emprunt directement
        Loan loan = Loan.builder()
                .user(reservation.getUser())
                .book(reservation.getBook())
                .reservation(reservation)
                .loanDate(now)
                .dueDate(now.plusDays(reservation.getLoanDuration()))
                .initialDuration(reservation.getLoanDuration())
                .extensionCount(0)
                .status(Loan.LoanStatus.ACTIVE)
                .build();
        loanRepository.save(loan);

        // Passer directement en CONVERTED (pas de VALIDATED intermédiaire)
        reservation.setStatus(Reservation.ReservationStatus.CONVERTED);
        reservation.setValidatedAt(now);
        reservation.setValidatedBy(librarianId);
        reservation.setConvertedToLoanAt(now);

        Reservation savedReservation = reservationRepository.save(reservation);

        // Envoyer email de confirmation de l'emprunt
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new ReservationValidatedEmailStrategy(emailService));
        emailContext.executeStrategy(
                reservation.getUser().getEmail(),
                reservation.getUser().getFirstname(),
                reservation.getBook().getTitle()
        );

        return savedReservation;
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(RESERVATION_NOT_FOUND));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
            reservation.getStatus() == Reservation.ReservationStatus.CONVERTED) {
            throw new IllegalStateException("Cette réservation ne peut pas être annulée");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());

        // Réincrémenter le stock
        Book book = reservation.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        reservationRepository.save(reservation);
    }
}
