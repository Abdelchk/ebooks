package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class StockAvailableEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public StockAvailableEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String firstname = (String) params[0];
        String bookTitle = (String) params[1];
        String quantity = (String) params[2];
        
        String subject = "📚 Livre de nouveau disponible !";
        String body = String.format(
                "Bonjour %s,\n\n" +
                "Bonne nouvelle ! Le livre \"%s\" que vous aviez mis en alerte est de nouveau disponible.\n\n" +
                "Stock actuel : %s exemplaire(s)\n\n" +
                "Nous vous conseillons de le réserver rapidement avant qu'il ne soit de nouveau en rupture de stock.\n\n" +
                "Connectez-vous à votre compte pour ajouter ce livre à votre panier.\n\n" +
                "Cordialement,\n" +
                "L'équipe de la bibliothèque",
                firstname, bookTitle, quantity
        );
        
        emailService.sendEmail(to, subject, body);
    }
}

