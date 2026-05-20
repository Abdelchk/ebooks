package fr.ensitech.ebooks.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.Book;

@Service
public interface IBookService {
	Book addOrUpdate(Book book);
    Book getBookById(Long id);
	List<Book> getBooks();
	List<Book> getBooksByTitleContaining(String texte);
	List<Book> searchBooks(String query);
	List<Book> getBooksByCategory(String category);
	List<Book> searchBooksByCategory(String category, String query);
	Book updateBook(Book book);
	void deleteBook(Long id);

	// Méthodes pour le bibliothécaire
	Book save(Book book);
	void deleteById(Long id);
	List<Book> findLowStockBooks();
}
