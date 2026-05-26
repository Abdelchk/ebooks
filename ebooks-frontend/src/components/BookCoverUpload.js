import React, { useState, useRef } from 'react';
import PropTypes from 'prop-types';
import { imageService } from '../services/imageService';

/**
 * Composant réutilisable pour l'upload d'une image de couverture de livre.
 * Utilisé dans le dashboard bibliothécaire et admin lors de la création/modification d'un livre.
 *
 * Props :
 *   - currentImageUrl : URL actuelle de la couverture (si c'est une modification)
 *   - onUploadSuccess : callback(newUrl) appelé après un upload réussi
 */
const BookCoverUpload = ({ currentImageUrl, onUploadSuccess }) => {
    const [preview, setPreview] = useState(currentImageUrl || null);
    const [loading, setLoading] = useState(false);
    const [error, setError]     = useState(null);
    const fileInputRef          = useRef(null);

    const handleFileChange = async (e) => {
        const file = e.target.files?.[0];
        if (!file) return;

        const localPreview = URL.createObjectURL(file);
        setPreview(localPreview);
        setError(null);
        setLoading(true);

        try {
            if (currentImageUrl?.includes('cloudinary.com')) {
                await imageService.deleteBookCover(currentImageUrl);
            }
            const cloudinaryUrl = await imageService.uploadBookCover(file);
            setPreview(cloudinaryUrl);
            onUploadSuccess(cloudinaryUrl);
        } catch (err) {
            setError(err.message || "Erreur lors de l'upload.");
            setPreview(currentImageUrl || null);
        } finally {
            setLoading(false);
            URL.revokeObjectURL(localPreview);
        }
    };

    return (
        <div className="book-cover-upload">
            <button
                type="button"
                className="cover-preview mb-2 p-0 border-0 bg-transparent"
                style={{
                    width: '150px', height: '225px',
                    border: '2px dashed #ccc', borderRadius: '8px',
                    overflow: 'hidden', display: 'flex',
                    alignItems: 'center', justifyContent: 'center',
                    background: '#f8f9fa', cursor: 'pointer',
                }}
                onClick={() => fileInputRef.current?.click()}
                aria-label="Cliquer pour choisir une image de couverture"
            >
                {preview ? (
                    <img
                        src={preview}
                        alt="Prévisualisation de la couverture"
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                ) : (
                    <div className="text-center text-muted small">
                        <i className="bi bi-image fs-3 d-block mb-1" />
                        Cliquer pour<br />ajouter une image
                    </div>
                )}
            </button>

            <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                style={{ display: 'none' }}
                onChange={handleFileChange}
                aria-label="Sélectionner une image de couverture"
            />

            <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={() => fileInputRef.current?.click()}
                disabled={loading}
            >
                {loading ? (
                    <>
                        <output className="spinner-border spinner-border-sm me-1" aria-hidden="true" />
                        {'Upload en cours...'}
                    </>
                ) : (
                    <>
                        <i className="bi bi-upload me-1" />
                        {preview ? 'Changer la couverture' : 'Ajouter une couverture'}
                    </>
                )}
            </button>

            <div className="text-muted small mt-1">
                Formats acceptés : JPEG, PNG, WebP — Max 5 Mo
            </div>

            {error && (
                <div className="alert alert-danger py-1 px-2 mt-2 small" role="alert">
                    {error}
                </div>
            )}
        </div>
    );
};

BookCoverUpload.propTypes = {
    currentImageUrl: PropTypes.string,
    onUploadSuccess: PropTypes.func.isRequired,
};

BookCoverUpload.defaultProps = {
    currentImageUrl: null,
};

export default BookCoverUpload;

