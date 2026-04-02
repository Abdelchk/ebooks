package fr.ensitech.ebooks.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;

@Service
public class BookService implements IBookService {
	
	@Autowired
	private IBookRepository bookRepository;

	@Override
	public Book addOrUpdate(Book book) throws Exception {
		
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
    public Book getBookById(Long id) throws Exception {
        return bookRepository.findById(id)
            .orElseThrow(() -> new Exception("Livre non trouvé avec l'ID : " + id));
    }

    @Override
	public List<Book> getBooks() throws Exception {;
		return bookRepository.findAll();
	}

	@Override
	public List<Book> getBooksByTitleContaining(String texte) throws Exception {
		return bookRepository.findByTitleContaining(texte);
	}

	@Override
	public List<Book> searchBooks(String query) throws Exception {
		return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
	}

	@Override
	public List<Book> getBooksByCategory(String category) throws Exception {
		return bookRepository.findByCategory(category);
	}

	@Override
	public List<Book> searchBooksByCategory(String category, String query) throws Exception {
		return bookRepository.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndAuthorContainingIgnoreCase(
				category, query, category, query);
	}

	@Override
	public Book updateBook(Book book) throws Exception {
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
			IStockAlertService stockAlertService = new StockAlertService();
	        try {
	            stockAlertService.notifyUsersForBook(book.getId());
	        } catch (Exception e) {
	            // Log l'erreur mais ne pas faire échouer la mise à jour
	            System.err.println("Erreur lors de la notification des alertes: " + e.getMessage());
	        }
	    }

	    return updatedBook;
	}


	@Override
	public void deleteBook(Long id) throws Exception {
		bookRepository.deleteById(id);
	}

}
