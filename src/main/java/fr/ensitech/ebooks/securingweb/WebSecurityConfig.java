package fr.ensitech.ebooks.securingweb;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private TwoFactorAuthenticationSuccessHandler twoFactorAuthSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(16, 32, 1, 4096, 3); // requis pour le hash
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Désactivation du CSRF (nécessaire pour les APIs REST)
                .csrf(csrf -> csrf.disable())

                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                    // Autoriser les endpoints publics de l'API REST
                    .requestMatchers("/", "/api/auth/**", "/api/rest/books/all", "/api/rest/books/*", "/api/rest/books/search", "/api/rest/books/category/**").permitAll()
                    // Endpoints protégés nécessitant authentification
                    .requestMatchers("/api/rest/cart/**").authenticated()
                    .requestMatchers("/api/rest/reservations/**").authenticated()
                    .requestMatchers("/api/rest/loans/**").authenticated()
                    // Endpoints nécessitant l'authentification
                    .requestMatchers("/api/rest/**").authenticated()
                    // Tout le reste nécessite une authentification
                    .anyRequest().authenticated()
                )
                // Désactiver le formLogin car nous gérons l'authentification via API REST
                // .formLogin(form -> form
                //     .loginPage("/login")
                //     .successHandler(twoFactorAuthSuccessHandler)
                //     .failureHandler(customAuthenticationFailureHandler())
                //     .permitAll()
                // )
                .logout(logout -> logout
                    .logoutUrl("/logout")
                    .permitAll()
                    )
                // Authentification Basic (Postman friendly)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request,
                                                HttpServletResponse response,
                                                AuthenticationException exception)
                    throws IOException, ServletException {

                if (exception instanceof DisabledException) {
                    getRedirectStrategy().sendRedirect(request, response, "/login?error=disabled");
                } else {
                    getRedirectStrategy().sendRedirect(request, response, "/login?error");
                }
            }
        };
    }
}
