package fr.ensitech.ebooks.entity;

import javax.validation.constraints.NotEmpty;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "book")
// @Data generate all getters and setters and constructor
@Getter @Setter @NoArgsConstructor @ToString @AllArgsConstructor
@Builder
public class Book {
	
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 48)
	@NotEmpty(message = "Le titre ne peut pas être vide")
	@Length(min= 2, max = 48, message = "Le titre ne peut pas dépasser 48 caractères")
	private String title;
	
	@Column(length = 255, nullable = false)
	@NotEmpty(message = "La description ne peut pas être vide")
	private String description;
	
	@Column(nullable = false)
	@NotNull(message = "Ce champ est obligatoire !")
	private Boolean isPublished;
	
	@Column(nullable = false)
	@NotNull(message = "La date de publication est obligatoire !")
	private Date publicationDate;
	
	@Column(length = 60, nullable = false)
	@NotEmpty(message = "L'auteur est obligatoire !")
	private String author;
	
	@Column(length = 200, nullable = false)
	private String coverImageUrl;
	
	@Column(nullable = false)
	@NotNull(message = "La quantité est obligatoire !")
	private int quantity;

    @Column(length = 30, nullable = false)
    @NotEmpty(message = "La catégorie est obligatoire !")
    private String category;
	
}
