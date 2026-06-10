package fr.ensitech.ebooks.entity;

import jakarta.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.validator.constraints.Length;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @ToString @AllArgsConstructor
@Builder
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 60, nullable = false)
	@NotEmpty(message = "Le prénom est obligatoire !")
	@Length(min = 2, message = "Le prénom doit être constitué de 2 à 48 caractères !")
	@Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le prénom ne doit contenir que des lettres !")
	private String firstname;

	@Column(length = 60, nullable = false)
	@NotEmpty(message = "Le nom est obligatoire !")
	@Length(min = 2, message = "Le nom doit être constitué de 2 à 48 caractères !")
	@Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s-]+$", message = "Le nom ne doit contenir que des lettres !")
	private String lastname;

	@Column(length = 60, nullable = false, unique = true)
	@NotEmpty(message = "L'email est obligatoire !")
	@Length(min = 10, message = "L'email doit être constitué de 2 à 48 caractères !")
	@Pattern(regexp = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+\\.[A-Z|a-z]{2,}\\b", message = "L'email doit être valide !")
	private String email;

	@Column(nullable = false)
	@NotEmpty(message = "Le mot de passe est obligatoire !")
	// Note: La validation du format du mot de passe est faite dans le service AVANT l'encodage.
	// Le champ stocke un hash Argon2 qui ne respecterait pas un regex de mot de passe en clair.
	private String password;

	@Column(nullable = false)
	@NotNull(message = "La date de naissance est obligatoire !")
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate birthdate;

	@Column(length = 15, nullable = false)
	@NotEmpty(message = "Le numéro de téléphone est obligatoire !")
	@Length(min = 10, message = "Le numéro de téléphone doit être constitué de 10 à 15 caractères !")
	@Pattern(regexp = "^\\+?\\d{10,15}$", message = "Le numéro de téléphone doit être valide !")
	private String phoneNumber;
	
	@Builder.Default
	@Column(length = 10, columnDefinition = "varchar(10) default 'client'")
	private String role = "client"; // valeur Java par défaut pour new User() ET User.builder().build()

    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 100, nullable = false)
    private String verificationToken;

    // Historique des 5 derniers mots de passe (séparés par des espaces)
    @Builder.Default
    @Column(length = 1500, columnDefinition = "varchar(1500) default ''")
    private String passwordHistory = "";

    // Date de dernière mise à jour du mot de passe
    @Column
    private LocalDate lastPasswordUpdateDate;

    // Token pour la réinitialisation du mot de passe
    @Column(length = 100)
    private String resetPasswordToken;

    // Date d'expiration du token de réinitialisation
    @Column
    private LocalDate resetTokenExpiryDate;

    @Column
    private LocalDateTime lastVerificationCodeSentAt;

    // Champs transitoires pour la validation du CAPTCHA (ne sont pas stockés en base)
    @Transient
    private String captchaId;

    @Transient

    private String captchaInput;

}
