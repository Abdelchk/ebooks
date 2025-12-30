package fr.ensitech.ebooks.email;

public class AccountDeactivatedEmailStrategy implements EmailStrategy {
    private final fr.ensitech.ebooks.service.EmailService emailService;

    public AccountDeactivatedEmailStrategy(fr.ensitech.ebooks.service.EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        emailService.sendEmail(to, "Suppression de votre compte",
                "Votre compte a été supprimé avec succès. Nous sommes désolés de vous voir partir.");
    }
}
