package fr.ensitech.ebooks.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe utilitaire pour gérer l'historique des mots de passe.
 * Transforme une chaîne de mots de passe séparés par des espaces en liste et vice-versa.
 */
public class PasswordHistoryTokenizer {

    private static final int MAX_PASSWORD_HISTORY = 5;
    private static final String DELIMITER = " ";

    /**
     * Convertit une chaîne d'historique en liste de mots de passe
     * @param passwordHistory chaîne contenant les mots de passe séparés par des espaces
     * @return liste des mots de passe
     */
    public static List<String> tokenize(String passwordHistory) {
        if (passwordHistory == null || passwordHistory.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(passwordHistory.split(DELIMITER))
                .filter(s -> !s.trim().isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Convertit une liste de mots de passe en chaîne d'historique
     * @param passwords liste des mots de passe
     * @return chaîne contenant les mots de passe séparés par des espaces
     */
    public static String detokenize(List<String> passwords) {
        if (passwords == null || passwords.isEmpty()) {
            return "";
        }
        return String.join(DELIMITER, passwords);
    }

    /**
     * Ajoute un nouveau mot de passe à l'historique et maintient uniquement les 5 derniers
     * @param currentHistory historique actuel
     * @param newHashedPassword nouveau mot de passe hashé à ajouter
     * @return nouvel historique avec le mot de passe ajouté
     */
    public static String addPasswordToHistory(String currentHistory, String newHashedPassword) {
        List<String> passwords = tokenize(currentHistory);

        // Ajouter le nouveau mot de passe au début de la liste
        passwords.add(0, newHashedPassword);

        // Garder seulement les 5 derniers mots de passe
        if (passwords.size() > MAX_PASSWORD_HISTORY) {
            passwords = passwords.subList(0, MAX_PASSWORD_HISTORY);
        }

        return detokenize(passwords);
    }

    /**
     * Vérifie si un mot de passe existe dans l'historique
     * @param passwordHistory historique des mots de passe
     * @param hashedPassword mot de passe hashé à vérifier
     * @return true si le mot de passe existe dans l'historique
     */
    public static boolean isPasswordInHistory(String passwordHistory, String hashedPassword) {
        List<String> passwords = tokenize(passwordHistory);
        return passwords.contains(hashedPassword);
    }

    /**
     * Obtient le nombre maximum de mots de passe à conserver
     * @return nombre maximum de mots de passe
     */
    public static int getMaxPasswordHistory() {
        return MAX_PASSWORD_HISTORY;
    }
}

