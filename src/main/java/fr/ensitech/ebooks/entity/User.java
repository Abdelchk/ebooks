package fr.ensitech.ebooks.entity;

import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "user")
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
	@Length(min = 12, message = "Le mot de passe doit être constitué de 8 à 48 caractères !")
	@Pattern(regexp = "^(?=.*[A-Za-zÀ-ÖØ-öø-ÿ])(?=.*\\d)(?=.*[@$!%*?&amp;#])[A-Za-zÀ-ÖØ-öø-ÿ\\d@$!%*?&amp;#]{12,}$", 
             message = "Veuillez saisir un mot de passe valide. 12 caractères minimum, au moins une lettre majuscule, une lettre minuscule, un chiffre et un caractère spécial.")
	private String password;

	@Column(nullable = false)
	@NotNull(message = "La date de naissance est obligatoire !")
	private LocalDate birthdate;

	@Column(length = 15, nullable = false)
	@NotEmpty(message = "Le numéro de téléphone est obligatoire !")
	@Length(min = 10, message = "Le numéro de téléphone doit être constitué de 10 à 15 caractères !")
	@Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Le numéro de téléphone doit être valide !")
	private String phoneNumber;
	
	@Column(length = 10, columnDefinition = "varchar(10) default 'client'")
	private String role;
    
    @Column(nullable = false)
    private boolean enabled;

    @Column(length = 100, nullable = false)
    private String verificationToken;

    // Historique des 5 derniers mots de passe (séparés par des espaces)
    @Column(length = 1500, columnDefinition = "varchar(1500) default ''")
    private String passwordHistory;

    // Date de dernière mise à jour du mot de passe
    @Column
    private LocalDate lastPasswordUpdateDate;

    // Token pour la réinitialisation du mot de passe
    @Column(length = 100)
    private String resetPasswordToken;

    // Date d'expiration du token de réinitialisation
    @Column
    private LocalDate resetTokenExpiryDate;

}
