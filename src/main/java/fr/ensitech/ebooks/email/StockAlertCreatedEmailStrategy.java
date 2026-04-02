package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class StockAlertCreatedEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public StockAlertCreatedEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String firstname = (String) params[0];
        String bookTitle = (String) params[1];
        
        String subject = "Alerte de disponibilité activée";
        String body = String.format(
                "Bonjour %s,\n\n" +
                "Votre alerte de disponibilité pour le livre \"%s\" a bien été activée.\n\n" +
                "Vous recevrez un email dès que ce livre sera de nouveau disponible en stock.\n\n" +
                "Cordialement,\n" +
                "L'équipe de la bibliothèque",
                firstname, bookTitle
        );
        
        emailService.sendEmail(to, subject, body);
    }
}

