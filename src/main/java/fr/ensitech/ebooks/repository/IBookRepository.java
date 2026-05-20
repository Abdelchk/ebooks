package fr.ensitech.ebooks.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.ensitech.ebooks.entity.Book;

/*
 *  Sans ajouter aucune méthode supplémentaire, JpaRepository fournit déjà des méthodes pour effectuer des opérations CRUD (Create, Read, Update, Delete) sur l'entité Book :
 *  save(), findOne(), findAll(), count(), delete(), ...
 */
@Repository
public interface IBookRepository extends JpaRepository<Book, Long> {

	List<Book> findByTitle(String title);
	List<Book> findByIsPublished(boolean isPublished);
	List<Book> findByTitleAndIsPublished(String title, boolean isPublished);
	List<Book> findByTitleOrDescriptionContaining(String title, String description);
	List<Book> findByIdGreaterThan(Long id);
	List<Book> findByTitleContaining(String texte);
	List<Book> findByPublicationDate(Date date);
	List<Book> findByPublicationDateBetween(Date dateInf, Date dateSup);
	List<Book> findByTitleOrderByPublicationDateDesc(String title);
	List<Book> findByAuthorContainingIgnoreCase(String author);
	List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
	List<Book> findByCategory(String category);
	List<Book> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndAuthorContainingIgnoreCase(
			String category1, String title, String category2, String author);
}
