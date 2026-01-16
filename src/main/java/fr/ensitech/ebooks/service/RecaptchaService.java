package fr.ensitech.ebooks.service;

import com.google.cloud.recaptchaenterprise.v1.RecaptchaEnterpriseServiceClient;
import com.google.recaptchaenterprise.v1.Assessment;
import com.google.recaptchaenterprise.v1.CreateAssessmentRequest;
import com.google.recaptchaenterprise.v1.Event;
import com.google.recaptchaenterprise.v1.ProjectName;
import com.google.recaptchaenterprise.v1.RiskAnalysis.ClassificationReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.io.IOException;

/**
 * Service pour la validation reCAPTCHA Enterprise de Google
 */
@Service
public class RecaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(RecaptchaService.class);

    @Value("${recaptcha.project.id}")
    private String projectId;

    @Value("${recaptcha.site.key}")
    private String siteKey;

    @Value("${recaptcha.score.threshold:0.5}")
    private float scoreThreshold;

    /**
     * Méthode appelée après l'injection des propriétés pour valider la configuration
     */
    @PostConstruct
    public void init() {
        logger.info("=== Configuration reCAPTCHA ===");
        logger.info("Project ID: {}", projectId);
        logger.info("Site Key: {}", siteKey);
        logger.info("Score Threshold: {}", scoreThreshold);
        
        if (projectId == null || projectId.isEmpty()) {
            logger.error("❌ ERREUR: recaptcha.project.id n'est pas configuré !");
        }
        if (siteKey == null || siteKey.isEmpty()) {
            logger.error("❌ ERREUR: recaptcha.site.key n'est pas configuré !");
        }
        logger.info("=== Fin Configuration reCAPTCHA ===");
    }

    /**
     * Vérifie le token reCAPTCHA et retourne true si valide
     *
     * @param token Le token généré côté client
     * @param expectedAction L'action attendue (ex: "REGISTER")
     * @return true si la validation réussit, false sinon
     */
    public boolean verifyToken(String token, String expectedAction) {
        try (RecaptchaEnterpriseServiceClient client = RecaptchaEnterpriseServiceClient.create()) {

            // Définir les propriétés de l'événement
            Event event = Event.newBuilder()
                    .setSiteKey(siteKey)
                    .setToken(token)
                    .build();

            // Créer la demande d'évaluation
            CreateAssessmentRequest createAssessmentRequest = CreateAssessmentRequest.newBuilder()
                    .setParent(ProjectName.of(projectId).toString())
                    .setAssessment(Assessment.newBuilder().setEvent(event).build())
                    .build();

            Assessment response = client.createAssessment(createAssessmentRequest);

            // Vérifier si le token est valide
            if (!response.getTokenProperties().getValid()) {
                logger.warn("Token reCAPTCHA invalide. Raison: {}",
                           response.getTokenProperties().getInvalidReason().name());
                return false;
            }

            // Vérifier si l'action attendue correspond
            if (!response.getTokenProperties().getAction().equals(expectedAction)) {
                logger.warn("Action reCAPTCHA non conforme. Attendu: {}, Reçu: {}",
                           expectedAction,
                           response.getTokenProperties().getAction());
                return false;
            }

            // Obtenir le score de risque
            float recaptchaScore = response.getRiskAnalysis().getScore();
            logger.info("Score reCAPTCHA: {}", recaptchaScore);

            // Log des raisons de classification
            for (ClassificationReason reason : response.getRiskAnalysis().getReasonsList()) {
                logger.info("Raison de classification: {}", reason);
            }

            // Vérifier si le score est au-dessus du seuil
            if (recaptchaScore < scoreThreshold) {
                logger.warn("Score reCAPTCHA trop bas: {} (seuil: {})", recaptchaScore, scoreThreshold);
                return false;
            }

            logger.info("Validation reCAPTCHA réussie. Score: {}, Assessment: {}",
                       recaptchaScore,
                       response.getName().substring(response.getName().lastIndexOf("/") + 1));

            return true;

        } catch (IOException e) {
            logger.error("Erreur lors de la validation reCAPTCHA", e);
            return false;
        }
    }

    /**
     * Obtient la clé de site pour l'intégration côté client
     */
    public String getSiteKey() {
        logger.debug("getSiteKey() appelé - Retour: {}", siteKey);
        if (siteKey == null || siteKey.isEmpty()) {
            logger.error("❌ ERREUR CRITIQUE: La clé reCAPTCHA est NULL ou vide !");
        }
        return siteKey;
    }
}

