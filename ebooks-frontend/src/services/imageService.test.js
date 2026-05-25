import { imageService } from './imageService';
import api from './api';

jest.mock('./api');

describe('imageService', () => {
    afterEach(() => {
        jest.clearAllMocks();
    });

    describe('uploadBookCover', () => {
        const createFile = (name = 'cover.jpg', type = 'image/jpeg', size = 1024) => {
            const file = new File(['x'.repeat(size)], name, { type });
            return file;
        };

        it('uploade un fichier JPEG valide et retourne l\'URL Cloudinary', async () => {
            api.post.mockResolvedValue({ data: { url: 'https://res.cloudinary.com/demo/image/upload/ebooks/covers/abc123.jpg' } });

            const file = createFile('cover.jpg', 'image/jpeg');
            const result = await imageService.uploadBookCover(file);

            expect(api.post).toHaveBeenCalledWith(
                '/api/rest/images/upload',
                expect.any(FormData),
                { headers: { 'Content-Type': 'multipart/form-data' } }
            );
            expect(result).toBe('https://res.cloudinary.com/demo/image/upload/ebooks/covers/abc123.jpg');
        });

        it('accepte les fichiers PNG, WebP et GIF', async () => {
            api.post.mockResolvedValue({ data: { url: 'https://res.cloudinary.com/demo/image.png' } });

            for (const type of ['image/png', 'image/webp', 'image/gif']) {
                const file = createFile(`cover.${type.split('/')[1]}`, type);
                const url = await imageService.uploadBookCover(file);
                expect(url).toBeDefined();
            }
        });

        it('rejette un format non autorisé (PDF)', async () => {
            const file = createFile('document.pdf', 'application/pdf');

            await expect(imageService.uploadBookCover(file))
                .rejects.toThrow('Format non supporté');
            expect(api.post).not.toHaveBeenCalled();
        });

        it('rejette un fichier dépassant 5 Mo', async () => {
            const bigFile = createFile('huge.jpg', 'image/jpeg', 6 * 1024 * 1024);

            await expect(imageService.uploadBookCover(bigFile))
                .rejects.toThrow('dépasse 5 Mo');
            expect(api.post).not.toHaveBeenCalled();
        });

        it('propage les erreurs réseau', async () => {
            api.post.mockRejectedValue(new Error('Network Error'));
            const file = createFile();

            await expect(imageService.uploadBookCover(file))
                .rejects.toThrow('Network Error');
        });
    });

    describe('deleteBookCover', () => {
        it('appelle DELETE avec l\'URL Cloudinary en query param', async () => {
            api.delete.mockResolvedValue({ data: { message: 'Supprimé' } });
            const url = 'https://res.cloudinary.com/demo/image/upload/ebooks/covers/abc.jpg';

            await imageService.deleteBookCover(url);

            expect(api.delete).toHaveBeenCalledWith('/api/rest/images/delete', { params: { url } });
        });

        it('ne fait rien si l\'URL n\'est pas une URL Cloudinary', async () => {
            await imageService.deleteBookCover('https://example.com/image.jpg');
            expect(api.delete).not.toHaveBeenCalled();
        });

        it('ne fait rien si l\'URL est null ou vide', async () => {
            await imageService.deleteBookCover(null);
            await imageService.deleteBookCover('');
            expect(api.delete).not.toHaveBeenCalled();
        });
    });
});

