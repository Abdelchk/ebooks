package fr.ensitech.ebooks.repository;

import fr.ensitech.ebooks.entity.SecurityQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ISecurityQuestionsRepository extends JpaRepository<SecurityQuestions, Long> {
}
