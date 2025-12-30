package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.service.IBookService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin("http://localhost:8080")
@RequestMapping("/api/rest/books")
public class BookRestController implements IBookController{
    @Autowired
    private IBookService bookService;

    // URI => http://localhost:8080/api/rest/books/infos
    @Override
    @GetMapping("/infos")
    public String getInfos() {
        return "Bonjour de la part d'Ensitech.";
    }

    // URI => http://localhost:8080/api/rest/books/create
    @Override
    @PostMapping("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        System.out.println("create invoqued");
        if (book == null
                || book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()
                //...etc
                ) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Book _book = bookService.addOrUpdate(book);
            return new ResponseEntity<>(_book, HttpStatus.CREATED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<String> updateBook(Book book) {
        if (book == null
                || book.getId() == null || book.getId() <= 0
                || book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()
                //...etc
                ) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.addOrUpdate(book);
            return new ResponseEntity<>("Livre mis à jour avec succès", HttpStatus.ACCEPTED);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DeleteMapping("remove/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<String> deleteBook(@PathVariable("id") Long id) {
        if (id <=0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            bookService.deleteBook(id);
            return new ResponseEntity<>("Le Livre (id = " + id + ") a été supprimé avec succès", HttpStatus.OK);

        } catch (Exception e) {
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
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GetMapping("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseEntity<List<Book>> getAllBooks() {
        try {
            List<Book> books = bookService.getBooks();
            return new ResponseEntity<>(books, HttpStatus.OK);
        } catch (Exception e) {
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
    public ResponseEntity<Book> getBookByIsbn(String isbn) throws Exception {
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
