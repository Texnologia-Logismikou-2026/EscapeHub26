package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.wikidata.PersonInfo;
import org.example.escapehubbackend.external.wikidata.WikidataClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.util.List;

import static org.example.escapehubbackend.external.ExternalApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Les 4 cas exigés (succès, échec, timeout, JSON invalide) et des cas limites.
 * Le JSON ci-dessous est une version réduite du vrai format Special:EntityData ;
 * les identifiants des relations sont des valeurs de test.
 */
class WikidataClientTest {

    private static final String ENTITY_JSON = """
            {
              "entities": {
                "Q1": {
                  "id": "Q1",
                  "labels": {
                    "en": { "language": "en", "value": "Test Person" },
                    "fr": { "language": "fr", "value": "Personne Test" }
                  },
                  "claims": {
                    "P22": [ { "mainsnak": { "snaktype": "value",
                               "datavalue": { "type": "wikibase-entityid",
                                              "value": { "entity-type": "item", "id": "Q10" } } } } ],
                    "P25": [ { "mainsnak": { "snaktype": "value",
                               "datavalue": { "type": "wikibase-entityid",
                                              "value": { "entity-type": "item", "id": "Q20" } } } } ],
                    "P26": [ { "mainsnak": { "snaktype": "somevalue" } } ]
                  }
                }
              }
            }
            """;

    private HttpClient mockedClient;
    private WikidataClient client;

    @BeforeEach
    void setUp() {
        mockedClient = mock(HttpClient.class);
        client = new WikidataClient(httpClientWith(mockedClient), noSnapshots());
    }

    // --- 1. Succès ---

    @Test
    void getPerson_success() throws Exception {
        respond(mockedClient, 200, ENTITY_JSON);

        PersonInfo person = client.getPerson("Q1");

        assertEquals("Q1", person.wikidataId());
        assertEquals("Test Person", person.name());
        assertEquals(List.of("Q10"), person.fatherIds());
        assertEquals(List.of("Q20"), person.motherIds());
        assertTrue(person.spouseIds().isEmpty(), "une 'valeur inconnue' n'a pas d'id et doit être ignorée");
    }

    // --- 2. Échec ---

    @Test
    void getPerson_serverError() throws Exception {
        respond(mockedClient, 503, "Service Unavailable");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getPerson("Q1"));
        assertEquals(ExternalApiException.Reason.HTTP_ERROR, e.getReason());
    }

    @Test
    void getPerson_rateLimited() throws Exception {
        respond(mockedClient, 429, "");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getPerson("Q1"));
        assertEquals(ExternalApiException.Reason.RATE_LIMITED, e.getReason());
    }

    // --- 3. Timeout ---

    @Test
    void getPerson_timeout() throws Exception {
        timeout(mockedClient);

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getPerson("Q1"));
        assertEquals(ExternalApiException.Reason.TIMEOUT, e.getReason());
    }

    // --- 4. JSON invalide ---

    @Test
    void getPerson_invalidJson() throws Exception {
        respond(mockedClient, 200, "{ this is not json");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getPerson("Q1"));
        assertEquals(ExternalApiException.Reason.INVALID_RESPONSE, e.getReason());
    }

    @Test
    void getPerson_noEntity() throws Exception {
        respond(mockedClient, 200, "{\"entities\": {}}");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getPerson("Q1"));
        assertEquals(ExternalApiException.Reason.INVALID_RESPONSE, e.getReason());
    }

    // --- Autres cas ---

    @Test
    void getPerson_fallsBackToAnotherLabel() throws Exception {
        respond(mockedClient, 200, """
                { "entities": { "Q2": { "id": "Q2",
                  "labels": { "el": { "language": "el", "value": "Δοκιμή" } } } } }
                """);

        assertEquals("Δοκιμή", client.getPerson("Q2").name());
    }

    @Test
    void getPerson_rejectsInvalidId_withoutCallingApi() {
        ExternalApiException e = assertThrows(ExternalApiException.class, () -> client.getPerson("Mary Shelley"));
        assertEquals(ExternalApiException.Reason.INVALID_INPUT, e.getReason());
        verifyNoInteractions(mockedClient);
    }
}
