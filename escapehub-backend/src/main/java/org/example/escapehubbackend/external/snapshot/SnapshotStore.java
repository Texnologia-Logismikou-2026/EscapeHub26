package org.example.escapehubbackend.external.snapshot;

import org.example.escapehubbackend.external.ExternalApiException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;

/**
 * Lit les réponses d'API "gelées" (snapshots), stockées en JSON dans
 * src/main/resources/snapshots/ (MVP §7).
 *
 * Exemples de clés :
 *   openlibrary/search-the-modern-prometheus
 *   openlibrary/author-OL25342A
 *   wikidata/Q47152
 */
public class SnapshotStore {

    public static final String DEFAULT_BASE_PATH = "snapshots";

    private final String basePath;   // le dossier où chercher (dans resources/)

    public SnapshotStore(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Renvoie le contenu JSON du snapshot correspondant à la clé, s'il existe.
     * Optional vide = pas de snapshot -> le client appellera l'API en direct.
     */
    public Optional<String> find(String key) {
        // ex. "/snapshots/openlibrary/author-OL25342A.json"
        String resource = "/" + basePath + "/" + key + ".json";

        // getResourceAsStream cherche le fichier dans "resources" (le classpath).
        // try-with-resources : le fichier est fermé automatiquement à la fin.
        try (InputStream in = SnapshotStore.class.getResourceAsStream(resource)) {
            if (in == null) {
                return Optional.empty();   // le fichier n'existe pas
            }
            return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_RESPONSE,
                    "Cannot read snapshot " + resource, e);
        }
    }

    /**
     * Transforme un texte libre en nom de fichier stable :
     * "The Modern Prometheus" -> "the-modern-prometheus".
     */
    public static String slug(String text) {
        return text.toLowerCase(Locale.ROOT)          // tout en minuscules
                .replaceAll("[^a-z0-9]+", "-")        // tout ce qui n'est pas lettre/chiffre -> "-"
                .replaceAll("(^-+)|(-+$)", "");       // enlève les "-" au début et à la fin
    }
}
