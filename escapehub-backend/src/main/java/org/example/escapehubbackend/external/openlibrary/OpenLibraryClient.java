package org.example.escapehubbackend.external.openlibrary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;   // annotations : ancien package
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.escapehubbackend.external.ExternalApiException;
import org.example.escapehubbackend.external.ExternalHttpClient;
import org.example.escapehubbackend.external.snapshot.SnapshotStore;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;                       // Jackson 3 : nouveau package
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;


@Component
public class OpenLibraryClient {

    static final String BASE_URL = "https://openlibrary.org";
    // Seulement les champs utiles (comme ton curl avec fields=)
    static final String SEARCH_FIELDS = "key,title,author_name,author_key,first_publish_year";

    // Formats attendus des identifiants (pour refuser les entrées bizarres)
    private static final Pattern AUTHOR_KEY = Pattern.compile("OL\\d+A");
    private static final Pattern WIKIDATA_ID = Pattern.compile("Q\\d+");

    // L'outil Jackson qui transforme le texte JSON en objets Java
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final ExternalHttpClient http;
    private final SnapshotStore snapshots;

    public OpenLibraryClient(ExternalHttpClient http, SnapshotStore snapshots) {
        this.http = http;
        this.snapshots = snapshots;
    }

    /**
     * Cherche un livre à partir d'un texte (titre, sous-titre...). Renvoie le premier résultat.
     * Optional vide = Open Library n'a rien trouvé.
     */
    public Optional<BookInfo> searchBook(String query) {
        if (query == null || query.isBlank()) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_INPUT, "Empty search query");
        }
        // 1. Snapshot d'abord, sinon appel en direct
        String snapshotKey = "openlibrary/search-" + SnapshotStore.slug(query);
        String json = snapshots.find(snapshotKey).orElseGet(() -> http.get(searchUri(query)));

        // 2. JSON -> objets Java
        SearchResponse response = parse(json, SearchResponse.class);

        // 3. Aucun résultat ?
        if (response.docs() == null || response.docs().isEmpty()) {
            return Optional.empty();
        }

        // 4. On traduit le premier résultat en BookInfo (noms Java propres)
        SearchDoc doc = response.docs().get(0);
        return Optional.of(new BookInfo(
                doc.key(), doc.title(), doc.authorName(), doc.authorKey(), doc.firstPublishYear()));
    }

    /**
     * Récupère un auteur par son identifiant Open Library (ex. "OL25342A").
     */
    public AuthorInfo getAuthor(String authorKey) {
        // Sécurité : on refuse tout identifiant qui n'a pas le bon format
        if (authorKey == null || !AUTHOR_KEY.matcher(authorKey).matches()) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_INPUT,
                    "Invalid Open Library author id: " + authorKey);
        }
        String snapshotKey = "openlibrary/author-" + authorKey;
        String json = snapshots.find(snapshotKey)
                .orElseGet(() -> http.get(URI.create(BASE_URL + "/authors/" + authorKey + ".json")));

        AuthorResponse response = parse(json, AuthorResponse.class);

        // Le pont vers Wikidata : remote_ids.wikidata
        String wikidataId = response.remoteIds() == null ? null : response.remoteIds().get("wikidata");
        if (wikidataId != null && !WIKIDATA_ID.matcher(wikidataId).matches()) {
            wikidataId = null;   // identifiant malformé : on l'ignore
        }
        return new AuthorInfo(authorKey, response.name(), response.birthDate(), response.deathDate(), wikidataId);
    }

    /** Construit l'URL de recherche. URLEncoder transforme les espaces et caractères spéciaux. */
    static URI searchUri(String query) {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return URI.create(BASE_URL + "/search.json?q=" + q + "&limit=1&fields=" + SEARCH_FIELDS);
    }

    /** Transforme le texte JSON en objet Java. JSON invalide -> INVALID_RESPONSE. */
    private static <T> T parse(String json, Class<T> type) {
        try {
            T value = MAPPER.readValue(json, type);
            if (value == null) {
                throw new ExternalApiException(ExternalApiException.Reason.INVALID_RESPONSE,
                        "Empty JSON response from Open Library");
            }
            return value;
        } catch (JacksonException e) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_RESPONSE,
                    "Invalid JSON from Open Library", e);
        }
    }

    // --- La forme du JSON brut d'Open Library (seulement les champs utiles) ---

    @JsonIgnoreProperties(ignoreUnknown = true)   // ignore les ~100 autres champs
    record SearchResponse(List<SearchDoc> docs) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record SearchDoc(
            String key,
            String title,
            @JsonProperty("author_name") List<String> authorName,           // nom JSON -> nom Java
            @JsonProperty("author_key") List<String> authorKey,
            @JsonProperty("first_publish_year") Integer firstPublishYear) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AuthorResponse(
            String name,
            @JsonProperty("birth_date") String birthDate,
            @JsonProperty("death_date") String deathDate,
            @JsonProperty("remote_ids") Map<String, String> remoteIds) {
    }
}
