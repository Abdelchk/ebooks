package fr.ensitech.ebooks.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;

@Service
@RequiredArgsConstructor
public class BookService implements IBookService {

	private static final Logger logger = LoggerFactory.getLogger(BookService.class);

	private final IBookRepository bookRepository;
	private final IStockAlertService stockAlertService;

	@Override
	public Book addOrUpdate(Book book) {

		if (book == null) {
			throw new NullPointerException("Book ne doit pas être null");
		}
		if (book.getTitle() == null || book.getTitle().isBlank()
				|| book.getDescription() == null || book.getDescription().isBlank()
				|| book.getIsPublished() == null
                || book.getAuthor() == null || book.getAuthor().isBlank()
                || book.getPublicationDate() == null
                || book.getCategory() == null || book.getCategory().isBlank()) {
			throw new IllegalArgumentException("Tous les paramètres de book doivent être renseignés !");
			
		}
		return bookRepository.save(book);
	}

    @Override
    @Transactional(readOnly = true)
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Livre non trouvé avec l'ID : " + id));
    }

    @Override
    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    @Override
    public List<Book> getBooksByTitleContaining(String texte) {
        return bookRepository.findByTitleContaining(texte);
    }

    @Override
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    @Override
    public List<Book> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category);
    }

    @Override
    public List<Book> searchBooksByCategory(String category, String query) {
        return bookRepository.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndAuthorContainingIgnoreCase(
                category, query, category, query);
    }

    @Override
    public Book updateBook(Book book) {
        if (book == null) {
            throw new NullPointerException("Book ne doit pas être null");
        }
        if (book.getTitle() == null || book.getTitle().isBlank()
                || book.getDescription() == null || book.getDescription().isBlank()
                || book.getIsPublished() == null) {
            throw new IllegalArgumentException("Tous les paramètres de book doivent être renseignés !");
        }

        // Check if the book exists
        if (!bookRepository.existsById(book.getId())) {
            throw new IllegalArgumentException("Le livre n'existe pas");
        }

        // Récupérer l'ancien livre pour comparer le stock
        Book oldBook = bookRepository.findById(book.getId())
                .orElseThrow(() -> new IllegalArgumentException("Le livre n'existe pas"));

        boolean wasOutOfStock = oldBook.getQuantity() <= 0;
        boolean nowInStock = book.getQuantity() > 0;

        // Save the updated book
        Book updatedBook = bookRepository.save(book);

        // Notifier les utilisateurs si le livre est de nouveau en stock
        if (wasOutOfStock && nowInStock) {
            try {
                stockAlertService.notifyUsersForBook(book.getId());
            } catch (Exception e) {
                logger.error("Erreur lors de la notification des alertes: {}", e.getMessage());
            }
        }

        return updatedBook;
    }


    @Override
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<Book> findLowStockBooks() {
        // Retourner les livres avec quantité <= 2
        return bookRepository.findAll().stream()
            .filter(book -> book.getQuantity() <= 2)
            .toList();
    }

}
