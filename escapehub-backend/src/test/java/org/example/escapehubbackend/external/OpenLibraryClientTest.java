package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.openlibrary.AuthorInfo;
import org.example.escapehubbackend.external.openlibrary.BookInfo;
import org.example.escapehubbackend.external.openlibrary.OpenLibraryClient;
import org.example.escapehubbackend.external.snapshot.SnapshotStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;

import static org.example.escapehubbackend.external.ExternalApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Les 4 cas exigés (succès, échec, timeout, JSON invalide),
 * plus "aucun résultat", la validation des identifiants et l'utilisation des snapshots.
 */
class OpenLibraryClientTest {

    private static final String SEARCH_JSON = """
            {
              "numFound": 471,
              "docs": [
                {
                  "author_key": ["OL25342A"],
                  "author_name": ["Mary Shelley"],
                  "first_publish_year": 1818,
                  "key": "/works/OL450063W",
                  "title": "Frankenstein; or, The Modern Prometheus"
                }
              ]
            }
            """;

    private static final String AUTHOR_JSON = """
            {
              "name": "Mary Shelley",
              "birth_date": "30 August 1797",
              "death_date": "1 February 1851",
              "remote_ids": { "wikidata": "Q47152", "viaf": "6293" }
            }
            """;

    private HttpClient mockedClient;
    private OpenLibraryClient client;

    @BeforeEach
    void setUp() {
        mockedClient = mock(HttpClient.class);
        client = new OpenLibraryClient(httpClientWith(mockedClient), noSnapshots());
    }

    // --- 1. Succès ---

    @Test
    void searchBook_success() throws Exception {
        respond(mockedClient, 200, SEARCH_JSON);

        BookInfo book = client.searchBook("The Modern Prometheus").orElseThrow();

        assertEquals("Frankenstein; or, The Modern Prometheus", book.title());
        assertEquals(List.of("Mary Shelley"), book.authorNames());
        assertEquals(Optional.of("OL25342A"), book.firstAuthorKey());
        assertEquals(1818, book.firstPublishYear());
        assertEquals("/works/OL450063W", book.workKey());
    }

    @Test
    void getAuthor_success_returnsWikidataBridge() throws Exception {
        respond(mockedClient, 200, AUTHOR_JSON);

        AuthorInfo author = client.getAuthor("OL25342A");

        assertEquals("Mary Shelley", author.name());
        assertEquals(Optional.of("Q47152"), author.wikidata());
    }

    // --- 2. Échec ---

    @Test
    void searchBook_serverError() throws Exception {
        respond(mockedClient, 500, "Internal Server Error");

        ExternalApiException e = assertThrows(ExternalApiException.class,
                () -> client.searchBook("The Modern Prometheus"));
        assertEquals(ExternalApiException.Reason.HTTP_ERROR, e.getReason());
    }

    @Test
    void getAuthor_notFound() throws Exception {
        respond(mockedClient, 404, "");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getAuthor("OL999999999A"));
        assertEquals(ExternalApiException.Reason.NOT_FOUND, e.getReason());
    }

    // --- 3. Timeout ---

    @Test
    void searchBook_timeout() throws Exception {
        timeout(mockedClient);

        ExternalApiException e = assertThrows(ExternalApiException.class,
                () -> client.searchBook("The Modern Prometheus"));
        assertEquals(ExternalApiException.Reason.TIMEOUT, e.getReason());
    }

    // --- 4. JSON invalide ---

    @Test
    void searchBook_invalidJson() throws Exception {
        respond(mockedClient, 200, "<html>Not JSON</html>");

        ExternalApiException e = assertThrows(ExternalApiException.class,
                () -> client.searchBook("The Modern Prometheus"));
        assertEquals(ExternalApiException.Reason.INVALID_RESPONSE, e.getReason());
    }

    @Test
    void searchBook_wrongJsonShape() throws Exception {
        respond(mockedClient, 200, "{\"docs\": \"this should be a list\"}");

        ExternalApiException e = assertThrows(ExternalApiException.class,
                () -> client.searchBook("The Modern Prometheus"));
        assertEquals(ExternalApiException.Reason.INVALID_RESPONSE, e.getReason());
    }

    // --- Autres cas ---

    @Test
    void searchBook_noResult() throws Exception {
        respond(mockedClient, 200, "{\"numFound\": 0, \"docs\": []}");

        assertTrue(client.searchBook("zzzz unknown book zzzz").isEmpty());
    }

    @Test
    void getAuthor_rejectsInvalidId_withoutCallingApi() {
        ExternalApiException e = assertThrows(ExternalApiException.class,
                () -> client.getAuthor("../../admin"));
        assertEquals(ExternalApiException.Reason.INVALID_INPUT, e.getReason());
        verifyNoInteractions(mockedClient);   // internet n'a jamais été appelé
    }

    @Test
    void usesSnapshot_withoutCallingApi() {
        OpenLibraryClient withSnapshots =
                new OpenLibraryClient(httpClientWith(mockedClient), new SnapshotStore(SnapshotStore.DEFAULT_BASE_PATH));

        BookInfo book = withSnapshots.searchBook("The Modern Prometheus").orElseThrow();
        AuthorInfo author = withSnapshots.getAuthor("OL25342A");

        assertEquals("Mary Shelley", book.authorNames().get(0));
        assertEquals(Optional.of("Q47152"), author.wikidata());
        verifyNoInteractions(mockedClient);   // les snapshots suffisent : aucun appel réseau
    }
}
