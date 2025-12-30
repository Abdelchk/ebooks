package fr.ensitech.ebooks.main;

import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.service.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootApplication
@ComponentScan(basePackages = "fr.ensitech.ebooks")
public class MainClassTest {

//    public static void main(String[] args) {
//        SpringApplication.run(MainClassTest.class, args);
//    }
//
//    @Bean
//    public CommandLineRunner testService(IUserService userService) {
//        return args -> {
//            System.out.println("=== Test du UserService ===");
//
//            // Récupérer l'utilisateur existant
//            User currentUser = userService.findByEmail("check.abdel@gmail.com")
//                    .orElseThrow(() -> new UsernameNotFoundException("Email non trouvé"));
//
//            // Modifier des champs (y compris l'email)
//            currentUser.setEmail("jean.dupont@gmail.com");
//
//            // Cet appel fera une MISE À JOUR (car currentUser.getId() != null)
//            User updatedUser = userService.addOrUpdateUser(currentUser);
//            System.out.println("Utilisateur mis à jour : " + updatedUser.getEmail());
//        };
//    }

}
