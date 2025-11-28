package fr.ensitech.ebooks.securingweb;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Intercepteur pour vérifier l'expiration du mot de passe
 */
@Component
public class PasswordExpirationInterceptor implements HandlerInterceptor {

    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Ignorer si l'utilisateur n'est pas authentifié
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        // Ignorer pour certaines URL spécifiques
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/update-password") ||
            requestURI.startsWith("/logout") ||
            requestURI.startsWith("/css") ||
            requestURI.startsWith("/js") ||
            requestURI.startsWith("/verify-code") ||
            requestURI.startsWith("/resend-code")) {
            return true;
        }

        // Récupérer l'utilisateur connecté
        String email = auth.getName();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LocalDate lastPasswordUpdate = user.getLastPasswordUpdateDate();

            // Si l'utilisateur n'a jamais changé son mot de passe, on considère la date d'aujourd'hui
            if (lastPasswordUpdate == null) {
                return true; // Première connexion, pas d'expiration
            }

            // Vérifier si le mot de passe a expiré (12 semaines = 84 jours)
            long daysSinceLastUpdate = ChronoUnit.DAYS.between(lastPasswordUpdate, LocalDate.now());

            if (daysSinceLastUpdate >= 84) {
                // Mot de passe expiré, rediriger vers la page de changement
                request.getSession().setAttribute("passwordExpired", true);
                request.getSession().setAttribute("errorMessage",
                    "Votre mot de passe a expiré. Vous devez le changer pour continuer.");
                response.sendRedirect(request.getContextPath() + "/update-password");
                return false;
            }

            // Avertir si le mot de passe expire bientôt (dans les 7 jours)
            if (daysSinceLastUpdate >= 77 && daysSinceLastUpdate < 84) {
                long daysRemaining = 84 - daysSinceLastUpdate;
                request.getSession().setAttribute("passwordWarning",
                    "Attention : Votre mot de passe expire dans " + daysRemaining + " jour(s).");
            }
        }

        return true;
    }
}

