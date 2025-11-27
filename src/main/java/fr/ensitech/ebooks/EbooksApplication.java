package fr.ensitech.ebooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.service.BookService;
import fr.ensitech.ebooks.service.IBookService;

@SpringBootApplication
public class EbooksApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(EbooksApplication.class, args);
		
	}
}
