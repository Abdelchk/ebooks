import React, { useState, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';
import { chatService } from '../../services/chatService';
import { cartService } from '../../services/cartService';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import './Chatbot.css';

const STORAGE_KEY = 'chatbot_history';
const MAX_HISTORY_SIZE = 100; // Limite la taille pour éviter localStorage overflow

const INITIAL_MESSAGE = {
    id: 'initial',
    role: 'bot',
    text: '👋 Bonjour ! Je suis votre assistant bibliothèque.\nPosez-moi une question ou cherchez un livre par titre ou auteur !',
};

// Sanitize un message avant stockage (supprime les propriétés non attendues)
const sanitizeMessage = (msg) => ({
    id: msg.id,
    role: msg.role === 'user' || msg.role === 'bot' ? msg.role : 'bot',
    text: typeof msg.text === 'string' ? msg.text.slice(0, 2000) : '',
    books: Array.isArray(msg.books) ? msg.books.map(b => ({
        id: Number(b.id),
        title: String(b.title || '').slice(0, 200),
        author: String(b.author || '').slice(0, 100),
        quantity: Number(b.quantity) || 0,
        addedToCart: Boolean(b.addedToCart),
    })) : undefined,
});

const loadHistory = () => {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);
        if (!saved) return [INITIAL_MESSAGE];
        const parsed = JSON.parse(saved);
        if (!Array.isArray(parsed)) return [INITIAL_MESSAGE];
        return parsed.map(sanitizeMessage).slice(-MAX_HISTORY_SIZE);
    } catch (e) {
        // localStorage indisponible ou JSON invalide
        console.warn('Impossible de charger l\'historique du chat:', e.message);
        return [INITIAL_MESSAGE];
    }
};

// Extrait : met à jour addedToCart pour un livre donné dans les messages
const markBookAsAdded = (messages, bookId) =>
    messages.map(msg => {
        if (!msg.books?.some(b => b.id === bookId)) return msg;
        return { ...msg, books: msg.books.map(b => b.id === bookId ? { ...b, addedToCart: true } : b) };
    });

const Chatbot = () => {
    const { user } = useAuth();
    const { refreshCartCount } = useCart();
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState(loadHistory);
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [addingBook, setAddingBook] = useState(null);
    const [cartError, setCartError] = useState(null);
    const messagesEndRef = useRef(null);

    // Scroll automatique vers le bas à chaque nouveau message
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, loading]);

    // Sauvegarde automatique dans localStorage (sanitized)
    useEffect(() => {
        try {
            const sanitized = messages.map(sanitizeMessage).slice(-MAX_HISTORY_SIZE);
            localStorage.setItem(STORAGE_KEY, JSON.stringify(sanitized));
        } catch (e) {
            // localStorage plein ou désactivé
            console.warn('Impossible de sauvegarder l\'historique du chat:', e.message);
        }
    }, [messages]);

    // Effacer l'historique
    const handleClearHistory = () => {
        setMessages([INITIAL_MESSAGE]);
        localStorage.removeItem(STORAGE_KEY);
    };

    // Le chatbot n'est visible que pour les utilisateurs connectés
    if (!user) return null;

    const handleSend = async () => {
        const trimmed = input.trim();
        if (!trimmed || loading) return;

        const userMsg = { id: `user-${Date.now()}`, role: 'user', text: trimmed };
        setMessages(prev => [...prev, userMsg]);
        setInput('');
        setLoading(true);

        try {
            const data = await chatService.sendMessage(trimmed);
            const botMsg = {
                id: `bot-${Date.now()}`,
                role: 'bot',
                text: data.reply,
                books: data.books || []
            };
            setMessages(prev => [...prev, botMsg]);
        } catch (err) {
            console.error('Erreur chatbot:', err.message);
            setMessages(prev => [...prev, {
                id: `err-${Date.now()}`,
                role: 'bot',
                text: '❌ Une erreur est survenue. Veuillez réessayer.'
            }]);
        } finally {
            setLoading(false);
        }
    };

    const handleAddToCart = async (book) => {
        setAddingBook(book.id);
        try {
            await cartService.addToCart(book.id, 14);
            await refreshCartCount();
            setMessages(prev => markBookAsAdded(prev, book.id));
        } catch (err) {
            console.error('Erreur ajout panier:', err.message);
            setCartError('Impossible d\'ajouter au panier. Vérifiez votre connexion.');
            setTimeout(() => setCartError(null), 4000);
        } finally {
            setAddingBook(null);
        }
    };

    return (
        <>
            {/* ── Bouton flottant ── */}
            <button
                className="chatbot-toggle"
                onClick={() => setIsOpen(prev => !prev)}
                aria-label="Ouvrir l'assistant bibliothèque"
                title="Assistant bibliothèque"
            >
                {isOpen ? '✕' : '💬'}
            </button>

            {/* ── Fenêtre de chat : <dialog> pour l'accessibilité ── */}
            {isOpen && (
                <dialog
                    className="chatbot-window"
                    aria-label="Assistant bibliothèque"
                    open
                >
                    {/* Header */}
                    <div className="chatbot-header">
                        <span>🤖 Assistant Bibliothèque</span>
                        <div style={{ display: 'flex', gap: 8 }}>
                            <button
                                onClick={handleClearHistory}
                                aria-label="Effacer l'historique"
                                title="Effacer l'historique"
                                style={{ fontSize: 14 }}
                            >
                                🗑️
                            </button>
                            <button onClick={() => setIsOpen(false)} aria-label="Fermer">✕</button>
                        </div>
                    </div>

                    {/* Messages */}
                    <div className="chatbot-messages">
                        {messages.map(msg => (
                            <div key={msg.id} className={`message ${msg.role}`}>
                                <div className="message-bubble">{msg.text}</div>

                                {/* Cartes livres avec bouton panier */}
                                {msg.books?.map(book => (
                                    <BookCard
                                        key={book.id}
                                        book={book}
                                        addingBook={addingBook}
                                        onAddToCart={handleAddToCart}
                                    />
                                ))}
                            </div>
                        ))}

                        {/* Indicateur de frappe */}
                        {loading && (
                            <div className="message bot">
                                <div className="typing-indicator">
                                    <span /><span /><span />
                                </div>
                            </div>
                        )}

                        <div ref={messagesEndRef} />
                    </div>

                    {/* Erreur panier */}
                    {cartError && (
                        <div style={{ background: '#fee2e2', color: '#991b1b', padding: '6px 10px', fontSize: 13, margin: '4px 8px', borderRadius: 6 }}>
                            {cartError}
                        </div>
                    )}

                    {/* Saisie */}
                    <div className="chatbot-input">
                        <input
                            type="text"
                            value={input}
                            onChange={e => setInput(e.target.value)}
                            onKeyDown={e => e.key === 'Enter' && handleSend()}
                            placeholder="Cherchez un livre, un auteur..."
                            disabled={loading}
                            aria-label="Message pour l'assistant"
                            autoFocus
                        />
                        <button
                            onClick={handleSend}
                            disabled={loading || !input.trim()}
                            aria-label="Envoyer"
                        >
                            ➤
                        </button>
                    </div>
                </dialog>
            )}
        </>
    );
};

// ── Sous-composant BookCard (réduit la profondeur d'imbrication) ──────────────
const BookCard = ({ book, addingBook, onAddToCart }) => {
    const isAdding = addingBook === book.id;
    const isAvailable = book.quantity > 0;

    return (
        <div className="chatbot-book-card">
            <div className="chatbot-book-info">
                <div className="chatbot-book-title" title={book.title}>{book.title}</div>
                <div className="chatbot-book-author">{book.author}</div>
            </div>
            <span className={`chatbot-book-badge ${isAvailable ? 'available' : 'unavailable'}`}>
                {isAvailable ? `✅ ${book.quantity} dispo` : '❌ Épuisé'}
            </span>
            {isAvailable && (
                book.addedToCart
                    ? <span style={{ color: '#166534', fontSize: 13 }}>✅ Ajouté !</span>
                    : (
                        <button
                            className="chatbot-add-btn"
                            onClick={() => onAddToCart(book)}
                            disabled={isAdding}
                            aria-label={`Ajouter ${book.title} au panier`}
                        >
                            {isAdding ? '...' : '🛒'}
                        </button>
                    )
            )}
        </div>
    );
};

BookCard.propTypes = {
    book: PropTypes.shape({
        id: PropTypes.number.isRequired,
        title: PropTypes.string.isRequired,
        author: PropTypes.string.isRequired,
        quantity: PropTypes.number.isRequired,
        addedToCart: PropTypes.bool,
    }).isRequired,
    addingBook: PropTypes.number,
    onAddToCart: PropTypes.func.isRequired,
};

export default Chatbot;
