package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.entity.CartItem;
import fr.ensitech.ebooks.entity.User;
import fr.ensitech.ebooks.repository.IBookRepository;
import fr.ensitech.ebooks.repository.ICartItemRepository;
import fr.ensitech.ebooks.repository.IUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour la classe CartService
 */
@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ICartItemRepository cartItemRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IBookRepository bookRepository;

    @InjectMocks
    private CartService cartService;

    private User user;
    private Book book;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstname("Marie")
                .lastname("Curie")
                .email("marie.curie@test.com")
                .password("Password123@")
                .birthdate(LocalDate.of(1985, 3, 20))
                .phoneNumber("0611223344")
                .enabled(true)
                .build();

        book = Book.builder()
                .id(10L)
                .title("Les Misérables")
                .description("Roman de Victor Hugo")
                .isPublished(true)
                .publicationDate(new Date())
                .author("Victor Hugo")
                .category("Classique")
                .quantity(4)
                .build();

        cartItem = CartItem.builder()
                .id(100L)
                .user(user)
                .book(book)
                .loanDuration(14)
                .build();
    }

    @AfterEach
    void tearDown() {
        user = null;
        book = null;
        cartItem = null;
    }

    // ============ TESTS addToCart ============

    @Test
    void shouldAddBookToCartSuccessfully() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(cartItemRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItem);

        // WHEN
        CartItem result = cartService.addToCart(1L, 10L, 14);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getBook().getTitle()).isEqualTo("Les Misérables");
        assertThat(result.getLoanDuration()).isEqualTo(14);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void shouldAddBookToCartWithDefaultDurationWhenNull() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(cartItemRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        CartItem result = cartService.addToCart(1L, 10L, null);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getLoanDuration()).isEqualTo(14); // durée par défaut
    }

    @Test
    void shouldThrowWhenUserNotFoundOnAddToCart() {
        // GIVEN
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.addToCart(99L, 10L, 14))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Utilisateur non trouvé");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBookNotFoundOnAddToCart() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.addToCart(1L, 99L, 14))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Livre non trouvé");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBookIsOutOfStockOnAddToCart() {
        // GIVEN
        book.setQuantity(0);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.addToCart(1L, 10L, 14))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Livre non disponible en stock");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenBookAlreadyInCart() {
        // GIVEN
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(cartItemRepository.findByUserIdAndBookId(1L, 10L)).thenReturn(Optional.of(cartItem));

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.addToCart(1L, 10L, 14))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ce livre est déjà dans votre panier");

        verify(cartItemRepository, never()).save(any());
    }

    // ============ TESTS removeFromCart ============

    @Test
    void shouldRemoveCartItemSuccessfully() {
        // GIVEN
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);

        // WHEN
        cartService.removeFromCart(100L, 1L);

        // THEN
        verify(cartItemRepository).delete(cartItem);
    }

    @Test
    void shouldThrowWhenCartItemNotFound() {
        // GIVEN
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.removeFromCart(999L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Article non trouvé dans le panier");
    }

    @Test
    void shouldThrowWhenUserNotAuthorizedToRemoveCartItem() {
        // GIVEN - l'item appartient à userId=1, mais userId=2 tente de le supprimer
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.removeFromCart(100L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Vous n'êtes pas autorisé à supprimer cet article");

        verify(cartItemRepository, never()).delete(any());
    }

    // ============ TESTS clearCart ============

    @Test
    void shouldClearCartSuccessfully() {
        // GIVEN
        doNothing().when(cartItemRepository).deleteByUserId(1L);

        // WHEN
        cartService.clearCart(1L);

        // THEN
        verify(cartItemRepository).deleteByUserId(1L);
    }

    // ============ TESTS getCartItems ============

    @Test
    void shouldReturnCartItemsForUser() {
        // GIVEN
        when(cartItemRepository.findByUserId(1L)).thenReturn(List.of(cartItem));

        // WHEN
        List<CartItem> result = cartService.getCartItems(1L);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBook().getTitle()).isEqualTo("Les Misérables");
        verify(cartItemRepository).findByUserId(1L);
    }

    // ============ TESTS updateLoanDuration ============

    @Test
    void shouldUpdateLoanDurationSuccessfully() {
        // GIVEN
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        CartItem result = cartService.updateLoanDuration(100L, 1L, 21);

        // THEN
        assertThat(result.getLoanDuration()).isEqualTo(21);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void shouldThrowWhenLoanDurationIsLessThanOne() {
        // GIVEN
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.updateLoanDuration(100L, 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La durée d'emprunt doit être entre 1 et 30 jours");
    }

    @Test
    void shouldThrowWhenLoanDurationExceedsThirtyDays() {
        // GIVEN
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.updateLoanDuration(100L, 1L, 31))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La durée d'emprunt doit être entre 1 et 30 jours");
    }

    @Test
    void shouldThrowWhenUserNotAuthorizedToUpdateDuration() {
        // GIVEN - l'item appartient à userId=1, mais userId=5 tente de modifier
        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(cartItem));

        // WHEN / THEN
        assertThatThrownBy(() -> cartService.updateLoanDuration(100L, 5L, 21))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non autorisé");
    }

    // ============ TESTS getCartItemCount ============

    @Test
    void shouldReturnCartItemCount() {
        // GIVEN
        when(cartItemRepository.countByUserId(1L)).thenReturn(3L);

        // WHEN
        long count = cartService.getCartItemCount(1L);

        // THEN
        assertThat(count).isEqualTo(3L);
        verify(cartItemRepository).countByUserId(1L);
    }
}

