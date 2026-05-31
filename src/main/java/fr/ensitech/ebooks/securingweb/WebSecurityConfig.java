package fr.ensitech.ebooks.securingweb;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    // S1192 : constantes pour éviter la duplication des rôles
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_LIBRARIAN = "LIBRARIAN";

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // S6813 : injection par constructeur
    private final CustomUserDetailsService userDetailsService;

    public WebSecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 4096, 3);
    }

    @Bean
    // S1874 : DaoAuthenticationProvider et setUserDetailsService sont marqués deprecated dans Spring Security 6.4+
    // mais restent l'API standard recommandée pour l'authentification par BDD.
    // La suppression est intentionnelle - aucune alternative non-deprecated n'existe à ce stade.
    @SuppressWarnings({"deprecation", "java:S1874"})
    public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); // NOSONAR S1874
        provider.setUserDetailsService(userDetailsService); // NOSONAR S1874
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // setAllowedOriginPatterns supporte les wildcards et est compatible avec allowCredentials=true
        List<String> allowedOriginPatterns = new java.util.ArrayList<>(List.of(
            "http://localhost:3000",
            "https://*.vercel.app"   // Accepte tous les déploiements Vercel (prod + preview)
        ));
        // Ajouter un domaine custom uniquement s'il n'est pas déjà couvert
        // (localhost:3000 et *.vercel.app sont déjà dans la liste ci-dessus)
        if (frontendUrl != null && !frontendUrl.isBlank()
                && !frontendUrl.contains("localhost:3000")
                && !frontendUrl.contains("vercel.app")) {
            allowedOriginPatterns.add(frontendUrl);
        }
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("X-Backend-Version")); // Visible côté frontend
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Utilise un handler simple (sans XOR) adapté à HttpSessionCsrfTokenRepository.
        // XorCsrfTokenRequestAttributeHandler était conçu pour CookieCsrfTokenRepository afin
        // de prévenir l'attaque BREACH sur les cookies. Avec un repository de session côté
        // serveur, ce mécanisme XOR n'est plus nécessaire ni souhaitable.
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();

        // Protection CSRF via la session HTTP (côté serveur) :
        // Avantage cross-origin : le token n'est PAS stocké dans un cookie XSRF-TOKEN
        // (qui ne serait pas renvoyé par le navigateur en cross-origin sans SameSite=None).
        // Le token est lié à la session, identifiée par le cookie JSESSIONID
        // (déjà configuré SameSite=None;Secure en prod dans application-prod.properties).
        // Le frontend récupère le token via GET /api/auth/csrf (JSON) et l'envoie
        // dans l'en-tête X-XSRF-TOKEN sur toutes les requêtes d'écriture.
        HttpSessionCsrfTokenRepository csrfRepository = new HttpSessionCsrfTokenRepository();
        // Aligner le nom de l'en-tête avec la convention Axios (XSRF plutôt que CSRF)
        csrfRepository.setHeaderName("X-XSRF-TOKEN");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                    .csrfTokenRepository(csrfRepository)
                    .csrfTokenRequestHandler(requestHandler)
                )
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/api/auth/**", "/api/rest/books/all", "/api/rest/books/*",
                            "/api/rest/books/search", "/api/rest/books/category/**").permitAll()
                    .requestMatchers("/api/rest/images/**").hasAnyRole(ROLE_LIBRARIAN, ROLE_ADMIN)
                    .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                    .requestMatchers("/api/librarian/**").hasAnyRole(ROLE_LIBRARIAN, ROLE_ADMIN)
                    .requestMatchers("/api/rest/cart/**").authenticated()
                    .requestMatchers("/api/rest/reservations/**").authenticated()
                    .requestMatchers("/api/rest/loans/**").authenticated()
                    .requestMatchers("/api/rest/**").authenticated()
                    .anyRequest().authenticated()
                )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .permitAll()
                )
                // Remplace httpBasic par défaut qui envoie WWW-Authenticate: Basic
                // et déclenche la popup native du navigateur sur chaque 401.
                // Notre entry point retourne du JSON proprement sans popup.
                .httpBasic(basic -> basic.authenticationEntryPoint(apiAuthenticationEntryPoint()))
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(apiAuthenticationEntryPoint())
                );

        return http.build();
    }

    /**
     * Entry point personnalisé : retourne un JSON 401 sans header WWW-Authenticate.
     * Évite la popup native "Se connecter" du navigateur sur les requêtes API.
     */
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"authenticated\":false,\"message\":\"Non authentifié\"}");
        };
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request,
                                                HttpServletResponse response,
                                                AuthenticationException exception) throws IOException {
                if (exception instanceof DisabledException) {
                    getRedirectStrategy().sendRedirect(request, response, "/login?error=disabled");
                } else {
                    getRedirectStrategy().sendRedirect(request, response, "/login?error");
                }
            }
        };
    }

}
