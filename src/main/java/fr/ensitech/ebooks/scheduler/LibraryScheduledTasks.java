package fr.ensitech.ebooks.scheduler;

import fr.ensitech.ebooks.service.IReservationService;
import fr.ensitech.ebooks.service.ILoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LibraryScheduledTasks {

    @Autowired
    private IReservationService reservationService;

    @Autowired
    private ILoanService loanService;

    // Vérifier les réservations expirées toutes les heures
    @Scheduled(cron = "0 0 * * * *")
    public void checkExpiredReservations() {
        try {
            reservationService.checkAndExpireReservations();
            System.out.println("✓ Vérification réservations expirées effectuée");
        } catch (Exception e) {
            System.err.println("✗ Erreur vérification réservations: " + e.getMessage());
        }
    }

    // Envoyer rappels emprunts à rendre tous les jours à 9h
    @Scheduled(cron = "0 0 9 * * *")
    public void sendLoanReminders() {
        try {
            loanService.checkAndNotifyUpcomingDueDates();
            System.out.println("✓ Rappels emprunts envoyés");
        } catch (Exception e) {
            System.err.println("✗ Erreur envoi rappels: " + e.getMessage());
        }
    }

    // Vérifier emprunts en retard tous les jours à 10h
    @Scheduled(cron = "0 0 10 * * *")
    public void checkOverdueLoans() {
        try {
            loanService.checkAndNotifyOverdueLoans();
            System.out.println("✓ Emprunts en retard vérifiés");
        } catch (Exception e) {
            System.err.println("✗ Erreur vérification retards: " + e.getMessage());
        }
    }
}

