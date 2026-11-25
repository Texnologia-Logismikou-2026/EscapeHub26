package org.example.escapehubbackend.external.wikidata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.escapehubbackend.external.ExternalApiException;
import org.example.escapehubbackend.external.ExternalHttpClient;
import org.example.escapehubbackend.external.snapshot.SnapshotStore;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Client pour Wikidata :
 *   GET https://www.wikidata.org/wiki/Special:EntityData/{QID}.json
 * Wikimedia exige un User-Agent : il est ajouté par ExternalHttpClient.
 * Un snapshot est utilisé s'il existe ; sinon, l'API est appelée en direct.
 */
@Component
public class WikidataClient {

    static final String BASE_URL = "https://www.wikidata.org/wiki/Special:EntityData/";
    static final String FATHER = "P22";
    static final String MOTHER = "P25";
    static final String SPOUSE = "P26";

    private static final Pattern QID = Pattern.compile("Q\\d+");   // "Q" + des chiffres
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private final ExternalHttpClient http;
    private final SnapshotStore snapshots;

    public WikidataClient(ExternalHttpClient http, SnapshotStore snapshots) {
        this.http = http;
        this.snapshots = snapshots;
    }

    /**
     * Récupère une personne et ses relations familiales.
     * @param wikidataId ex. "Q47152" (peut venir de remote_ids d'Open Library)
     */
    public PersonInfo getPerson(String wikidataId) {
        // Sécurité : format de l'identifiant
        if (wikidataId == null || !QID.matcher(wikidataId).matches()) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_INPUT,
                    "Invalid Wikidata id: " + wikidataId);
        }
        // Snapshot d'abord, sinon appel en direct
        String json = snapshots.find("wikidata/" + wikidataId)
                .orElseGet(() -> http.get(URI.create(BASE_URL + wikidataId + ".json")));

        EntityDataResponse response = parse(json);
        if (response.entities() == null || response.entities().isEmpty()) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_RESPONSE,
                    "No entity in Wikidata response for " + wikidataId);
        }
        // Wikidata peut répondre avec un identifiant redirigé (fiches fusionnées) :
        // on prend donc la première entité, quelle que soit sa clé.
        Entity entity = response.entities().values().iterator().next();

        return new PersonInfo(
                entity.id() != null ? entity.id() : wikidataId,
                label(entity),
                relatedIds(entity, FATHER),
                relatedIds(entity, MOTHER),
                relatedIds(entity, SPOUSE));
    }

    /** Le nom en anglais, ou à défaut le premier nom disponible. */
    private static String label(Entity entity) {
        if (entity.labels() == null || entity.labels().isEmpty()) {
            return null;
        }
        Label english = entity.labels().get("en");
        if (english != null) {
            return english.value();
        }
        return entity.labels().values().iterator().next().value();
    }

    /**
     * Extrait les identifiants "Q..." d'une propriété de relation (ex. P22 = père).
     * Chemin dans le JSON : claims -> P22 -> [ mainsnak -> datavalue -> value -> id ]
     */
    private static List<String> relatedIds(Entity entity, String property) {
        if (entity.claims() == null) {
            return List.of();
        }
        List<Claim> claims = entity.claims().getOrDefault(property, List.of());
        return claims.stream()
                .map(Claim::mainsnak)
                .filter(Objects::nonNull)
                .map(Snak::datavalue)              // null pour "valeur inconnue"
                .filter(Objects::nonNull)
                .map(DataValue::value)
                .filter(Map.class::isInstance)     // la valeur doit être un objet { "id": ... }
                .map(value -> ((Map<?, ?>) value).get("id"))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toList();
    }

    /** JSON -> objets Java. JSON invalide -> INVALID_RESPONSE. */
    private static EntityDataResponse parse(String json) {
        try {
            EntityDataResponse value = MAPPER.readValue(json, EntityDataResponse.class);
            if (value == null) {
                throw new ExternalApiException(ExternalApiException.Reason.INVALID_RESPONSE,
                        "Empty JSON response from Wikidata");
            }
            return value;
        } catch (JacksonException e) {
            throw new ExternalApiException(ExternalApiException.Reason.INVALID_RESPONSE,
                    "Invalid JSON from Wikidata", e);
        }
    }

    // --- La forme du JSON brut de Wikidata (seulement les champs utiles) ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    record EntityDataResponse(Map<String, Entity> entities) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Entity(String id, Map<String, Label> labels, Map<String, List<Claim>> claims) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Label(String language, String value) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Claim(Snak mainsnak) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Snak(String snaktype, DataValue datavalue) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record DataValue(Object value, String type) {
    }
}
