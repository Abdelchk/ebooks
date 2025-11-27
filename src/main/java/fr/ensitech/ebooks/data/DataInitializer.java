package fr.ensitech.ebooks.data;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import fr.ensitech.ebooks.enums.SecurityQuestionEnum;
import fr.ensitech.ebooks.repository.ISecurityQuestionsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(ISecurityQuestionsRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<SecurityQuestions> questions = Arrays.stream(SecurityQuestionEnum.values())
                        .map(this::createQuestion)
                        .collect(Collectors.toList());
                repository.saveAll(questions);
            }
        };
    }

    private SecurityQuestions createQuestion(SecurityQuestionEnum questionEnum) {
        SecurityQuestions sq = new SecurityQuestions();
        sq.setQuestion(questionEnum.getQuestion());
        return sq;
    }
}
