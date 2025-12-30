package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class AccountUpdatedEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public AccountUpdatedEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        emailService.sendEmail(to, "Mise à jour du profil utilisateur",
                "Votre profil utilisateur a été mis à jour avec succès.");
    }
}
