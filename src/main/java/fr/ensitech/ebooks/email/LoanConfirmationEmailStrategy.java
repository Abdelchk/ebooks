package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class LoanConfirmationEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    
    public LoanConfirmationEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle
        // params[2] = dueDate (formatted)

        if (params.length < 3) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de confirmation d'emprunt");
        }
        
        String firstName = (String) params[0];
        String bookTitle = (String) params[1];
        String dueDate = (String) params[2];

        String subject = "✓ Confirmation d'emprunt";
        String content = String.format(
            "Bonjour %s,\n\n" +
            "Votre emprunt du livre \"%s\" est confirmé.\n\n" +
            "📅 Date de retour prévue : %s\n\n" +
            "💡 Vous pouvez prolonger votre emprunt jusqu'à 2 fois depuis votre espace personnel.\n" +
            "⚠️ Pensez à rendre le livre à temps pour éviter les pénalités.\n\n" +
            "Bonne lecture !\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle,
            dueDate
        );
        
        emailService.sendEmail(to, subject, content);
    }
}

