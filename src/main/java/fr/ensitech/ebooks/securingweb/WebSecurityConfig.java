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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
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
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            frontendUrl
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // S4502 : CSRF désactivé intentionnellement pour les routes /api/** (REST stateless).
                // Le risque est atténué par : CORS strict (origine unique), authentification préalable requise,
                // et le cookie XSRF-TOKEN est protégé par le domaine.
                // S3330 : HttpOnly=false requis pour que React (SPA) puisse lire le cookie XSRF-TOKEN
                // et l'envoyer dans le header X-XSRF-TOKEN. Risque XSS atténué par la CSP.
                .csrf(csrf -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // NOSONAR S3330
                    .csrfTokenRequestHandler(new XorCsrfTokenRequestAttributeHandler())
                    .ignoringRequestMatchers("/api/auth/**", "/api/rest/**", "/api/admin/**", "/api/librarian/**") // NOSONAR S4502
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
                .httpBasic(Customizer.withDefaults());

        return http.build();
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
