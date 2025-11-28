package fr.ensitech.ebooks.utils;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHistoryTokenizerTest {

    @Test
    void testTokenizeEmptyString() {
        List<String> result = PasswordHistoryTokenizer.tokenize("");
        assertTrue(result.isEmpty(), "La liste devrait être vide pour une chaîne vide");
    }

    @Test
    void testTokenizeNull() {
        List<String> result = PasswordHistoryTokenizer.tokenize(null);
        assertTrue(result.isEmpty(), "La liste devrait être vide pour null");
    }

    @Test
    void testTokenizeSinglePassword() {
        String history = "$2a$10$abcdefghijklmnopqrstuvwxyz";
        List<String> result = PasswordHistoryTokenizer.tokenize(history);
        assertEquals(1, result.size(), "Devrait contenir 1 mot de passe");
        assertEquals(history, result.get(0));
    }

    @Test
    void testTokenizeMultiplePasswords() {
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String hash3 = "$2a$10$hash3";
        String history = hash1 + " " + hash2 + " " + hash3;

        List<String> result = PasswordHistoryTokenizer.tokenize(history);

        assertEquals(3, result.size(), "Devrait contenir 3 mots de passe");
        assertEquals(hash1, result.get(0));
        assertEquals(hash2, result.get(1));
        assertEquals(hash3, result.get(2));
    }

    @Test
    void testDetokenizeEmptyList() {
        String result = PasswordHistoryTokenizer.detokenize(Arrays.asList());
        assertEquals("", result, "Devrait retourner une chaîne vide");
    }

    @Test
    void testDetokenizeNull() {
        String result = PasswordHistoryTokenizer.detokenize(null);
        assertEquals("", result, "Devrait retourner une chaîne vide pour null");
    }

    @Test
    void testDetokenizeSinglePassword() {
        String hash = "$2a$10$abcdefghijklmnopqrstuvwxyz";
        String result = PasswordHistoryTokenizer.detokenize(Arrays.asList(hash));
        assertEquals(hash, result);
    }

    @Test
    void testDetokenizeMultiplePasswords() {
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String hash3 = "$2a$10$hash3";

        String result = PasswordHistoryTokenizer.detokenize(Arrays.asList(hash1, hash2, hash3));

        assertEquals(hash1 + " " + hash2 + " " + hash3, result);
    }

    @Test
    void testAddPasswordToHistoryEmpty() {
        String newHash = "$2a$10$newHash";
        String result = PasswordHistoryTokenizer.addPasswordToHistory("", newHash);

        assertEquals(newHash, result, "Devrait contenir uniquement le nouveau mot de passe");
    }

    @Test
    void testAddPasswordToHistoryWithExisting() {
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String currentHistory = hash1 + " " + hash2;
        String newHash = "$2a$10$newHash";

        String result = PasswordHistoryTokenizer.addPasswordToHistory(currentHistory, newHash);

        List<String> passwords = PasswordHistoryTokenizer.tokenize(result);
        assertEquals(3, passwords.size(), "Devrait contenir 3 mots de passe");
        assertEquals(newHash, passwords.get(0), "Le nouveau mot de passe devrait être en premier");
        assertEquals(hash1, passwords.get(1));
        assertEquals(hash2, passwords.get(2));
    }

    @Test
    void testAddPasswordToHistoryMaxFive() {
        // Créer un historique avec 5 mots de passe
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String hash3 = "$2a$10$hash3";
        String hash4 = "$2a$10$hash4";
        String hash5 = "$2a$10$hash5";
        String currentHistory = hash1 + " " + hash2 + " " + hash3 + " " + hash4 + " " + hash5;
        String newHash = "$2a$10$newHash";

        String result = PasswordHistoryTokenizer.addPasswordToHistory(currentHistory, newHash);

        List<String> passwords = PasswordHistoryTokenizer.tokenize(result);
        assertEquals(5, passwords.size(), "Devrait contenir exactement 5 mots de passe");
        assertEquals(newHash, passwords.get(0), "Le nouveau mot de passe devrait être en premier");
        assertEquals(hash1, passwords.get(1));
        assertEquals(hash2, passwords.get(2));
        assertEquals(hash3, passwords.get(3));
        assertEquals(hash4, passwords.get(4));
        assertFalse(passwords.contains(hash5), "Le plus ancien mot de passe devrait avoir été supprimé");
    }

    @Test
    void testTokenizeDetokenizeRoundTrip() {
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String hash3 = "$2a$10$hash3";
        String original = hash1 + " " + hash2 + " " + hash3;

        List<String> tokenized = PasswordHistoryTokenizer.tokenize(original);
        String detokenized = PasswordHistoryTokenizer.detokenize(tokenized);

        assertEquals(original, detokenized, "Devrait obtenir la même chaîne après tokenize/detokenize");
    }

    @Test
    void testIsPasswordInHistoryTrue() {
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String hash3 = "$2a$10$hash3";
        String history = hash1 + " " + hash2 + " " + hash3;

        assertTrue(PasswordHistoryTokenizer.isPasswordInHistory(history, hash2),
                "Devrait trouver le mot de passe dans l'historique");
    }

    @Test
    void testIsPasswordInHistoryFalse() {
        String hash1 = "$2a$10$hash1";
        String hash2 = "$2a$10$hash2";
        String hash3 = "$2a$10$hash3";
        String history = hash1 + " " + hash2;

        assertFalse(PasswordHistoryTokenizer.isPasswordInHistory(history, hash3),
                "Ne devrait pas trouver le mot de passe dans l'historique");
    }

    @Test
    void testGetMaxPasswordHistory() {
        assertEquals(5, PasswordHistoryTokenizer.getMaxPasswordHistory(),
                "Le maximum de mots de passe devrait être 5");
    }
}

