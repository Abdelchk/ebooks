package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.service.IBookService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/rest/books")
public class BookRestController implements IBookController {

    private static final Logger logger = LoggerFactory.getLogger(BookRestController.class);

    private final IBookService bookService;

    public BookRestController(IBookService bookService) {
        this.bookService = bookService;
    }

    @Override
    @GetMapping("/infos")
    public String getInfos() {
        return "Bonjour de la part d'Ensitech.";
    }

    @Override
    @PostMapping("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        if (book == null
                || book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Book _book = bookService.addOrUpdate(book);
            return new ResponseEntity<>(_book, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du livre", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> updateBook(Book book) {
        if (book == null
                || book.getId() == null || book.getId() <= 0
                || book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.addOrUpdate(book);
            return new ResponseEntity<>("Livre mis à jour avec succès", HttpStatus.ACCEPTED);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du livre", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DeleteMapping("remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<String> deleteBook(@PathVariable("id") Long id) {
        if (id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>("Le Livre (id = " + id + ") a été supprimé avec succès", HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression du livre id={}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("{id}")
    public ResponseEntity<Book> getBookById(@PathVariable("id") Long id) {
        if (id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Book book = bookService.getBookById(id);
            return new ResponseEntity<>(book, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du livre id={}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getBooks();
            logger.debug("Nombre de livres récupérés : {}", books.size());
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des livres", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<List<Book>> searchBooks(@RequestParam("q") String query) {
        try {
            List<Book> books = bookService.searchBooks(query);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des livres", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/category/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<List<Book>> getBooksByCategory(@PathVariable("category") String category) {
        try {
            List<Book> books = bookService.getBooksByCategory(category);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des livres par catégorie", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/category/{category}/search")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<List<Book>> searchBooksByCategory(
            @PathVariable("category") String category,
            @RequestParam("q") String query) {
        try {
            List<Book> books = bookService.searchBooksByCategory(category, query);
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par catégorie", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<Book>> findBooksByTitleContaining(String text) {
        return null;
    }

    @Override
    public ResponseEntity<List<Book>> findBooksByTitleOrDescriptionContaining(String text) {
        return null;
    }

    @Override
    public ResponseEntity<List<Book>> findBooksByPublicationDate(Date beginDate, Date endDate) {
        return null;
    }

    @Override
    public ResponseEntity<List<Book>> getBooksByAuthor(String text) {
        return null;
    }

    @Override
    public ResponseEntity<Book> getBookByIsbn(String isbn) {
        return null;
    }

    @Override
    public ResponseEntity<List<Book>> getPublishedBooks(boolean isPublished) {
        return null;
    }

    @Override
    public ResponseEntity<List<Book>> getBooksByAuthorName(String text) {
        return null;
    }
}
