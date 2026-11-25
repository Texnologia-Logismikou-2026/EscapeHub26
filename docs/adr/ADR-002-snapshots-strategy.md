# ADR-002: Στρατηγική Caching & Snapshots για Εξωτερικά Public APIs

- **Κατάσταση:** Εγκεκριμένο (Accepted)
- **Ημερομηνία:** 2026-11-23
- **Σχετικό Issue:** Issue #1 / Issue #3 — Public APIs Integration (Open Library & Wikidata)

## Πλαίσιο (Context)
Η αποστολή εξαρτάται από δύο εξωτερικά Public APIs (Open Library API και Wikidata API). Τα εξωτερικά APIs ενδέχεται να παρουσιάσουν καθυστερήσεις (latency), περιορισμούς ρυθμού (rate limits / HTTP 429), προσωρινή μη διαθεσιμότητα ή αλλαγές σε πεδία απόκρισης, γεγονός που θα έσπαγε τόσο το gameplay όσο και τα αυτοματοποιημένα tests στο CI.

## Απόφαση (Decision)
Υιοθετείται στρατηγική **Live-first με In-Memory Cache και Local JSON Snapshot Fallback**:
1. **Live κλήση με αυστηρό timeout** (π.χ. 3–5 δευτερόλεπτα) και κατάλληλο `User-Agent` header.
2. **In-Memory Caching (TTL):** Επιτυχείς αποκρίσεις αποθηκεύονται προσωρινά (π.χ. TTL 24 ώρες) ώστε να αποφεύγονται επαναλαμβανόμενες κλήσεις για τα ίδια βιβλία/οντότητες.
3. **Local Snapshots (`src/main/resources/snapshots/`):** Κάθε `apiSource` στο JSON της αποστολής ορίζει ένα `snapshotKey`. Αν το εξωτερικό API αποτύχει ή λήξει το timeout (ή σε offline/CI test mode), το σύστημα επιστρέφει αυτόματα το αντίστοιχο τοπικό snapshot.

## Εναλλακτικές που Απορρίφθηκαν
1. **Αποκλειστικά live κλήσεις χωρίς fallback:** Υψηλός κίνδυνος αποτυχίας στην επίδειξη και flaky CI tests.
2. **Αποκλειστικά στατικά δεδομένα χωρίς live API:** Παραβιάζει την απαίτηση ουσιαστικής αξιοποίησης πραγματικών Public APIs.

## Συνέπειες (Consequences)
- **Θετικές:** Αξιόπιστη λειτουργία ακόμη και σε διακοπή δικτύου/rate-limit, γρήγορα και ντετερμινιστικά tests στο GitHub Actions, σταθερό συμβόλαιο μέσω `snapshotKey` ανάμεσα στο Issue #1 (Mission Model) και το Issue #3 (API Client).
- **Αρνητικές:** Τα τοπικά snapshots πρέπει να διατηρούνται συγχρονισμένα με τη δομή των απαντήσεων των APIs αν προστεθούν νέες αποστολές.