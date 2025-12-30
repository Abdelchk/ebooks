package fr.ensitech.ebooks.securingweb;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.repository.IUserRepository;
import fr.ensitech.ebooks.service.IUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class TwoFactorAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private IUserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        User user = userService.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));

        // Vérifier si un code a déjà été envoyé dans les 24h
        LocalDateTime now = LocalDateTime.now();
        if (user.getLastVerificationCodeSentAt() != null &&
                user.getLastVerificationCodeSentAt().isAfter(now.minusHours(24))) {
            // Rediriger directement vers la page d'accueil
            response.sendRedirect("/accueil");
            return;
        }

        // Générer et envoyer le code 2FA
        userService.generateVerificationCode(user);

        // Rediriger vers la page de vérification du code
        response.sendRedirect("/verify-code");
    }

}
