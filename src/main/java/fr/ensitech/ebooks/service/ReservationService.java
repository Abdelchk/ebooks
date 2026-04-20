package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.email.*;
import fr.ensitech.ebooks.entity.*;
import fr.ensitech.ebooks.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
public class ReservationService implements IReservationService {

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private ICartItemRepository cartItemRepository;

    @Autowired
    private IBookRepository bookRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ILoanRepository loanRepository;

    @Autowired
    private EmailService emailService;

    @Override
    @Transactional
    public List<Reservation> createReservationsFromCart(Long userId) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Utilisateur non trouvé"));

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new Exception("Le panier est vide");
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
            throw new Exception("Livres non disponibles : " + String.join(", ", unavailableBooks));
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
    public Reservation cancelReservation(Long reservationId, Long userId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new Exception("Non autorisé");
        }

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new Exception("Seules les réservations en attente peuvent être annulées");
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
    public List<Reservation> getUserReservations(Long userId) throws Exception {
        return reservationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void checkAndExpireReservations() throws Exception {
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
    public Reservation convertToLoan(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (reservation.getStatus() != Reservation.ReservationStatus.VALIDATED &&
            reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new Exception("Cette réservation ne peut pas être convertie");
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
    public Reservation validateReservation(Long reservationId, Long librarianId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new Exception("Cette réservation ne peut pas être validée");
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
    public void cancelReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
            reservation.getStatus() == Reservation.ReservationStatus.CONVERTED) {
            throw new Exception("Cette réservation ne peut pas être annulée");
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

