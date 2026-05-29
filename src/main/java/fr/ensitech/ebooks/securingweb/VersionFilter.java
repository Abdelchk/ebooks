package fr.ensitech.ebooks.securingweb;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Filtre HTTP qui ajoute automatiquement les headers de version à chaque réponse.
 * Visible dans l'onglet Réseau du navigateur → En-têtes de réponse.
 *
 * X-Backend-Version: 1.2.3
 */
@Component
@Order(1)
public class VersionFilter implements Filter {

    @Value("${app.version:unknown}")
    private String appVersion;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader("X-Backend-Version", appVersion);
        }

        chain.doFilter(request, response);
    }
}

