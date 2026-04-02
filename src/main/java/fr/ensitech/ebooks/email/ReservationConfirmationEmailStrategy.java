package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.entity.Reservation;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.EmailService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReservationConfirmationEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public ReservationConfirmationEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = reservationsInfo (formatted string)

        if (params.length < 2) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de confirmation de réservation");
        }
        
        String firstName = (String) params[0];
        String reservationsInfo = (String) params[1];

        String subject = "✓ Confirmation de vos réservations";
        StringBuilder content = new StringBuilder();
        content.append("Bonjour ").append(firstName).append(",\n\n");
        content.append("Vos réservations ont été confirmées :\n\n");
        content.append(reservationsInfo);
        content.append("\n\n📍 Merci de retirer vos livres dans les 72 heures.");
        content.append("\n\n💡 Vous pouvez annuler vos réservations depuis votre espace personnel.");
        content.append("\n\nCordialement,\nL'équipe Ebooks");
        
        emailService.sendEmail(to, subject, content.toString());
    }
    
    // Méthode utilitaire pour formater les réservations
    public static String formatReservations(List<Reservation> reservations) {
        StringBuilder info = new StringBuilder();
        for (Reservation res : reservations) {
            info.append("📚 ").append(res.getBook().getTitle())
                .append("\n   À retirer avant le : ")
                .append(res.getExpirationDate().format(DATE_FORMATTER))
                .append("\n   Durée d'emprunt prévue : ")
                .append(res.getLoanDuration()).append(" jours\n\n");
        }
        return info.toString();
    }
}

