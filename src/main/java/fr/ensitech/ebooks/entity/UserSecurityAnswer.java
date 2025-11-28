package fr.ensitech.ebooks.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "user_security_answers")
public class UserSecurityAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private SecurityQuestions securityQuestion;

    @Column(nullable = false, length = 100)
    private String hashedAnswer;
}
