# Snapshots / fixtures των εξωτερικών APIs

## Γιατί
Το MVP (§7) απαιτεί οι σωστές απαντήσεις να μην αλλάζουν όταν αλλάζουν οι εξωτερικές πηγές,
και η αποστολή να λειτουργεί όταν ένα API δεν απαντά.

## Πώς λειτουργεί
- Τα snapshots είναι αρχεία JSON στο `escapehub-backend/src/main/resources/snapshots/`.
- Οι clients (`OpenLibraryClient`, `WikidataClient`) **αναζητούν πρώτα snapshot**· μόνο αν δεν υπάρχει καλούν το ζωντανό API.
- Η ανάγνωση γίνεται από την κλάση `SnapshotStore`.

| Κλήση | Αρχείο |
|---|---|
| `searchBook("The Modern Prometheus")` | `snapshots/openlibrary/search-the-modern-prometheus.json` |
| `getAuthor("OL25342A")` | `snapshots/openlibrary/author-OL25342A.json` |
| `getPerson("Q47152")` | `snapshots/wikidata/Q47152.json` |

## Tests
- Τα unit tests δεν καλούν ποτέ τα ζωντανά APIs: ο `HttpClient` αντικαθίσταται από mock (Mockito).
- Περιπτώσεις: επιτυχία, αποτυχία (404/429/5xx), timeout, μη έγκυρο JSON.
- Το test `usesSnapshot_withoutCallingApi` επιβεβαιώνει ότι με snapshot δεν γίνεται καμία κλήση δικτύου.

## Περιορισμοί
- Ένα snapshot «παγώνει» τα δεδομένα· αν μια πληροφορία ήταν λάθος, το αρχείο ενημερώνεται χειροκίνητα (commit + review).
- Οι σχέσεις του Wikidata επιστρέφονται ως `Q...`· για τα ονόματα χρειάζεται νέα κλήση `getPerson`.
