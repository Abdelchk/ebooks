package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class AccountActivatedEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public AccountActivatedEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        emailService.sendEmail(to, "Compte activé",
                "Votre compte a été activé avec succès. Vous pouvez maintenant vous connecter.");
    }
}
