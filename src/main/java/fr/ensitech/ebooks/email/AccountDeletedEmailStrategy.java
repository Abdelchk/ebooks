package fr.ensitech.ebooks.email;

import fr.ensitech.ebooks.service.EmailService;

public class AccountDeletedEmailStrategy implements EmailStrategy {

    private final EmailService emailService;

    public AccountDeletedEmailStrategy(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void sendEmail(String to, Object... params) {
        String firstname = params.length > 0 ? (String) params[0] : "Utilisateur";

        String subject = "Confirmation de suppression de compte - Ebooks";

        String body = String.format(
            """
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                    <h2 style="color: #dc3545; border-bottom: 2px solid #dc3545; padding-bottom: 10px;">
                        🗑️ Compte Supprimé
                    </h2>
                    
                    <p>Bonjour %s,</p>
                    
                    <p>Nous vous confirmons que votre compte Ebooks a été <strong>définitivement supprimé</strong>.</p>
                    
                    <div style="background-color: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #721c24;">⚠️ Données supprimées :</h3>
                        <ul style="margin: 5px 0;">
                            <li>Informations personnelles</li>
                            <li>Historique des emprunts et réservations</li>
                            <li>Questions et réponses de sécurité</li>
                            <li>Codes de vérification</li>
                        </ul>
                    </div>
                    
                    <div style="background-color: #d1ecf1; border-left: 4px solid #0c5460; padding: 15px; margin: 20px 0;">
                        <h4 style="margin-top: 0; color: #0c5460;">📋 Informations importantes :</h4>
                        <p style="margin: 5px 0;">• Cette action est <strong>irréversible</strong></p>
                        <p style="margin: 5px 0;">• Vous ne pouvez plus accéder à votre compte</p>
                        <p style="margin: 5px 0;">• Vos données ont été définitivement effacées de nos serveurs</p>
                        <p style="margin: 5px 0;">• Vous pouvez créer un nouveau compte à tout moment</p>
                    </div>
                    
                    <div style="background-color: #fff3cd; border-left: 4px solid #856404; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0;"><strong>⚠️ Si vous n'avez pas demandé cette suppression :</strong></p>
                        <p style="margin: 5px 0;">Contactez-nous immédiatement à <a href="mailto:support@ebooks.fr">support@ebooks.fr</a></p>
                    </div>
                    
                    <p>Nous sommes désolés de vous voir partir. Si vous changez d'avis, n'hésitez pas à créer un nouveau compte.</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;">
                        <p style="margin: 5px 0; color: #666;">Merci d'avoir utilisé Ebooks</p>
                        <p style="margin: 5px 0; color: #999; font-size: 12px;">
                            Conformément au RGPD, toutes vos données personnelles ont été supprimées.
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
            firstname
        );

        emailService.sendEmail(to, subject, body);
    }
}

