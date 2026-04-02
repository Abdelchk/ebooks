package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class LoanExtensionEmailStrategy implements EmailStrategy {
    
    private final EmailService emailService;
    
    public LoanExtensionEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }
    
    @Override
    public void sendEmail(String to, Object... params) {
        // params[0] = userFirstName
        // params[1] = bookTitle
        // params[2] = newDueDate (formatted)
        // params[3] = extensionCount

        if (params.length < 4) {
            throw new IllegalArgumentException("Paramètres manquants pour l'email de prolongation d'emprunt");
        }
        
        String firstName = (String) params[0];
        String bookTitle = (String) params[1];
        String newDueDate = (String) params[2];
        String extensionCount = (String) params[3];

        String subject = "✓ Prolongation d'emprunt confirmée";
        String content = String.format(
            "Bonjour %s,\n\n" +
            "Votre emprunt du livre \"%s\" a été prolongé de 7 jours.\n\n" +
            "📅 Nouvelle date de retour : %s\n" +
            "📊 Nombre de prolongations effectuées : %s/2\n\n" +
            "%s\n\n" +
            "Cordialement,\n" +
            "L'équipe Ebooks",
            firstName,
            bookTitle,
            newDueDate,
            extensionCount,
            extensionCount.equals("2") ? 
                "⚠️ Attention : Vous avez atteint le nombre maximum de prolongations." :
                "💡 Vous pouvez encore prolonger une fois si nécessaire."
        );
        
        emailService.sendEmail(to, subject, content);
    }
}

