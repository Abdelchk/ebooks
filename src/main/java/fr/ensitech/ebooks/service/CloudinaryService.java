package fr.ensitech.ebooks.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key",    apiKey,
                "api_secret", apiSecret,
                "secure",     true
        ));
    }

    /**
     * Uploade une image de couverture de livre vers Cloudinary.
     *
     * @param file Le fichier image envoyé par le frontend
     * @return L'URL HTTPS de l'image hébergée sur Cloudinary
     * @throws IOException En cas d'erreur lors de l'upload
     */
    public String uploadBookCover(MultipartFile file) throws IOException {
        String publicId = "ebooks/covers/" + UUID.randomUUID();

        @SuppressWarnings("unchecked")
        Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "public_id",      publicId,
                        "overwrite",      true,
                        // Transformation automatique : recadrage centré 400x600 (format livre)
                        "transformation", "c_fill,w_400,h_600,q_auto,f_auto",
                        "folder",         "ebooks/covers"
                )
        );

        return (String) result.get("secure_url");
    }

    /**
     * Supprime une image de Cloudinary à partir de son URL.
     * Utile lors de la mise à jour d'une couverture existante.
     *
     * @param imageUrl L'URL Cloudinary de l'image à supprimer
     */
    public void deleteBookCover(String imageUrl) throws IOException {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return; // Ne pas tenter de supprimer une URL externe non-Cloudinary
        }
        // Extraire le public_id de l'URL Cloudinary
        // Format : https://res.cloudinary.com/{cloud}/image/upload/v{version}/{public_id}.{ext}
        String[] parts = imageUrl.split("/upload/");
        if (parts.length < 2) {
            return;
        }
        // Supprimer la partie vXXXXXXX/ si présente, puis l'extension
        String publicIdWithExt = parts[1].replaceFirst("v\\d+/", "");
        String publicId = publicIdWithExt.replaceAll("\\.[^.]+$", "");

        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}


