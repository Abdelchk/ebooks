package fr.ensitech.ebooks.init;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.utils.Dates;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


@Configuration
@Profile("!test")  // Ne pas exécuter en mode test (H2 CI)
public class DataLoader {

    @Bean
    public CommandLineRunner loadBooks(IBookRepository bookRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.save(Book.builder()
                        .title("Spring en action")
                        .description("Un livre complet pour apprendre Spring Boot.")
                        .isPublished(true)
                        .publicationDate(Dates.convertStringToDate("23/01/2025"))
                        .author("Craig Walls")
                        .category("Informatique")
                        .coverImageUrl("https://example.com/spring.jpg")
                        .quantity(10)
                        .build());

                bookRepository.save(Book.builder()
                        .title("Thymeleaf Basics")
                        .description("Introduction à Thymeleaf avec exemples.")
                        .isPublished(true)
                        .publicationDate(Dates.convertStringToDate("15/05/2022"))
                        .author("John Smith")
                        .category("Informatique")
                        .coverImageUrl("https://example.com/thymeleaf.jpg")
                        .quantity(5)
                        .build());

                System.out.println("📚 Livres de test insérés avec succès !");
            }
        };
    }
}
