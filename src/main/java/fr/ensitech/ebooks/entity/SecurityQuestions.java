package fr.ensitech.ebooks.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "security_questions")
public class SecurityQuestions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String question;
}
