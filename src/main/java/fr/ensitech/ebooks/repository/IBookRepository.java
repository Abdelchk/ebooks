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

	List<Book> findByTitle(String title) throws Exception;
	List<Book> findByIsPublished(boolean isPublished) throws Exception;
	List<Book> findByTitleAndIsPublished(String title, boolean isPublished) throws Exception;
	List<Book> findByTitleOrDescriptionContaining(String title, String description) throws Exception;
	List<Book> findByIdGreaterThan(Long id) throws Exception;
	List<Book> findByTitleContaining(String texte) throws Exception;
	List<Book> findByPublicationDate(Date date) throws Exception;
	List<Book> findByPublicationDateBetween(Date dateInf, Date dateSup) throws Exception;
	List<Book> findByTitleOrderByPublicationDateDesc(String title) throws Exception;
}
