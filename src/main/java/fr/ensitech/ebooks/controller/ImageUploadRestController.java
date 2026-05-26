package fr.ensitech.ebooks.controller;

import fr.ensitech.ebooks.service.CloudinaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/rest/images")
public class ImageUploadRestController {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5 Mo
    private static final String ERROR_KEY = "error";
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    private final CloudinaryService cloudinaryService;

    public ImageUploadRestController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload d'une image de couverture de livre vers Cloudinary.
     * Accessible uniquement aux LIBRARIAN et ADMIN.
     * POST /api/rest/images/upload (multipart/form-data)
     *
     * @param file Le fichier image à uploader
     * @return La réponse contenant l'URL de l'image uploadée
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Map<String, String>> uploadBookCover(
            @RequestPart("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Le fichier est vide ou manquant."));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Le fichier dépasse la taille maximale autorisée (5 Mo)."));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "Type de fichier non autorisé. Formats acceptés : JPEG, PNG, WebP, GIF."));
        }

        try {
            String url = cloudinaryService.uploadBookCover(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, "Erreur lors de l'upload : " + e.getMessage()));
        }
    }

    /**
     * Suppression d'une image de couverture depuis Cloudinary.
     * Accessible uniquement aux LIBRARIAN et ADMIN.
     * DELETE /api/rest/images/delete?url={imageUrl}
     *
     * @param imageUrl L'URL Cloudinary de l'image à supprimer
     * @return Un message de confirmation
     */
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('LIBRARIAN', 'ADMIN')")
    public ResponseEntity<Map<String, String>> deleteBookCover(
            @RequestParam("url") String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(ERROR_KEY, "L'URL de l'image est requise."));
        }

        try {
            cloudinaryService.deleteBookCover(imageUrl);
            return ResponseEntity.ok(Map.of("message", "Image supprimée avec succès."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(ERROR_KEY, "Erreur lors de la suppression : " + e.getMessage()));
        }
    }
}

