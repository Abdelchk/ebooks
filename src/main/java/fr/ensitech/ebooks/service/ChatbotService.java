package fr.ensitech.ebooks.service;

import fr.ensitech.ebooks.dto.BookDto;
import fr.ensitech.ebooks.dto.ChatResponse;
import fr.ensitech.ebooks.entity.Book;
import fr.ensitech.ebooks.repository.IBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Profile("!test")
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatClient.Builder chatClientBuilder;
    private final IBookRepository bookRepository;

    public ChatResponse chat(String userMessage) {

        ChatClient chatClient = chatClientBuilder.build();

        // ── 1. Étape d'extraction : le LLM identifie ce que cherche l'utilisateur
        // Peu importe comment la question est posée, le LLM extrait le titre ou l'auteur
        String extractedQuery = chatClient.prompt()
                .system("""
                        Tu es un extracteur de mots-clés pour une bibliothèque.
                        Extrais UNIQUEMENT le titre du livre ou le nom de l'auteur mentionné dans le message.
                        Réponds avec SEULEMENT les mots-clés pertinents (1 à 5 mots max), sans ponctuation ni explication.
                        Si la question ne concerne pas un livre précis (ex: règles d'emprunt), réponds: GENERAL
                        
                        Exemples :
                        - "je cherche un livre de Victor Hugo" → Victor Hugo
                        - "avez-vous Les Misérables ?" → Les Misérables
                        - "quelque chose de Camus" → Camus
                        - "livres de fantasy" → fantasy
                        - "comment prolonger mon emprunt ?" → GENERAL
                        """)
                .user(userMessage)
                .call()
                .content();

        // ── 2. RAG : recherche en BDD avec les mots-clés extraits par le LLM ──
        List<Book> foundBooks;
        if (extractedQuery == null || extractedQuery.isBlank() || extractedQuery.equalsIgnoreCase("GENERAL")) {
            foundBooks = List.of(); // Pas de recherche si question générale
        } else {
            // Recherche sur chaque mot extrait, résultats fusionnés sans doublons
            foundBooks = Arrays.stream(extractedQuery.split("[\\s,;.!?']+"))
                    .filter(word -> !word.isBlank())
                    .flatMap(word -> bookRepository
                            .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(word, word)
                            .stream())
                    .distinct()
                    .toList();
        }

        // ── 2. Construire le contexte à injecter dans le prompt ─────────────
        String bookContext = foundBooks.isEmpty()
                ? "Aucun livre trouvé dans le catalogue pour cette recherche."
                : foundBooks.stream()
                  .map(b -> "- '%s' de %s (catégorie: %s, disponible: %s)"
                            .formatted(
                                    b.getTitle(),
                                    b.getAuthor(),
                                    b.getCategory(),
                                    b.getQuantity() > 0 ? "oui (" + b.getQuantity() + " exemplaires)" : "non (stock épuisé)"
                            ))
                  .collect(java.util.stream.Collectors.joining("\n"));

        // ── 3. Prompt système : donne le rôle + le contexte BDD au LLM ──────
        String systemPrompt = """
                Tu es l'assistant virtuel de la bibliothèque en ligne EBooks.
                Tu aides les utilisateurs à trouver des livres, comprendre les règles
                d'emprunt (durée max 30 jours, 2 prolongations max de 7 jours chacune),
                et naviguer dans le catalogue.
                Réponds toujours en français, de façon concise et amicale.
                
                RÈGLES IMPORTANTES :
                - Ne mentionne JAMAIS un livre qui n'est pas dans la liste ci-dessous.
                - Ne suggère JAMAIS de titres inventés ou issus de tes connaissances générales.
                - Si aucun livre n'est trouvé, dis-le clairement et propose à l'utilisateur
                  d'affiner sa recherche ou de contacter un bibliothécaire.
                - Si un livre est disponible (quantité > 0), encourage l'emprunt.
                - Si le stock est épuisé, propose une réservation.
                
                Voici les livres du catalogue correspondant à la demande de l'utilisateur :
                """ + bookContext;

        // ── 4. Appel final au LLM avec le contexte BDD ──────────────────────
        String reply = chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();

        // ── 5. Mapper les livres trouvés en DTO pour le frontend ─────────────
        List<BookDto> bookDtos = foundBooks.stream()
                .map(b -> new BookDto(
                        b.getId(),
                        b.getTitle(),
                        b.getAuthor(),
                        b.getCategory(),
                        b.getQuantity()))
                .toList();

        return new ChatResponse(reply, bookDtos);
    }
}