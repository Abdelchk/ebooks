import React, { useState, useRef, useEffect } from 'react';
import { chatService } from '../../services/chatService';
import { cartService } from '../../services/cartService';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import './Chatbot.css';

const STORAGE_KEY = 'chatbot_history';

const INITIAL_MESSAGE = {
    role: 'bot',
    text: '👋 Bonjour ! Je suis votre assistant bibliothèque.\nPosez-moi une question ou cherchez un livre par titre ou auteur !',
};

const loadHistory = () => {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);
        return saved ? JSON.parse(saved) : [INITIAL_MESSAGE];
    } catch {
        return [INITIAL_MESSAGE];
    }
};

const Chatbot = () => {
    const { user } = useAuth();
    const { refreshCartCount } = useCart();
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState(loadHistory);  // ← chargé depuis localStorage
    const [input, setInput] = useState('');
    const [loading, setLoading] = useState(false);
    const [addingBook, setAddingBook] = useState(null);
    const messagesEndRef = useRef(null);

    // Scroll automatique vers le bas à chaque nouveau message
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, loading]);

    // Sauvegarde automatique dans localStorage à chaque changement de messages
    useEffect(() => {
        try {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(messages));
        } catch {
            // localStorage plein ou désactivé → on ignore silencieusement
        }
    }, [messages]);

    // Effacer l'historique
    const handleClearHistory = () => {
        const fresh = [INITIAL_MESSAGE];
        setMessages(fresh);
        localStorage.removeItem(STORAGE_KEY);
    };

    // Le chatbot n'est visible que pour les utilisateurs connectés
    if (!user) return null;

    const handleSend = async () => {
        const trimmed = input.trim();
        if (!trimmed || loading) return;

        // Ajouter le message utilisateur
        setMessages(prev => [...prev, { role: 'user', text: trimmed }]);
        setInput('');
        setLoading(true);

        try {
            const data = await chatService.sendMessage(trimmed);
            setMessages(prev => [...prev, {
                role: 'bot',
                text: data.reply,
                books: data.books || []
            }]);
        } catch (err) {
            setMessages(prev => [...prev, {
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
            await refreshCartCount(); // ← met à jour le badge panier dans la navbar
            // Remplacer le bouton par un message de confirmation
            setMessages(prev => prev.map(msg =>
                msg.books?.some(b => b.id === book.id)
                    ? {
                        ...msg,
                        books: msg.books.map(b =>
                            b.id === book.id ? { ...b, addedToCart: true } : b
                        )
                    }
                    : msg
            ));
        } catch (err) {
            alert('Impossible d\'ajouter au panier. Vérifiez votre connexion.');
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

            {/* ── Fenêtre de chat ── */}
            {isOpen && (
                <div className="chatbot-window" role="dialog" aria-label="Assistant bibliothèque">

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
                        {messages.map((msg, i) => (
                            <div key={i} className={`message ${msg.role}`}>
                                <div className="message-bubble">{msg.text}</div>

                                {/* Cartes livres avec bouton panier */}
                                {msg.books?.map(book => (
                                    <div key={book.id} className="chatbot-book-card">
                                        <div className="chatbot-book-info">
                                            <div className="chatbot-book-title" title={book.title}>
                                                {book.title}
                                            </div>
                                            <div className="chatbot-book-author">{book.author}</div>
                                        </div>

                                        <span className={`chatbot-book-badge ${book.quantity > 0 ? 'available' : 'unavailable'}`}>
                                            {book.quantity > 0 ? `✅ ${book.quantity} dispo` : '❌ Épuisé'}
                                        </span>

                                        {book.quantity > 0 && (
                                            book.addedToCart
                                                ? <span style={{ color: '#166534', fontSize: 13 }}>✅ Ajouté !</span>
                                                : (
                                                    <button
                                                        className="chatbot-add-btn"
                                                        onClick={() => handleAddToCart(book)}
                                                        disabled={addingBook === book.id}
                                                        aria-label={`Ajouter ${book.title} au panier`}
                                                    >
                                                        {addingBook === book.id ? '...' : '🛒'}
                                                    </button>
                                                )
                                        )}
                                    </div>
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
                </div>
            )}
        </>
    );
};

export default Chatbot;

