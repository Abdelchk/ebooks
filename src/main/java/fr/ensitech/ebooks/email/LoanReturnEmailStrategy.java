package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class LoanReturnEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    
    public LoanReturnEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle
        // params[2] = wasLate (true/false)

        if (params.length < 3) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de retour de livre");
        }
        
        String firstName = (String) params[0];
        String bookTitle = (String) params[1];
        boolean wasLate = (Boolean) params[2];

        String subject = "✓ Retour de livre confirmé";
        String content = String.format(
            "Bonjour %s,\n\n" +
            "Le retour du livre \"%s\" a été enregistré avec succès.\n\n" +
            "%s\n\n" +
            "Merci d'avoir utilisé notre service !\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle,
            wasLate ? 
                "⚠️ Note : Ce livre a été retourné en retard. Merci de respecter les dates de retour à l'avenir." :
                "✓ Merci d'avoir respecté la date de retour !"
        );
        
        emailService.sendEmail(to, subject, content);
    }
}

