package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe BookService
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IStockAlertService stockAlertService;

    @InjectMocks
    private BookService bookService;

    private Book validBook;

    @BeforeEach
    void setUp() {
        validBook = Book.builder()
                .id(1L)
                .title("Le Seigneur des Anneaux")
                .description("Un roman de fantasy épique")
                .isPublished(true)
                .publicationDate(new Date())
                .author("J.R.R. Tolkien")
                .category("Fantasy")
                .quantity(5)
                .build();
    }

    @AfterEach
    void tearDown() {
        validBook = null;
    }

    // ============ TESTS addOrUpdate ============

    @Test
    void shouldAddBookSuccessfully() {
        // GIVEN
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        // WHEN
        Book result = bookService.addOrUpdate(validBook);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Le Seigneur des Anneaux");
        assertThat(result.getAuthor()).isEqualTo("J.R.R. Tolkien");
        verify(bookRepository).save(validBook);
    }

    @Test
    void shouldThrowNullPointerWhenBookIsNull() {
        assertThatThrownBy(() -> bookService.addOrUpdate(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Book ne doit pas être null");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentWhenTitleIsBlank() {
        // GIVEN
        validBook.setTitle("   ");

        // WHEN / THEN
        assertThatThrownBy(() -> bookService.addOrUpdate(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tous les paramètres de book doivent être renseignés !");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentWhenDescriptionIsNull() {
        // GIVEN
        validBook.setDescription(null);

        // WHEN / THEN
        assertThatThrownBy(() -> bookService.addOrUpdate(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tous les paramètres de book doivent être renseignés !");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentWhenAuthorIsNull() {
        // GIVEN
        validBook.setAuthor(null);

        // WHEN / THEN
        assertThatThrownBy(() -> bookService.addOrUpdate(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tous les paramètres de book doivent être renseignés !");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void shouldThrowIllegalArgumentWhenCategoryIsBlank() {
        // GIVEN
        validBook.setCategory("");

        // WHEN / THEN
        assertThatThrownBy(() -> bookService.addOrUpdate(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tous les paramètres de book doivent être renseignés !");

        verify(bookRepository, never()).save(any());
    }

    // ============ TESTS getBookById ============

    @Test
    void shouldGetBookByIdSuccessfully() {
        // GIVEN
        when(bookRepository.findById(1L)).thenReturn(Optional.of(validBook));

        // WHEN
        Book result = bookService.getBookById(1L);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Le Seigneur des Anneaux");
        verify(bookRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenBookNotFoundById() {
        // GIVEN
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Livre non trouvé avec l'ID : 99");

        verify(bookRepository).findById(99L);
    }

    // ============ TESTS getBooks ============

    @Test
    void shouldReturnAllBooks() {
        // GIVEN
        Book book2 = Book.builder()
                .id(2L)
                .title("Harry Potter")
                .description("Sorcier")
                .isPublished(true)
                .publicationDate(new Date())
                .author("J.K. Rowling")
                .category("Fantasy")
                .quantity(3)
                .build();
        when(bookRepository.findAll()).thenReturn(List.of(validBook, book2));

        // WHEN
        List<Book> result = bookService.getBooks();

        // THEN
        assertThat(result).hasSize(2);
        verify(bookRepository).findAll();
    }

    // ============ TESTS updateBook ============

    @Test
    void shouldUpdateBookSuccessfully() {
        // GIVEN - livre en stock, on le remet en stock
        Book oldBook = Book.builder()
                .id(1L)
                .title("Ancien titre")
                .description("Ancienne description")
                .isPublished(false)
                .publicationDate(new Date())
                .author("Auteur")
                .category("Roman")
                .quantity(5)
                .build();

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(oldBook));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        // WHEN
        Book result = bookService.updateBook(validBook);

        // THEN
        assertThat(result).isNotNull();
        verify(bookRepository).existsById(1L);
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(validBook);
    }

    @Test
    void shouldNotifyUsersWhenBookRestockedAfterUpdate() {
        // GIVEN - ancien stock = 0, nouveau stock > 0
        Book outOfStockBook = Book.builder()
                .id(1L)
                .title("Titre")
                .description("Desc")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Auteur")
                .category("Roman")
                .quantity(0)
                .build();

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(outOfStockBook));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook); // validBook.quantity = 5

        // WHEN
        bookService.updateBook(validBook);

        // THEN
        verify(stockAlertService).notifyUsersForBook(1L);
    }

    @Test
    void shouldNotNotifyUsersWhenBookWasAlreadyInStock() {
        // GIVEN - ancien stock > 0, donc pas de notification
        Book inStockBook = Book.builder()
                .id(1L)
                .title("Titre")
                .description("Desc")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Auteur")
                .category("Roman")
                .quantity(3) // déjà en stock
                .build();

        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(inStockBook));
        when(bookRepository.save(any(Book.class))).thenReturn(validBook);

        // WHEN
        bookService.updateBook(validBook);

        // THEN - pas de notification car c'était déjà en stock
        verify(stockAlertService, never()).notifyUsersForBook(anyLong());
    }

    @Test
    void shouldThrowWhenUpdatingNullBook() {
        assertThatThrownBy(() -> bookService.updateBook(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Book ne doit pas être null");
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentBook() {
        // GIVEN
        when(bookRepository.existsById(1L)).thenReturn(false);

        // WHEN / THEN
        assertThatThrownBy(() -> bookService.updateBook(validBook))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Le livre n'existe pas");

        verify(bookRepository, never()).save(any());
    }

    // ============ TESTS deleteBook ============

    @Test
    void shouldDeleteBookById() {
        // GIVEN
        doNothing().when(bookRepository).deleteById(1L);

        // WHEN
        bookService.deleteBook(1L);

        // THEN
        verify(bookRepository).deleteById(1L);
    }

    // ============ TESTS findLowStockBooks ============

    @Test
    void shouldFindLowStockBooks() {
        // GIVEN
        Book lowStockBook = Book.builder()
                .id(2L)
                .title("Livre rare")
                .description("Peu en stock")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Auteur")
                .category("Roman")
                .quantity(1)
                .build();

        Book normalStockBook = Book.builder()
                .id(3L)
                .title("Livre normal")
                .description("Bien fourni")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Auteur 2")
                .category("Science-fiction")
                .quantity(10)
                .build();

        when(bookRepository.findAll()).thenReturn(List.of(lowStockBook, normalStockBook));

        // WHEN
        List<Book> result = bookService.findLowStockBooks();

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Livre rare");
        verify(bookRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoLowStockBooks() {
        // GIVEN
        when(bookRepository.findAll()).thenReturn(List.of(validBook)); // quantity = 5

        // WHEN
        List<Book> result = bookService.findLowStockBooks();

        // THEN
        assertThat(result).isEmpty();
    }

    // ============ TESTS searchBooks ============

    @Test
    void shouldSearchBooksByQuery() {
        // GIVEN
        when(bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("tolkien", "tolkien"))
                .thenReturn(List.of(validBook));

        // WHEN
        List<Book> result = bookService.searchBooks("tolkien");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("J.R.R. Tolkien");
    }

    @Test
    void shouldGetBooksByCategory() {
        // GIVEN
        when(bookRepository.findByCategory("Fantasy")).thenReturn(List.of(validBook));

        // WHEN
        List<Book> result = bookService.getBooksByCategory("Fantasy");

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Fantasy");
        verify(bookRepository).findByCategory("Fantasy");
    }
}

