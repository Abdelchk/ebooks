package fr.ensitech.ebooks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.utils.Dates;

@SpringBootTest
class EbooksApplicationTests {
	
	@Autowired
	private IBookRepository bookRepository;
	
	private Book book1, book2, book3;

	@Test
	void contextLoads() {
	}
	
	@BeforeEach
	void setup() {
		book1 = Book.builder().title("Java").description("Spring Boot est un framework Java")
				.isPublished(true).publicationDate(Dates.convertStringToDate("23/01/2025")).build();
		
		bookRepository.save(book1);
		
		book2 = Book.builder().title("Java").description("Initiation à Java")
				.isPublished(true).publicationDate(Dates.convertStringToDate("15/05/2022")).build();
		
		bookRepository.save(book2);
		
		book3 = Book.builder().title("Java").description("Exercices avancés en Java")
				.isPublished(true).publicationDate(Dates.convertStringToDate("02/02/2002")).build();
		
		bookRepository.save(book3);
		
	}
	
	@AfterEach
	void tearDown() {
		bookRepository.deleteAll();
	}
	
//	@Test
//	void testFindAllBooks() {
//		List<Book> _books = bookRepository.findAll();
//		assertNotNull(_books);
//		assertFalse(_books.isEmpty());
//		assertThat(_books.size()).isEqualTo(3);
//	}
	
	@Test
	void testFindBookByTitle() throws Exception {
		List<Book> _books = bookRepository.findByTitleOrderByPublicationDateDesc("Java");
		assertNotNull(_books);
		assertFalse(_books.isEmpty());
		assertThat(_books.size()).isEqualTo(3);
	}

//	@Test
//	@DisplayName("Test de la méthode de création d'un book")
//	void testSaveBook() {
//		Book _book = bookRepository.save(book1);
//		assertNotNull(_book);
//		assertNotNull(_book.getId());
//		assertThat(_book.getId()).isGreaterThan(0);
//		assertThat(_book.getTitle()).isEqualTo(book1.getTitle());
//		assertThat(_book.getDescription()).isEqualTo(book1.getDescription());
//		assertThat(_book.getPublicationDate()).isEqualTo(book1.getPublicationDate());
//		assertThat(_book.getIsPublished()).isEqualTo(book1.getIsPublished());
//	}
}
