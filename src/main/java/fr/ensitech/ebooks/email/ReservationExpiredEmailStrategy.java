package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class ReservationExpiredEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    
    public ReservationExpiredEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle

        if (params.length < 2) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de réservation expirée");
        }
        
        String firstName = (String) params[0];
        String bookTitle = (String) params[1];

        String subject = "⚠️ Réservation expirée";
        String content = String.format(
            "Bonjour %s,\n\n" +
            "Votre réservation pour le livre \"%s\" a expiré car elle n'a pas été retirée dans les délais (72 heures).\n\n" +
            "Le livre est à nouveau disponible pour les autres utilisateurs.\n\n" +
            "💡 Conseil : Pensez à retirer vos réservations plus rapidement la prochaine fois.\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle
        );
        
        emailService.sendEmail(to, subject, content);
    }
}

