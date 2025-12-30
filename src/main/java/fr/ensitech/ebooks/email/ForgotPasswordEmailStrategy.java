package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class ForgotPasswordEmailStrategy implements EmailStrategy{
    private final EmailService emailService;

    public ForgotPasswordEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String resetLink = (String) params[0];
        emailService.sendEmail(to, "Réinitialisation de votre mot de passe",
                "Cliquez sur ce lien pour réinitialiser votre mot de passe : " + resetLink +
                        "\n\nCe lien expire dans 24 heures.");
    }
}
