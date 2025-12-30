package fr.ensitech.ebooks.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.ensitech.ebooks.entity.Book;

@Service
public interface IBookService {
	Book addOrUpdate(Book book) throws Exception;
    Book getBookById(Long id) throws Exception;
	List<Book> getBooks() throws Exception;
	List<Book> getBooksByTitleContaining(String texte) throws Exception;
	Book updateBook(Book book) throws Exception;
	void deleteBook(Long id) throws Exception;
}
