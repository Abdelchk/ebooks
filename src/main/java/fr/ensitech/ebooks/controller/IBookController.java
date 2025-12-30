package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.Book;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

public interface IBookController {

    String getInfos();
    ResponseEntity<Book> createBook(Book book);
    ResponseEntity<String> updateBook(Book book);
    ResponseEntity<String> deleteBook(Long id);
    ResponseEntity<Book> getBookById(Long id);
    ResponseEntity<List<Book>> getAllBooks();
    ResponseEntity<List<Book>> findBooksByTitleContaining(String text);
    ResponseEntity<List<Book>> findBooksByTitleOrDescriptionContaining(String text);
    ResponseEntity<List<Book>> findBooksByPublicationDate(Date beginDate, Date endDate);
    ResponseEntity<List<Book>> getBooksByAuthor(String text);
    ResponseEntity<Book> getBookByIsbn(String isbn) throws Exception;
    ResponseEntity<List<Book>> getPublishedBooks(boolean isPublished);
    ResponseEntity<List<Book>> getBooksByAuthorName(String text);
}
