package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.service.IBookService;
import fr.ensitech.ebooks.entity.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BookController {

    @Autowired
    private IBookService bookService;

    @GetMapping("/accueil")
    public String listBooks(Model model) {
        try {
            List<Book> books = bookService.getBooks();
            model.addAttribute("books", books);
			for (Book book : books) {
				System.out.println(book.getTitle() + " - " + book.getDescription());
			}
        } catch (Exception e) {
        	e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des livres.");
        }
        return "accueil"; // nom du fichier .html dans /templates
    }
}
