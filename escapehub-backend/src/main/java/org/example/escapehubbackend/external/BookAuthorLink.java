package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.openlibrary.AuthorInfo;
import org.example.escapehubbackend.external.openlibrary.BookInfo;
import org.example.escapehubbackend.external.wikidata.PersonInfo;

import java.util.Optional;

/**
 * Résultat de la recherche croisée (MVP §6) : un livre trouvé sur Open Library,
 * son auteur sur Open Library, et le même auteur sur Wikidata.
 * La chaîne peut s'arrêter avant la fin : author et person peuvent être absents.
 */
public record BookAuthorLink(BookInfo book, AuthorInfo author, PersonInfo person) {

    public Optional<AuthorInfo> authorInfo() {
        return Optional.ofNullable(author);
    }

    public Optional<PersonInfo> personInfo() {
        return Optional.ofNullable(person);
    }
}
