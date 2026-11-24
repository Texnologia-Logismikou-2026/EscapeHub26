package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.snapshot.SnapshotStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration Spring des clients d'API externes.
 * Les valeurs peuvent être changées dans application.properties :
 *   escapehub.external.user-agent=EscapeHub/0.2 (contact@example.org)
 *   escapehub.external.timeout-seconds=5
 */
@Configuration
public class ExternalApiConfig {

    @Bean
    public ExternalHttpClient externalHttpClient(
            @Value("${escapehub.external.user-agent:EscapeHub/0.2 (+https://github.com/Texnologia-Logismikou-2026/EscapeHub26)}")
            String userAgent,
            @Value("${escapehub.external.timeout-seconds:5}")
            long timeoutSeconds) {

        Duration timeout = Duration.ofSeconds(timeoutSeconds);
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)                       // timeout de connexion
                .followRedirects(HttpClient.Redirect.NORMAL)   // suit les redirections HTTP
                .build();
        return new ExternalHttpClient(httpClient, userAgent, timeout);
    }

    @Bean
    public SnapshotStore snapshotStore() {
        return new SnapshotStore(SnapshotStore.DEFAULT_BASE_PATH);
    }
}
