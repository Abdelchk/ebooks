package fr.ensitech.ebooks.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "verification_codes")
public class VerificationCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(nullable = false, length = 6)
    @Pattern(regexp = "^[0-9]{6}$", message = "Le code doit contenir exactement 6 chiffres")
    private String code;

    @Column(nullable = false)
    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean used = false;
}
