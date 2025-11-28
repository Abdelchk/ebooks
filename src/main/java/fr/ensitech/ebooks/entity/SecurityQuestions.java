package fr.ensitech.ebooks.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "security_questions")
public class SecurityQuestions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "La question ne peut pas être vide")
    @Size(max = 255, message = "La question ne doit pas dépasser 100 caractères")
    private String question;
}

