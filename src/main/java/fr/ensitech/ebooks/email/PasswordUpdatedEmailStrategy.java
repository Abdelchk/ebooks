package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class PasswordUpdatedEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public PasswordUpdatedEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String resetLink = (String) params[0];
        emailService.sendEmail(to, "Mot de passe mis à jour",
                "Votre mot de passe a été mis à jour avec succès.");
    }
}
