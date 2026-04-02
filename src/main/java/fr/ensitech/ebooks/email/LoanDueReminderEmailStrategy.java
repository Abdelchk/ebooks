package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class LoanDueReminderEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    
    public LoanDueReminderEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle
        // params[2] = dueDate (formatted)
        // params[3] = daysRemaining

        if (params.length < 4) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de rappel d'échéance");
        }
        
        String firstName = (String) params[0];
        String bookTitle = (String) params[1];
        String dueDate = (String) params[2];
        String daysRemaining = (String) params[3];

        String subject = String.format("⏰ Rappel : retour de livre dans %s jour(s)", daysRemaining);
        String content = String.format(
            "Bonjour %s,\n\n" +
            "Ceci est un rappel concernant votre emprunt :\n\n" +
            "📚 Livre : \"%s\"\n" +
            "📅 Date de retour : %s\n" +
            "⏰ Temps restant : %s jour(s)\n\n" +
            "💡 Conseil : Vous pouvez prolonger votre emprunt depuis votre espace personnel (si non déjà prolongé 2 fois).\n" +
            "⚠️ Un retard entraînera des pénalités.\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle,
            dueDate,
            daysRemaining
        );
        
        emailService.sendEmail(to, subject, content);
    }
}

