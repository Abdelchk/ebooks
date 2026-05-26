import React from 'react';
import { render, screen, fireEvent, act } from '@testing-library/react';
import BookCoverUpload from './BookCoverUpload';
import { imageService } from '../services/imageService';

// ── Mocks ─────────────────────────────────────────────────────────────────────
jest.mock('../services/imageService');

const CLOUDINARY_URL = 'https://res.cloudinary.com/demo/image/upload/covers/book.jpg';
const NEW_CLOUDINARY_URL = 'https://res.cloudinary.com/demo/image/upload/covers/new.jpg';
const LOCAL_BLOB_URL = 'blob:http://localhost/fake-object-url';

beforeAll(() => {
    global.URL.createObjectURL = jest.fn(() => LOCAL_BLOB_URL);
    global.URL.revokeObjectURL = jest.fn();
});

afterEach(() => {
    jest.clearAllMocks();
});

// ── Helpers ───────────────────────────────────────────────────────────────────
const makeFile = (name = 'cover.jpg', type = 'image/jpeg', size = 1024) =>
    new File(['x'.repeat(size)], name, { type });

const renderComponent = (props = {}) => {
    const onUploadSuccess = props.onUploadSuccess ?? jest.fn();
    const view = render(
        <BookCoverUpload onUploadSuccess={onUploadSuccess} {...props} />
    );
    return { ...view, onUploadSuccess };
};

const uploadFile = (input, file) => {
    fireEvent.change(input, { target: { files: [file] } });
};

// ── Tests ─────────────────────────────────────────────────────────────────────
describe('BookCoverUpload', () => {

    // ── Rendu initial ──────────────────────────────────────────────────────────
    describe('rendu initial', () => {
        it('affiche le placeholder et le bouton "Ajouter une couverture" sans image initiale', () => {
            renderComponent();

            expect(screen.getByText(/Ajouter une couverture/i)).toBeInTheDocument();
            expect(screen.getByText(/Cliquer pour/i)).toBeInTheDocument();
            expect(screen.queryByRole('img')).not.toBeInTheDocument();
        });

        it('affiche l\'image existante et le bouton "Changer la couverture" avec currentImageUrl', () => {
            renderComponent({ currentImageUrl: CLOUDINARY_URL });

            const img = screen.getByAltText(/Prévisualisation de la couverture/i);
            expect(img).toBeInTheDocument();
            expect(img).toHaveAttribute('src', CLOUDINARY_URL);
            expect(screen.getByText(/Changer la couverture/i)).toBeInTheDocument();
        });

        it('affiche le texte sur les formats acceptés', () => {
            renderComponent();
            expect(screen.getByText(/Formats acceptés/i)).toBeInTheDocument();
        });

        it('n\'affiche pas d\'erreur au départ', () => {
            renderComponent();
            expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        });
    });

    // ── Interaction clic ───────────────────────────────────────────────────────
    describe('interaction utilisateur', () => {
        it('le bouton preview déclenche le clic sur l\'input file', () => {
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);
            const clickSpy = jest.spyOn(fileInput, 'click');

            fireEvent.click(screen.getByLabelText(/Cliquer pour choisir une image de couverture/i));

            expect(clickSpy).toHaveBeenCalledTimes(1);
        });

        it('le bouton "Ajouter une couverture" déclenche le clic sur l\'input file', () => {
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);
            const clickSpy = jest.spyOn(fileInput, 'click');

            fireEvent.click(screen.getByText(/Ajouter une couverture/i));

            expect(clickSpy).toHaveBeenCalledTimes(1);
        });

        it('ne fait rien si l\'événement change n\'a pas de fichier', async () => {
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            fireEvent.change(fileInput, { target: { files: [] } });

            expect(imageService.uploadBookCover).not.toHaveBeenCalled();
        });
    });

    // ── Upload réussi ──────────────────────────────────────────────────────────
    describe('upload réussi', () => {
        it('upload sans image Cloudinary existante : n\'appelle pas deleteBookCover', async () => {
            imageService.uploadBookCover.mockResolvedValue(NEW_CLOUDINARY_URL);
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => {
                uploadFile(fileInput, makeFile());
            });

            expect(imageService.deleteBookCover).not.toHaveBeenCalled();
            expect(imageService.uploadBookCover).toHaveBeenCalledTimes(1);
        });

        it('upload avec une image Cloudinary existante : appelle deleteBookCover avant l\'upload', async () => {
            imageService.deleteBookCover.mockResolvedValue();
            imageService.uploadBookCover.mockResolvedValue(NEW_CLOUDINARY_URL);
            renderComponent({ currentImageUrl: CLOUDINARY_URL });
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => {
                uploadFile(fileInput, makeFile());
            });

            expect(imageService.deleteBookCover).toHaveBeenCalledWith(CLOUDINARY_URL);
            expect(imageService.uploadBookCover).toHaveBeenCalledTimes(1);
        });

        it('appelle onUploadSuccess avec la nouvelle URL Cloudinary', async () => {
            imageService.uploadBookCover.mockResolvedValue(NEW_CLOUDINARY_URL);
            const { onUploadSuccess } = renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => {
                uploadFile(fileInput, makeFile());
            });

            expect(onUploadSuccess).toHaveBeenCalledWith(NEW_CLOUDINARY_URL);
        });

        it('met à jour la preview avec la nouvelle URL Cloudinary après l\'upload', async () => {
            imageService.uploadBookCover.mockResolvedValue(NEW_CLOUDINARY_URL);
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => {
                uploadFile(fileInput, makeFile());
            });

            const img = screen.getByAltText(/Prévisualisation de la couverture/i);
            expect(img).toHaveAttribute('src', NEW_CLOUDINARY_URL);
        });

        it('révoque l\'URL blob locale après l\'upload (finally)', async () => {
            imageService.uploadBookCover.mockResolvedValue(NEW_CLOUDINARY_URL);
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => {
                uploadFile(fileInput, makeFile());
            });

            expect(URL.revokeObjectURL).toHaveBeenCalledTimes(1);
        });

        it('n\'affiche pas d\'erreur après un upload réussi', async () => {
            imageService.uploadBookCover.mockResolvedValue(NEW_CLOUDINARY_URL);
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => {
                uploadFile(fileInput, makeFile());
            });

            expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        });
    });

    // ── État de chargement ─────────────────────────────────────────────────────
    describe('état de chargement', () => {
        it('affiche le spinner et désactive le bouton pendant l\'upload', async () => {
            let resolveUpload;
            imageService.uploadBookCover.mockReturnValue(
                new Promise((resolve) => { resolveUpload = resolve; })
            );

            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            act(() => { uploadFile(fileInput, makeFile()); });

            expect(await screen.findByText(/Upload en cours/i)).toBeInTheDocument();
            expect(await screen.findByRole('button', { name: /Upload en cours/i })).toBeDisabled();

            await act(async () => { resolveUpload(NEW_CLOUDINARY_URL); });
        });
    });

    // ── Erreur d'upload ────────────────────────────────────────────────────────
    describe('erreur d\'upload', () => {
        it('affiche le message d\'erreur retourné par le service', async () => {
            imageService.uploadBookCover.mockRejectedValue(new Error('Connexion refusée'));
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => { uploadFile(fileInput, makeFile()); });

            expect(screen.getByRole('alert')).toHaveTextContent('Connexion refusée');
        });

        it('affiche un message générique si l\'erreur n\'a pas de message', async () => {
            imageService.uploadBookCover.mockRejectedValue({});
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => { uploadFile(fileInput, makeFile()); });

            expect(screen.getByRole('alert')).toHaveTextContent("Erreur lors de l'upload.");
        });

        it('restaure la preview initiale en cas d\'erreur (sans image initiale)', async () => {
            imageService.uploadBookCover.mockRejectedValue(new Error('Erreur réseau'));
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => { uploadFile(fileInput, makeFile()); });

            expect(screen.queryByRole('img')).not.toBeInTheDocument();
        });

        it('restaure la preview de currentImageUrl en cas d\'erreur', async () => {
            imageService.deleteBookCover.mockResolvedValue();
            imageService.uploadBookCover.mockRejectedValue(new Error('Erreur réseau'));
            renderComponent({ currentImageUrl: CLOUDINARY_URL });
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => { uploadFile(fileInput, makeFile()); });

            const img = screen.getByAltText(/Prévisualisation de la couverture/i);
            expect(img).toHaveAttribute('src', CLOUDINARY_URL);
        });

        it('révoque l\'URL blob même en cas d\'erreur (finally)', async () => {
            imageService.uploadBookCover.mockRejectedValue(new Error('fail'));
            renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => { uploadFile(fileInput, makeFile()); });

            expect(URL.revokeObjectURL).toHaveBeenCalledTimes(1);
        });

        it('n\'appelle pas onUploadSuccess en cas d\'erreur', async () => {
            imageService.uploadBookCover.mockRejectedValue(new Error('fail'));
            const { onUploadSuccess } = renderComponent();
            const fileInput = screen.getByLabelText(/Sélectionner une image de couverture/i);

            await act(async () => { uploadFile(fileInput, makeFile()); });

            expect(onUploadSuccess).not.toHaveBeenCalled();
        });
    });
});




