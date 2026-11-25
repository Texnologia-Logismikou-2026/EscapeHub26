package org.example.escapehubbackend.external.openlibrary;

import java.util.List;
import java.util.Optional;

/**
 * Un livre trouvé sur Open Library, traduit en noms Java clairs.
 */
public record BookInfo(
        String workKey,           // ex. "/works/OL450063W"
        String title,
        List<String> authorNames, // tous les auteurs (un livre peut en avoir plusieurs)
        List<String> authorKeys,  // ex. "OL25342A", dans le même ordre que authorNames
        Integer firstPublishYear  // Integer : peut être null si l'année est inconnue
) {

    // Constructeur compact : remplace une liste null par une liste vide,
    // et crée une copie que personne ne pourra modifier.
    public BookInfo {
        authorNames = authorNames == null ? List.of() : List.copyOf(authorNames);
        authorKeys  = authorKeys  == null ? List.of() : List.copyOf(authorKeys);
    }

    /** L'identifiant du premier auteur, pour continuer la recherche. */
    public Optional<String> firstAuthorKey() {
        return authorKeys.isEmpty() ? Optional.empty() : Optional.of(authorKeys.get(0));
    }
}
