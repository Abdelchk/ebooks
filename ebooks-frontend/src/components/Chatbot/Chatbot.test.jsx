import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import Chatbot from './Chatbot';
import { chatService } from '../../services/chatService';

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

// ── Tests ─────────────────────────────────────────────────────────────────────
describe('Chatbot', () => {
    beforeEach(() => {
        jest.clearAllMocks();
        localStorage.clear();
        // Réinitialise les mocks de contexte à chaque test
        useAuth.mockReturnValue({ user: DEFAULT_USER });
        useCart.mockReturnValue({ cartCount: 0, refreshCartCount: mockRefreshCartCount });
        jest.spyOn(console, 'error').mockImplementation(() => {});
    });

    afterEach(() => {
        console.error.mockRestore();
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
        chatService.sendMessage = jest.fn().mockResolvedValue({
            reply: 'Voici Les Misérables',
            books: [{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 15 }]
        });

        render(<Chatbot />);
        openChatbot();

        const input = screen.getByLabelText("Message pour l'assistant");
        fireEvent.change(input, { target: { value: 'Hugo' } });
        fireEvent.click(screen.getByLabelText('Envoyer'));

        await waitFor(() => {
            expect(screen.getByText('Les Misérables')).toBeInTheDocument();
            expect(screen.getByLabelText('Ajouter Les Misérables au panier')).toBeInTheDocument();
        });
    });

    it('n\'affiche pas de bouton panier pour un livre épuisé', async () => {
        chatService.sendMessage = jest.fn().mockResolvedValue({
            reply: 'Livre épuisé',
            books: [{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 0 }]
        });

        render(<Chatbot />);
        openChatbot();

        const input = screen.getByLabelText("Message pour l'assistant");
        fireEvent.change(input, { target: { value: 'Hugo' } });
        fireEvent.click(screen.getByLabelText('Envoyer'));

        await waitFor(() => {
            expect(screen.getByText('Les Misérables')).toBeInTheDocument();
            expect(screen.queryByLabelText('Ajouter Les Misérables au panier')).not.toBeInTheDocument();
        });
    });

    it('efface l\'historique via le bouton 🗑️ et remet le message initial', () => {
        render(<Chatbot />);
        openChatbot();
        fireEvent.click(screen.getByLabelText("Effacer l'historique"));
        // Après effacement, le useEffect re-sauvegarde le message initial → ce n'est pas null
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
});






