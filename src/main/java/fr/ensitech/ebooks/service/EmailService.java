package fr.ensitech.ebooks.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender mailSender;

    /**
     * Envoie un email de façon ASYNCHRONE (thread séparé du pool Spring).
     *
     * Pourquoi async :
     * L'envoi SMTP peut bloquer plusieurs minutes si le serveur est lent ou injoignable
     * (même avec connectiontimeout=5000, le STARTTLS/SSL peut ignorer ce timeout).
     * En rendant l'envoi async, le thread HTTP qui appelle cette méthode (ex : login,
     * register) est libéré immédiatement → la réponse au client ne dépend plus du SMTP.
     *
     * La méthode gère ses propres exceptions en interne : elle ne peut pas les propager
     * vers l'appelant (thread différent), mais elle les logue correctement.
     */
    @Async
    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML activé
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email à {} : {}", to, e.getMessage());
        }
    }
}
