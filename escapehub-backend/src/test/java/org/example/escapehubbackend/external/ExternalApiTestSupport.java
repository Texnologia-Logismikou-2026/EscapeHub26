package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.snapshot.SnapshotStore;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Outils partagés par les tests des APIs externes.
 * Le vrai HttpClient est remplacé par un mock Mockito : aucun test n'appelle internet.
 */
public final class ExternalApiTestSupport {

    public static final String TEST_USER_AGENT = "EscapeHub-Test/1.0 (test@example.org)";
    public static final Duration TEST_TIMEOUT = Duration.ofSeconds(5);

    private ExternalApiTestSupport() {
    }

    /** Un SnapshotStore qui pointe vers un dossier inexistant : toujours vide, donc les clients utilisent HTTP. */
    public static SnapshotStore noSnapshots() {
        return new SnapshotStore("no-snapshots-in-tests");
    }

    public static ExternalHttpClient httpClientWith(HttpClient mockedClient) {
        return new ExternalHttpClient(mockedClient, TEST_USER_AGENT, TEST_TIMEOUT);
    }

    /** Le faux HttpClient répondra avec ce code HTTP et ce corps. */
    @SuppressWarnings("unchecked")
    public static void respond(HttpClient mockedClient, int status, String body) throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);
        doReturn(response).when(mockedClient).send(any(HttpRequest.class), any());
    }

    /** Le faux HttpClient se comportera comme une API qui ne répond jamais à temps. */
    public static void timeout(HttpClient mockedClient) throws Exception {
        doThrow(new HttpTimeoutException("request timed out"))
                .when(mockedClient).send(any(HttpRequest.class), any());
    }
}
