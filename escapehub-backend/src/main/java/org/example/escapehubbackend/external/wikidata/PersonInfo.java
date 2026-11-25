package org.example.escapehubbackend.external.wikidata;

import java.util.List;

/**
 * Une personne trouvée sur Wikidata, avec ses relations familiales.
 * Les relations sont des identifiants ("Q..."), pas des noms :
 * pour avoir le nom du père, on rappelle getPerson() avec son identifiant.
 */
public record PersonInfo(
        String wikidataId,       // ex. "Q47152"
        String name,             // le nom en anglais (ou une autre langue à défaut)
        List<String> fatherIds,  // propriété P22 = père
        List<String> motherIds,  // propriété P25 = mère
        List<String> spouseIds   // propriété P26 = conjoint(s)
) {

    public PersonInfo {
        fatherIds = fatherIds == null ? List.of() : List.copyOf(fatherIds);
        motherIds = motherIds == null ? List.of() : List.copyOf(motherIds);
        spouseIds = spouseIds == null ? List.of() : List.copyOf(spouseIds);
    }
}
