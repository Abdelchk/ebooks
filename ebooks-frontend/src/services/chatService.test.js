import { chatService } from './chatService';
import api from './api';

jest.mock('./api');

describe('chatService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('sendMessage appelle POST /api/rest/chat avec le bon payload', async () => {
        api.post.mockResolvedValue({ data: { reply: 'Bonjour !', books: [] } });

        const result = await chatService.sendMessage('Cherche Victor Hugo');

        expect(api.post).toHaveBeenCalledWith('/api/rest/chat', { message: 'Cherche Victor Hugo' });
        expect(result.reply).toBe('Bonjour !');
        expect(result.books).toEqual([]);
    });

    it('sendMessage retourne les livres trouvés par le backend', async () => {
        const mockData = {
            reply: 'Voici Les Misérables',
            books: [{ id: 3, title: 'Les Misérables', author: 'Victor Hugo', quantity: 15 }]
        };
        api.post.mockResolvedValue({ data: mockData });

        const result = await chatService.sendMessage('Victor Hugo');

        expect(result.books).toHaveLength(1);
        expect(result.books[0].title).toBe('Les Misérables');
    });

    it('propage les erreurs réseau', async () => {
        api.post.mockRejectedValue(new Error('Network Error'));

        await expect(chatService.sendMessage('Hello')).rejects.toThrow('Network Error');
    });
});

