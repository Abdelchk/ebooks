package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class LoanOverdueEmailStrategy implements EmailStrategy {

    private final EmailService emailService;

    public LoanOverdueEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle
        // params[2] = daysOverdue

        if (params.length < 3) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de retard");
        }

        String firstName = (String) params[0];
        String bookTitle = (String) params[1];
        String daysOverdue = (String) params[2];

        String subject = "🚨 RETARD : Livre en retard de retour";
        String content = String.format(
            "Bonjour %s,\n\n" +
            "⚠️ ATTENTION : Votre emprunt est EN RETARD\n\n" +
            "📚 Livre : \"%s\"\n" +
            "⏰ Retard : %s jour(s)\n\n" +
            "🚨 ACTIONS REQUISES :\n" +
            "1. Retournez le livre au plus vite\n" +
            "2. Des pénalités peuvent s'appliquer\n" +
            "3. Votre compte peut être suspendu en cas de retards répétés\n\n" +
            "📞 Pour toute question, contactez-nous.\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle,
            daysOverdue
        );

        emailService.sendEmail(to, subject, content);
    }
}

