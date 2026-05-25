import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import Chatbot from './Chatbot';
import { chatService } from '../../services/chatService';
import { cartService } from '../../services/cartService';

// ── Mocks ────────────────────────────────────────────────────────────────────
jest.mock('../../services/chatService');
jest.mock('../../services/cartService');
jest.mock('../../context/AuthContext', () => ({
    useAuth: jest.fn()
}));
jest.mock('../../context/CartContext', () => ({
    useCart: jest.fn()
}));

// ── Setup ─────────────────────────────────────────────────────────────────────
// JSDOM ne supporte pas scrollIntoView → on le mocke
beforeAll(() => {
    window.HTMLElement.prototype.scrollIntoView = jest.fn();
});

const { useAuth } = require('../../context/AuthContext');
const { useCart } = require('../../context/CartContext');

const DEFAULT_USER = { id: 1, email: 'user@test.com' };
const mockRefreshCartCount = jest.fn();

// ── Helpers ───────────────────────────────────────────────────────────────────
const openChatbot = () => {
    fireEvent.click(screen.getByLabelText("Ouvrir l'assistant bibliothèque"));
};

const sendMessageWithBooks = async (books) => {
    chatService.sendMessage = jest.fn().mockResolvedValue({ reply: 'Résultat', books });
    const input = screen.getByLabelText("Message pour l'assistant");
    fireEvent.change(input, { target: { value: 'Hugo' } });
    fireEvent.click(screen.getByLabelText('Envoyer'));
    await waitFor(() => expect(screen.getByText('Résultat')).toBeInTheDocument());
};

// ── Tests ─────────────────────────────────────────────────────────────────────
describe('Chatbot', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        localStorage.clear();
        // Réinitialise les mocks de contexte à chaque test
        useAuth.mockReturnValue({ user: DEFAULT_USER });
        useCart.mockReturnValue({ cartCount: 0, refreshCartCount: mockRefreshCartCount });
        jest.spyOn(console, 'error').mockImplementation(() => {});
        jest.spyOn(console, 'warn').mockImplementation(() => {});
    });

    afterEach(() => {
        console.error.mockRestore();
        console.warn.mockRestore();
    });

    it('n\'est pas visible si l\'utilisateur n\'est pas connecté', () => {
        useAuth.mockReturnValue({ user: null });
        const { container } = render(<Chatbot />);
        expect(container.firstChild).toBeNull();
    });

    it('affiche le bouton flottant si l\'utilisateur est connecté', () => {
        render(<Chatbot />);
        expect(screen.getByLabelText("Ouvrir l'assistant bibliothèque")).toBeInTheDocument();
    });

    it('ouvre la fenêtre de chat au clic sur le bouton', () => {
        render(<Chatbot />);
        openChatbot();
        expect(screen.getByRole('dialog')).toBeInTheDocument();
        expect(screen.getByText(/Bonjour/)).toBeInTheDocument();
    });

    it('affiche ✕ quand le chat est ouvert et 💬 quand il est fermé', () => {
        render(<Chatbot />);
        const btn = screen.getByLabelText("Ouvrir l'assistant bibliothèque");
        expect(btn.textContent).toBe('💬');
        fireEvent.click(btn);
        expect(btn.textContent).toBe('✕');
        fireEvent.click(screen.getByLabelText('Fermer'));
        expect(btn.textContent).toBe('💬');
    });

    it('ferme la fenêtre de chat au clic sur ✕', () => {
        render(<Chatbot />);
        openChatbot();
        fireEvent.click(screen.getByLabelText('Fermer'));
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('envoie un message et affiche la réponse du bot', async () => {
        chatService.sendMessage = jest.fn().mockResolvedValue({
            reply: 'Voici les livres trouvés !',
            books: []
        });

        render(<Chatbot />);
        openChatbot();

        const input = screen.getByLabelText("Message pour l'assistant");
        fireEvent.change(input, { target: { value: 'Victor Hugo' } });
        fireEvent.click(screen.getByLabelText('Envoyer'));

        await waitFor(() => {
            expect(screen.getByText('Voici les livres trouvés !')).toBeInTheDocument();
        });
        expect(chatService.sendMessage).toHaveBeenCalledWith('Victor Hugo');
    });

    it('affiche un message d\'erreur si le service échoue', async () => {
        chatService.sendMessage = jest.fn().mockRejectedValue(new Error('API Error'));

        render(<Chatbot />);
        openChatbot();

        const input = screen.getByLabelText("Message pour l'assistant");
        fireEvent.change(input, { target: { value: 'test' } });
        fireEvent.click(screen.getByLabelText('Envoyer'));

        await waitFor(() => {
            expect(screen.getByText(/Une erreur est survenue/)).toBeInTheDocument();
        });
    });

    it('envoie le message avec la touche Entrée', async () => {
        chatService.sendMessage = jest.fn().mockResolvedValue({ reply: 'OK', books: [] });

        render(<Chatbot />);
        openChatbot();

        const input = screen.getByLabelText("Message pour l'assistant");
        fireEvent.change(input, { target: { value: 'Bonjour' } });
        fireEvent.keyDown(input, { key: 'Enter' });

        await waitFor(() => {
            expect(chatService.sendMessage).toHaveBeenCalledWith('Bonjour');
        });
    });

    it('affiche les cartes livres avec le bouton panier si disponible', async () => {
        render(<Chatbot />);
        openChatbot();
        await sendMessageWithBooks([{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 15 }]);

        expect(screen.getByText('Les Misérables')).toBeInTheDocument();
        expect(screen.getByLabelText('Ajouter Les Misérables au panier')).toBeInTheDocument();
    });

    it('n\'affiche pas de bouton panier pour un livre épuisé', async () => {
        render(<Chatbot />);
        openChatbot();
        await sendMessageWithBooks([{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 0 }]);

        expect(screen.getByText('Les Misérables')).toBeInTheDocument();
        expect(screen.queryByLabelText('Ajouter Les Misérables au panier')).not.toBeInTheDocument();
        expect(screen.getByText(/Épuisé/)).toBeInTheDocument();
    });

    it('ajoute un livre au panier avec succès et affiche "✅ Ajouté !"', async () => {
        cartService.addToCart = jest.fn().mockResolvedValue({});

        render(<Chatbot />);
        openChatbot();
        await sendMessageWithBooks([{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 15 }]);

        fireEvent.click(screen.getByLabelText('Ajouter Les Misérables au panier'));

        await waitFor(() => {
            expect(cartService.addToCart).toHaveBeenCalledWith(3, 14);
            expect(mockRefreshCartCount).toHaveBeenCalled();
            expect(screen.getByText('✅ Ajouté !')).toBeInTheDocument();
        });
    });

    it('affiche une erreur si l\'ajout au panier échoue', async () => {
        cartService.addToCart = jest.fn().mockRejectedValue(new Error('Panier error'));

        render(<Chatbot />);
        openChatbot();
        await sendMessageWithBooks([{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 15 }]);

        fireEvent.click(screen.getByLabelText('Ajouter Les Misérables au panier'));

        await waitFor(() => {
            expect(screen.getByText(/Impossible d'ajouter au panier/)).toBeInTheDocument();
        });
    });

    it('efface l\'historique via le bouton 🗑️ et remet le message initial', () => {
        render(<Chatbot />);
        openChatbot();
        fireEvent.click(screen.getByLabelText("Effacer l'historique"));
        // Après effacement, le useEffect re-sauvegarde le message initial
        const saved = JSON.parse(localStorage.getItem('chatbot_history'));
        expect(saved).toHaveLength(1);
        expect(saved[0].role).toBe('bot');
    });

    it('ne soumet pas un message vide', () => {
        render(<Chatbot />);
        openChatbot();
        fireEvent.click(screen.getByLabelText('Envoyer'));
        expect(chatService.sendMessage).not.toHaveBeenCalled();
    });

    it('charge l\'historique depuis le localStorage au montage', () => {
        const history = [
            { id: 'initial', role: 'bot', text: '👋 Bonjour ! Je suis votre assistant bibliothèque.\nPosez-moi une question ou cherchez un livre par titre ou auteur !' },
            { id: 'user-1', role: 'user', text: 'message précédent' },
            { id: 'bot-1', role: 'bot', text: 'réponse précédente' },
        ];
        localStorage.setItem('chatbot_history', JSON.stringify(history));

        render(<Chatbot />);
        openChatbot();

        expect(screen.getByText('message précédent')).toBeInTheDocument();
        expect(screen.getByText('réponse précédente')).toBeInTheDocument();
    });

    it('ignore un localStorage invalide et affiche le message initial', () => {
        localStorage.setItem('chatbot_history', 'JSON_INVALIDE{{{');

        render(<Chatbot />);
        openChatbot();

        expect(screen.getByText(/Bonjour/)).toBeInTheDocument();
    });

    it('ignore un localStorage contenant un non-tableau et affiche le message initial', () => {
        localStorage.setItem('chatbot_history', JSON.stringify({ not: 'array' }));

        render(<Chatbot />);
        openChatbot();

        expect(screen.getByText(/Bonjour/)).toBeInTheDocument();
    });

    it('supprime l\'erreur panier automatiquement après le délai', async () => {
        jest.useFakeTimers();
        cartService.addToCart = jest.fn().mockRejectedValue(new Error('err'));

        render(<Chatbot />);
        openChatbot();
        await sendMessageWithBooks([{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 15 }]);

        fireEvent.click(screen.getByLabelText('Ajouter Les Misérables au panier'));

        await waitFor(() => {
            expect(screen.getByText(/Impossible d'ajouter au panier/)).toBeInTheDocument();
        });

        act(() => { jest.advanceTimersByTime(4100); });

        await waitFor(() => {
            expect(screen.queryByText(/Impossible d'ajouter au panier/)).not.toBeInTheDocument();
        });

        jest.useRealTimers();
    });
});






