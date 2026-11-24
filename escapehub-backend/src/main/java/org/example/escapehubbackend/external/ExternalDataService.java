package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.openlibrary.AuthorInfo;
import org.example.escapehubbackend.external.openlibrary.BookInfo;
import org.example.escapehubbackend.external.openlibrary.OpenLibraryClient;
import org.example.escapehubbackend.external.wikidata.PersonInfo;
import org.example.escapehubbackend.external.wikidata.WikidataClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Relie les deux sources publiques (MVP §6) :
 *   "The Modern Prometheus"
 *     -> Open Library search  -> livre + id auteur (OL25342A)
 *     -> Open Library author  -> remote_ids.wikidata (Q47152)
 *     -> Wikidata             -> père / mère / conjoint
 */
@Service
public class ExternalDataService {

    private final OpenLibraryClient openLibrary;
    private final WikidataClient wikidata;

    public ExternalDataService(OpenLibraryClient openLibrary, WikidataClient wikidata) {
        this.openLibrary = openLibrary;
        this.wikidata = wikidata;
    }

    /**
     * @return vide si aucun livre ne correspond ; sinon le livre + la chaîne aussi loin que possible
     */
    public Optional<BookAuthorLink> findBookAuthor(String query) {
        // 1. Chercher le livre
        Optional<BookInfo> book = openLibrary.searchBook(query);
        if (book.isEmpty()) {
            return Optional.empty();
        }
        // 2. Prendre l'identifiant du premier auteur
        Optional<String> authorKey = book.get().firstAuthorKey();
        if (authorKey.isEmpty()) {
            return Optional.of(new BookAuthorLink(book.get(), null, null));
        }
        // 3. Fiche auteur sur Open Library -> on y trouve l'identifiant Wikidata
        AuthorInfo author = openLibrary.getAuthor(authorKey.get());
        Optional<String> wikidataId = author.wikidata();
        if (wikidataId.isEmpty()) {
            return Optional.of(new BookAuthorLink(book.get(), author, null));
        }
        // 4. Le résultat d'Open Library lance la recherche sur Wikidata
        PersonInfo person = wikidata.getPerson(wikidataId.get());
        return Optional.of(new BookAuthorLink(book.get(), author, person));
    }
}
