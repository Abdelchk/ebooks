import api from './api';

/**
 * Service de gestion des images de couverture de livres via Cloudinary.
 */
export const imageService = {

    /**
     * Uploade une image de couverture vers le backend,
     * qui la transfère ensuite vers Cloudinary.
     *
     * @param {File} file - Le fichier image sélectionné par l'utilisateur
     * @returns {Promise<string>} L'URL Cloudinary de l'image uploadée
     * @throws {Error} Si le fichier est invalide ou si l'upload échoue
     */
    uploadBookCover: async (file) => {
        // Validation côté client avant l'envoi
        const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
        if (!allowedTypes.includes(file.type)) {
            throw new Error('Format non supporté. Utilisez JPEG, PNG, WebP ou GIF.');
        }

        const maxSize = 5 * 1024 * 1024; // 5 Mo
        if (file.size > maxSize) {
            throw new Error('Le fichier dépasse 5 Mo. Veuillez choisir une image plus légère.');
        }

        const formData = new FormData();
        formData.append('file', file);

        const response = await api.post('/api/rest/images/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });

        return response.data.url;
    },

    /**
     * Supprime une image de Cloudinary.
     * Appeler cette méthode avant de remplacer une image existante.
     *
     * @param {string} imageUrl - L'URL Cloudinary à supprimer
     */
    deleteBookCover: async (imageUrl) => {
        if (!imageUrl || !imageUrl.includes('cloudinary.com')) {
            return; // Ne pas tenter de supprimer une URL externe
        }
        await api.delete('/api/rest/images/delete', {
            params: { url: imageUrl },
        });
    },
};

