package org.example.escapehubbackend.external;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

/**
 * Le "robot messager" partagé par les clients Open Library et Wikidata.
 * - envoie toujours un User-Agent (obligatoire chez Wikimedia),
 * - applique un timeout,
 * - transforme chaque échec en ExternalApiException.
 * Le HttpClient est reçu de l'extérieur : dans les tests, on le remplace par un mock.
 */
public class ExternalHttpClient {

    private final HttpClient httpClient;    // l'outil Java qui fait vraiment l'appel réseau
    private final String userAgent;         // notre "carte de visite"
    private final Duration requestTimeout;  // temps d'attente maximum

    public ExternalHttpClient(HttpClient httpClient, String userAgent, Duration requestTimeout) {
        this.httpClient = httpClient;
        this.userAgent = userAgent;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Fait une requête GET et renvoie le corps de la réponse (le JSON, en texte).
     * Lance une ExternalApiException en cas de timeout, d'erreur réseau ou de code HTTP non 2xx.
     */
    public String get(URI uri) {
        // 1. Préparer la requête : l'équivalent de : curl -A "<userAgent>" "<uri>"
        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(requestTimeout)                 // timeout
                .header("User-Agent", userAgent)         // carte de visite
                .header("Accept", "application/json")    // "je veux du JSON"
                .GET()
                .build();

        // 2. Envoyer la requête, en attrapant les problèmes techniques
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (HttpTimeoutException e) {
            // L'API n'a pas répondu à temps
            throw new ExternalApiException(ExternalApiException.Reason.TIMEOUT,
                    "Timeout after " + requestTimeout.toSeconds() + "s calling " + uri, e);
        } catch (IOException e) {
            // Problème réseau : pas d'internet, serveur injoignable...
            throw new ExternalApiException(ExternalApiException.Reason.UNAVAILABLE,
                    "Network error calling " + uri, e);
        } catch (InterruptedException e) {
            // Le programme a été interrompu pendant l'attente
            Thread.currentThread().interrupt();
            throw new ExternalApiException(ExternalApiException.Reason.UNAVAILABLE,
                    "Interrupted while calling " + uri, e);
        }

        // 3. Vérifier le code HTTP de la réponse
        int status = response.statusCode();
        if (status == 404) {
            throw new ExternalApiException(ExternalApiException.Reason.NOT_FOUND,
                    "Not found (404): " + uri);
        }
        if (status == 429) {
            throw new ExternalApiException(ExternalApiException.Reason.RATE_LIMITED,
                    "Rate limit exceeded (429): " + uri);
        }
        if (status < 200 || status >= 300) {
            throw new ExternalApiException(ExternalApiException.Reason.HTTP_ERROR,
                    "HTTP " + status + " calling " + uri);
        }

        // 4. Tout va bien : on renvoie le JSON (sous forme de texte)
        return response.body();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public Duration getRequestTimeout() {
        return requestTimeout;
    }
}
