package org.example.escapehubbackend.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static org.example.escapehubbackend.external.ExternalApiTestSupport.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ExternalHttpClientTest {

    private static final URI ANY_URI = URI.create("https://example.org/test.json");

    private HttpClient mockedClient;
    private ExternalHttpClient http;

    @BeforeEach
    void setUp() {
        mockedClient = mock(HttpClient.class);
        http = httpClientWith(mockedClient);
    }

    @Test
    void sendsUserAgentAndTimeout() throws Exception {
        respond(mockedClient, 200, "{}");

        http.get(ANY_URI);

        // On "capture" la requête envoyée au faux client pour l'inspecter
        ArgumentCaptor<HttpRequest> request = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockedClient).send(request.capture(), any());
        assertEquals(TEST_USER_AGENT, request.getValue().headers().firstValue("User-Agent").orElse(null));
        assertEquals(TEST_TIMEOUT, request.getValue().timeout().orElse(null));
    }

    @Test
    void returnsBodyOnSuccess() throws Exception {
        respond(mockedClient, 200, "{\"ok\":true}");

        assertEquals("{\"ok\":true}", http.get(ANY_URI));
    }

    @Test
    void mapsTimeout() throws Exception {
        timeout(mockedClient);

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> http.get(ANY_URI));
        assertEquals(ExternalApiException.Reason.TIMEOUT, e.getReason());
    }

    @Test
    void mapsNetworkError() throws Exception {
        doThrow(new IOException("connection refused")).when(mockedClient).send(any(HttpRequest.class), any());

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> http.get(ANY_URI));
        assertEquals(ExternalApiException.Reason.UNAVAILABLE, e.getReason());
    }

    @Test
    void mapsNotFound() throws Exception {
        respond(mockedClient, 404, "");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> http.get(ANY_URI));
        assertEquals(ExternalApiException.Reason.NOT_FOUND, e.getReason());
    }

    @Test
    void mapsRateLimit() throws Exception {
        respond(mockedClient, 429, "");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> http.get(ANY_URI));
        assertEquals(ExternalApiException.Reason.RATE_LIMITED, e.getReason());
    }

    @Test
    void mapsServerError() throws Exception {
        respond(mockedClient, 503, "Service Unavailable");

        ExternalApiException e = assertThrows(ExternalApiException.class, () -> http.get(ANY_URI));
        assertEquals(ExternalApiException.Reason.HTTP_ERROR, e.getReason());
    }
}
