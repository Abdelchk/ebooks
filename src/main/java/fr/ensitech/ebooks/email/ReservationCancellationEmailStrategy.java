package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class ReservationCancellationEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    
    public ReservationCancellationEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle

        if (params.length < 2) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email d'annulation de réservation");
        }
        
        String firstName = (String) params[0];
        String bookTitle = (String) params[1];

        String subject = "✓ Annulation de réservation confirmée";
        String content = String.format(
            "Bonjour %s,\n\n" +
            "Votre réservation pour le livre \"%s\" a été annulée avec succès.\n\n" +
            "Le livre est à nouveau disponible pour les autres utilisateurs.\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle
        );
        
        emailService.sendEmail(to, subject, content);
    }
}

