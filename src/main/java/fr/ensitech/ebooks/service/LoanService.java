package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.email.*;
import fr.ensitech.ebooks.entity.*;
import fr.ensitech.ebooks.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService implements ILoanService {

    @Autowired
    private ILoanRepository loanRepository;

    @Autowired
    private IReservationRepository reservationRepository;

    @Autowired
    private IBookRepository bookRepository;

    @Autowired
    private EmailService emailService;

    private static final int MAX_EXTENSIONS = 2;
    private static final int EXTENSION_DAYS = 7;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    public Loan createLoanFromReservation(Long reservationId) throws Exception {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new Exception("Réservation non trouvée"));

        if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
            throw new Exception("Cette réservation ne peut pas être convertie en emprunt");
        }

        // Créer l'emprunt
        LocalDateTime now = LocalDateTime.now();
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

        loan = loanRepository.save(loan);

        // Mettre à jour la réservation
        reservation.setStatus(Reservation.ReservationStatus.CONVERTED);
        reservation.setConvertedToLoanAt(now);
        reservationRepository.save(reservation);

        // Envoyer email avec pattern Strategy
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new LoanConfirmationEmailStrategy(emailService));
        emailContext.executeStrategy(
            reservation.getUser().getEmail(),
            reservation.getUser().getFirstname(),
            loan.getBook().getTitle(),
            loan.getDueDate().format(DATE_FORMATTER)
        );

        return loan;
    }

    @Override
    @Transactional
    public Loan extendLoan(Long loanId, Long userId) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new Exception("Emprunt non trouvé"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new Exception("Non autorisé");
        }

        if (loan.getStatus() == Loan.LoanStatus.RETURNED) {
            throw new Exception("Cet emprunt est déjà terminé");
        }

        if (loan.getExtensionCount() >= MAX_EXTENSIONS) {
            throw new Exception("Nombre maximum de prolongations atteint (2)");
        }

        if (loan.isOverdue()) {
            throw new Exception("Impossible de prolonger un emprunt en retard");
        }

        // Prolonger
        loan.setDueDate(loan.getDueDate().plusDays(EXTENSION_DAYS));
        loan.setExtensionCount(loan.getExtensionCount() + 1);
        loan.setStatus(Loan.LoanStatus.EXTENDED);

        loan = loanRepository.save(loan);

        // Envoyer email avec pattern Strategy
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new LoanExtensionEmailStrategy(emailService));
        emailContext.executeStrategy(
            loan.getUser().getEmail(),
            loan.getUser().getFirstname(),
            loan.getBook().getTitle(),
            loan.getDueDate().format(DATE_FORMATTER),
            String.valueOf(loan.getExtensionCount())
        );

        return loan;
    }

    @Override
    @Transactional
    public Loan returnLoan(Long loanId, Long userId) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new Exception("Emprunt non trouvé"));

        if (!loan.getUser().getId().equals(userId)) {
            throw new Exception("Non autorisé");
        }

        if (loan.getStatus() == Loan.LoanStatus.RETURNED) {
            throw new Exception("Ce livre a déjà été rendu");
        }

        boolean wasLate = loan.isOverdue();

        // Marquer comme rendu
        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(Loan.LoanStatus.RETURNED);

        // Réincrémenter le stock
        Book book = loan.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        loan = loanRepository.save(loan);

        // Envoyer email avec pattern Strategy
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new LoanReturnEmailStrategy(emailService));
        emailContext.executeStrategy(
            loan.getUser().getEmail(),
            loan.getUser().getFirstname(),
            loan.getBook().getTitle(),
            wasLate
        );

        return loan;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getUserLoans(Long userId) throws Exception {
        return loanRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Loan> getActiveLoans(Long userId) throws Exception {
        return loanRepository.findByUserIdAndStatus(userId, Loan.LoanStatus.ACTIVE)
                .stream()
                .filter(loan -> loan.getReturnDate() == null)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void checkAndNotifyUpcomingDueDates() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in3Days = now.plusDays(3);

        // Rappel 3 jours avant
        List<Loan> upcomingLoans = loanRepository.findLoansNearingDueDate(
                Loan.LoanStatus.ACTIVE, now, in3Days);

        for (Loan loan : upcomingLoans) {
            if (loan.getFirstReminderSentAt() == null) {
                EmailContext emailContext = new EmailContext();
                emailContext.setStrategy(new LoanDueReminderEmailStrategy(emailService));
                emailContext.executeStrategy(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstname(),
                    loan.getBook().getTitle(),
                    loan.getDueDate().format(DATE_FORMATTER),
                    "3"
                );
                loan.setFirstReminderSentAt(now);
                loanRepository.save(loan);
            }
        }

        // Rappel 1 jour avant
        LocalDateTime tomorrow = now.plusDays(1);
        List<Loan> tomorrowLoans = loanRepository.findLoansNearingDueDate(
                Loan.LoanStatus.ACTIVE, now, tomorrow);

        for (Loan loan : tomorrowLoans) {
            if (loan.getSecondReminderSentAt() == null) {
                EmailContext emailContext = new EmailContext();
                emailContext.setStrategy(new LoanDueReminderEmailStrategy(emailService));
                emailContext.executeStrategy(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstname(),
                    loan.getBook().getTitle(),
                    loan.getDueDate().format(DATE_FORMATTER),
                    "1"
                );
                loan.setSecondReminderSentAt(now);
                loanRepository.save(loan);
            }
        }
    }

    @Override
    @Transactional
    public void checkAndNotifyOverdueLoans() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        List<Loan> overdueLoans = loanRepository.findOverdueLoans(now);

        for (Loan loan : overdueLoans) {
            loan.setStatus(Loan.LoanStatus.OVERDUE);

            if (loan.getOverdueNoticeSentAt() == null) {
                EmailContext emailContext = new EmailContext();
                emailContext.setStrategy(new LoanOverdueEmailStrategy(emailService));
                emailContext.executeStrategy(
                    loan.getUser().getEmail(),
                    loan.getUser().getFirstname(),
                    loan.getBook().getTitle(),
                    String.valueOf(loan.getDaysOverdue())
                );
                loan.setOverdueNoticeSentAt(now);
            }

            loanRepository.save(loan);
        }
    }
}

