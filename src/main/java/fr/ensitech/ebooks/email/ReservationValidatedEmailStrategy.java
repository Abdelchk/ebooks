package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class ReservationValidatedEmailStrategy implements EmailStrategy {

    private final EmailService emailService;

    public ReservationValidatedEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String firstname = (String) params[0];
        String bookTitle = (String) params[1];

        String subject = "Réservation validée - " + bookTitle;

        String body = String.format(
            """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                    <h2 style="color: #28a745; border-bottom: 2px solid #28a745; padding-bottom: 10px;">
                        ✅ Réservation Validée
                    </h2>
                    
                    <p>Bonjour %s,</p>
                    
                    <p>Bonne nouvelle ! Votre réservation a été <strong>validée par notre bibliothécaire</strong>.</p>
                    
                    <div style="background-color: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #155724;">Détails de la réservation :</h3>
                        <p style="margin: 5px 0;"><strong>📚 Livre :</strong> %s</p>
                        <p style="margin: 5px 0;"><strong>⏱️ Délai de retrait :</strong> 72 heures</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #856404;">⚠️ Important :</h4>
                        <p style="margin: 5px 0;">Vous avez <strong>72 heures</strong> pour retirer votre livre à la bibliothèque.</p>
                        <p style="margin: 5px 0;">Passé ce délai, la réservation sera automatiquement annulée.</p>
                    </div>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd; text-align: center;">
                        <p style="margin: 5px 0; color: #666; font-size: 14px;">Merci d'utiliser notre service Ebooks</p>
                        <p style="margin: 5px 0; color: #999; font-size: 12px;">Cet email est envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            firstname,
            bookTitle
        );

        emailService.sendEmail(to, subject, body);
    }
}

