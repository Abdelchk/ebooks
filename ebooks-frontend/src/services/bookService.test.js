import { bookService } from './bookService';
import api from './api';

jest.mock('./api');

describe('bookService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    it('getAllBooks appelle GET /api/rest/books/all', async () => {
        const mockBooks = [
            { id: 1, title: 'Les Misérables', author: 'Victor Hugo' },
            { id: 2, title: 'Notre-Dame de Paris', author: 'Victor Hugo' },
        ];
        api.get.mockResolvedValue({ data: mockBooks });

        const result = await bookService.getAllBooks();

        expect(api.get).toHaveBeenCalledWith('/api/rest/books/all');
        expect(result).toHaveLength(2);
        expect(result[0].title).toBe('Les Misérables');
    });

    it('getAllBooks propage les erreurs réseau', async () => {
        api.get.mockRejectedValue(new Error('Network Error'));

        await expect(bookService.getAllBooks()).rejects.toThrow('Network Error');
    });

    it('searchBooks appelle GET avec le paramètre q', async () => {
        api.get.mockResolvedValue({ data: [{ id: 1, title: 'Les Misérables' }] });

        const result = await bookService.searchBooks('Victor Hugo');

        expect(api.get).toHaveBeenCalledWith('/api/rest/books/search', { params: { q: 'Victor Hugo' } });
        expect(result).toHaveLength(1);
    });

    it('searchBooks retourne un tableau vide si aucun résultat', async () => {
        api.get.mockResolvedValue({ data: [] });

        const result = await bookService.searchBooks('XYZ inconnu');

        expect(result).toEqual([]);
    });

    it('getBooksByCategory appelle GET avec la catégorie', async () => {
        api.get.mockResolvedValue({ data: [{ id: 1, title: 'Roman classique', category: 'Fiction' }] });

        const result = await bookService.getBooksByCategory('Fiction');

        expect(api.get).toHaveBeenCalledWith('/api/rest/books/category/Fiction');
        expect(result[0].category).toBe('Fiction');
    });

    it('searchBooksByCategory appelle GET avec catégorie et query', async () => {
        api.get.mockResolvedValue({ data: [{ id: 1, title: 'Les Misérables' }] });

        const result = await bookService.searchBooksByCategory('Fiction', 'Hugo');

        expect(api.get).toHaveBeenCalledWith('/api/rest/books/category/Fiction/search', {
            params: { q: 'Hugo' },
        });
        expect(result).toHaveLength(1);
    });

    it('getBookById appelle GET /api/rest/books/:id', async () => {
        api.get.mockResolvedValue({ data: { id: 42, title: 'Le Comte de Monte-Cristo', author: 'Alexandre Dumas' } });

        const result = await bookService.getBookById(42);

        expect(api.get).toHaveBeenCalledWith('/api/rest/books/42');
        expect(result.id).toBe(42);
        expect(result.author).toBe('Alexandre Dumas');
    });

    it('createBook appelle POST /api/rest/books/create avec les données', async () => {
        const newBook = { title: 'Nouveau Livre', author: 'Auteur Test', isbn: '978-0000000000' };
        api.post.mockResolvedValue({ data: { id: 99, ...newBook } });

        const result = await bookService.createBook(newBook);

        expect(api.post).toHaveBeenCalledWith('/api/rest/books/create', newBook);
        expect(result.id).toBe(99);
        expect(result.title).toBe('Nouveau Livre');
    });

    it('updateBook appelle PUT /api/rest/books/update', async () => {
        const updatedBook = { id: 1, title: 'Les Misérables - Édition corrigée', author: 'Victor Hugo' };
        api.put.mockResolvedValue({ data: updatedBook });

        const result = await bookService.updateBook(updatedBook);

        expect(api.put).toHaveBeenCalledWith('/api/rest/books/update', updatedBook);
        expect(result.title).toBe('Les Misérables - Édition corrigée');
    });

    it('deleteBook appelle DELETE /api/rest/books/remove/:id', async () => {
        api.delete.mockResolvedValue({ data: { message: 'Livre supprimé' } });

        const result = await bookService.deleteBook(1);

        expect(api.delete).toHaveBeenCalledWith('/api/rest/books/remove/1');
        expect(result.message).toBe('Livre supprimé');
    });
});

