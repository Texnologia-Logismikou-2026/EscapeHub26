package org.example.escapehubbackend.external;

import org.example.escapehubbackend.external.openlibrary.AuthorInfo;
import org.example.escapehubbackend.external.openlibrary.BookInfo;
import org.example.escapehubbackend.external.openlibrary.OpenLibraryClient;
import org.example.escapehubbackend.external.wikidata.PersonInfo;
import org.example.escapehubbackend.external.wikidata.WikidataClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Teste la chaîne Open Library -> Wikidata avec des clients mockés. */
class ExternalDataServiceTest {

    private static final BookInfo FRANKENSTEIN = new BookInfo("/works/OL450063W",
            "Frankenstein; or, The Modern Prometheus", List.of("Mary Shelley"), List.of("OL25342A"), 1818);

    private OpenLibraryClient openLibrary;
    private WikidataClient wikidata;
    private ExternalDataService service;

    @BeforeEach
    void setUp() {
        openLibrary = mock(OpenLibraryClient.class);
        wikidata = mock(WikidataClient.class);
        service = new ExternalDataService(openLibrary, wikidata);
    }

    @Test
    void carriesOpenLibraryResultOverToWikidata() {
        when(openLibrary.searchBook("The Modern Prometheus")).thenReturn(Optional.of(FRANKENSTEIN));
        when(openLibrary.getAuthor("OL25342A"))
                .thenReturn(new AuthorInfo("OL25342A", "Mary Shelley", "30 August 1797", "1 February 1851", "Q47152"));
        when(wikidata.getPerson("Q47152"))
                .thenReturn(new PersonInfo("Q47152", "Mary Shelley", List.of("Q10"), List.of("Q20"), List.of("Q30")));

        BookAuthorLink link = service.findBookAuthor("The Modern Prometheus").orElseThrow();

        assertEquals(1818, link.book().firstPublishYear());
        assertEquals("Q47152", link.person().wikidataId());
        verify(wikidata).getPerson("Q47152");   // l'id trouvé sur Open Library est bien celui envoyé à Wikidata
    }

    @Test
    void stopsWhenNoBookFound() {
        when(openLibrary.searchBook(anyString())).thenReturn(Optional.empty());

        assertTrue(service.findBookAuthor("unknown").isEmpty());
        verify(wikidata, never()).getPerson(anyString());
    }

    @Test
    void stopsWhenAuthorHasNoWikidataId() {
        when(openLibrary.searchBook(anyString())).thenReturn(Optional.of(FRANKENSTEIN));
        when(openLibrary.getAuthor("OL25342A"))
                .thenReturn(new AuthorInfo("OL25342A", "Mary Shelley", null, null, null));

        BookAuthorLink link = service.findBookAuthor("The Modern Prometheus").orElseThrow();

        assertTrue(link.authorInfo().isPresent());
        assertTrue(link.personInfo().isEmpty());
        verify(wikidata, never()).getPerson(anyString());
    }

    @Test
    void propagatesApiFailure() {
        when(openLibrary.searchBook(anyString()))
                .thenThrow(new ExternalApiException(ExternalApiException.Reason.TIMEOUT, "timeout"));

        ExternalApiException e = assertThrows(ExternalApiException.class,
                () -> service.findBookAuthor("The Modern Prometheus"));
        assertEquals(ExternalApiException.Reason.TIMEOUT, e.getReason());
    }
}
