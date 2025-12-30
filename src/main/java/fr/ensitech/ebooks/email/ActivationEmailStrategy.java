package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class ActivationEmailStrategy implements EmailStrategy {
    private final EmailService emailService;

    public ActivationEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String activationLink = (String) params[0];
        emailService.sendEmail(to, "Activation de votre compte",
                "Cliquez sur ce lien pour activer votre compte : " + activationLink);
    }
}
