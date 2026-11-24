package org.example.escapehubbackend.external.openlibrary;

import java.util.Optional;

/**
 * Un auteur trouvé sur Open Library.
 * birthDate et deathDate sont du texte libre ("30 August 1797") :
 * on ne s'en sert pas pour valider les réponses des joueurs.
 */
public record AuthorInfo(
        String key,        // ex. "OL25342A"
        String name,
        String birthDate,
        String deathDate,
        String wikidataId  // ex. "Q47152" (le pont vers Wikidata), ou null si inconnu
) {

    /** Le pont vers Wikidata, s'il existe. */
    public Optional<String> wikidata() {
        return Optional.ofNullable(wikidataId);
    }
}
