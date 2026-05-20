package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.email.*;
import fr.ensitech.ebooks.entity.*;
import fr.ensitech.ebooks.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAlertService implements IStockAlertService {

    private final IStockAlertRepository stockAlertRepository;
    private final IUserRepository userRepository;
    private final IBookRepository bookRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public StockAlert createAlert(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé"));

        // Vérifier si une alerte active existe déjà
        var existingAlert = stockAlertRepository.findActiveAlertByUserAndBook(userId, bookId);
        if (existingAlert.isPresent()) {
            throw new IllegalStateException("Vous avez déjà une alerte active pour ce livre");
        }

        // Créer l'alerte
        StockAlert alert = StockAlert.builder()
                .user(user)
                .book(book)
                .status(StockAlert.AlertStatus.ACTIVE)
                .build();

        alert = stockAlertRepository.save(alert);

        // Envoyer email de confirmation avec pattern Strategy
        EmailContext emailContext = new EmailContext();
        emailContext.setStrategy(new StockAlertCreatedEmailStrategy(emailService));
        emailContext.executeStrategy(
                user.getEmail(),
                user.getFirstname(),
                book.getTitle()
        );

        return alert;
    }

    @Override
    @Transactional
    public StockAlert cancelAlert(Long alertId, Long userId) {
        StockAlert alert = stockAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alerte non trouvée"));

        if (!alert.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Non autorisé");
        }

        if (alert.getStatus() != StockAlert.AlertStatus.ACTIVE) {
            throw new IllegalStateException("Cette alerte ne peut pas être annulée");
        }

        alert.setStatus(StockAlert.AlertStatus.CANCELLED);
        return stockAlertRepository.save(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockAlert> getUserAlerts(Long userId) {
        return stockAlertRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void notifyUsersForBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé"));

        if (book.getQuantity() <= 0) {
            return; // Pas de stock, ne pas notifier
        }

        List<StockAlert> activeAlerts = stockAlertRepository.findActiveAlertsByBook(bookId);

        for (StockAlert alert : activeAlerts) {
            // Envoyer email avec pattern Strategy
            EmailContext emailContext = new EmailContext();
            emailContext.setStrategy(new StockAvailableEmailStrategy(emailService));
            emailContext.executeStrategy(
                    alert.getUser().getEmail(),
                    alert.getUser().getFirstname(),
                    book.getTitle(),
                    String.valueOf(book.getQuantity())
            );

            // Marquer comme notifié
            alert.setStatus(StockAlert.AlertStatus.NOTIFIED);
            alert.setNotifiedAt(LocalDateTime.now());
            stockAlertRepository.save(alert);
        }
    }
}
