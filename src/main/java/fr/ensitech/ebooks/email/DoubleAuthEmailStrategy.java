package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class DoubleAuthEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public DoubleAuthEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String authCode = (String) params[0];
        emailService.sendEmail(to, "Code d'authentification à deux facteurs",
                "Votre code d'authentification à deux facteurs est : " + authCode + ". Il expire dans 2 minutes.");
    }
}
