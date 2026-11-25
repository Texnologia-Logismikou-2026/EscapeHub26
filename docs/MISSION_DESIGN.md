# Σχεδιασμός Αποστολής (Mission Design) — EscapeHub

## 1. Γενικά Στοιχεία Αποστολής
- **ID / Code:** `1` / `encrypted-library`
- **Τίτλος:** Το Κρυπτογραφημένο Αρχείο της Βιβλιοθήκης (*The Encrypted Library Archive*)
- **Κατηγορία:** Μυστήριο / Ιστορική Έρευνα
- **Τρόπος Παιχνιδιού:** Ατομικά ή Ομαδικά (1–4 παίκτες)
- **Εξωτερικές Πηγές Δεδομένων (Public APIs):**
    1. **Open Library API** (`https://openlibrary.org/search.json`, `https://openlibrary.org/search/authors.json`, `https://openlibrary.org/authors/{key}.json`)
    2. **Wikidata API** (`https://www.wikidata.org/w/api.php?action=wbgetentities...`)

### Σύνοψη Σεναρίου
Ένας εκκεντρικός βιβλιοθηκάριος εξαφανίστηκε, αφήνοντας πίσω του ένα κλειδωμένο ψηφιακό χρηματοκιβώτιο που περιέχει ένα σπάνιο χειρόγραφο. Οι παίκτες έχουν πρόσβαση στο γραφείο του και σε τέσσερα αρχικά στοιχεία (evidence). Για να αποδράσουν, πρέπει να εξερευνήσουν **τρεις ανεξάρτητες διαδρομές έρευνας** με οποιαδήποτε σειρά, να αντλήσουν πραγματικά βιβλιογραφικά και βιογραφικά δεδομένα από το **Open Library** και τη **Wikidata**, και να συσχετίσουν τα ευρήματά τους στο **Shared Diary** της ομάδας.

---

## 2. Μη Γραμμική Δομή & Διαδρομές
Η αποστολή διαθέτει **3 παράλληλες διαδρομές** (Διαδρομές Α, Β, Γ) και **4 γρίφους** (3 ανεξάρτητους + 1 τελικό συνδυαστικό). Οι παίκτες μπορούν να τις επιλύσουν με οποιαδήποτε σειρά ή παράλληλα ως ομάδα. Και οι τρεις συγκλίνουν στο **Τελικό Στάδιο (Puzzle D)**.

```text
                       [ Έναρξη: Γραφείο Βιβλιοθηκάριου ]
                        /               |               \
                       v                v                v
          [ Διαδρομή Α ]          [ Διαδρομή Β ]          [ Διαδρομή Γ ]
        Σκισμένη Σελίδα          Κάρτα Δανεισμού         Σφραγίδα Ταχυδρομείου
      (Open Library Search)   (Open Library Authors)       (Wikidata API)
           Puzzle A                 Puzzle B                  Puzzle C
                       \                |                /
                        v               v               v
                [ Shared Diary: Συσχέτιση Κλειδιών Α + Β + Γ ]
                                        |
                                        v
               [ Τελικό Στάδιο (Puzzle D): Ψηφιακό Χρηματοκιβώτιο ]
                                        |
                                        v
                            [ Ολοκλήρωση / Απόδραση ]
```

---

## 3. Στοιχεία (Evidence), Γρίφοι & Χρήση Public APIs

### Αρχικό Στάδιο (Unlocked by default)
- **`evidence-1` (Ημερολόγιο Γραφείου):** *«Το χρηματοκιβώτιο ανοίγει μόνο όταν η πόλη γέννησης του οραματιστή ενωθεί με τη διαφορά των δύο ετών: το έτος του μεγάλου υποθαλάσσιου ταξιδιού μείον τη χρονιά γέννησης της δημιουργού της φρίκης (μορφή: `ΠΟΛΗ-ΔΙΑΦΟΡΑ`).»*
- **`evidence-2` (Σκισμένη Σελίδα — Διαδρομή Α):** Αναφέρει τον τίτλο *"Twenty Thousand Leagues Under the Sea"*.
- **`evidence-3` (Παλιά Κάρτα Αρχείου — Διαδρομή Β):** Αναφέρει το όνομα συγγραφέως *"Mary Shelley"*.
- **`evidence-4` (Σφραγίδα Ταχυδρομείου — Διαδρομή Γ):** Κρατά τον κωδικό οντότητας **`Q33977`** (*Jules Verne*) και ζητά τον τόπο γέννησής του (`P19`).

### Διαδρομή Α — `puzzle-a` (Τύπος: `API_YEAR_LOOKUP`)
- **Στόχος:** Εντοπισμός του έτους πρώτης έκδοσης (`first_publish_year`) του βιβλίου *"Twenty Thousand Leagues Under the Sea"*.
- **Public API (Open Library Search):**
  `GET https://openlibrary.org/search.json?title=Twenty+Thousand+Leagues+Under+the+Sea`
- **Επικύρωση:** Δυναμική σύγκριση με το πεδίο `first_publish_year` (`1870`) με fallback σε τοπικό snapshot.
- **Ανταμοιβή:** Ξεκλειδώνει το **`evidence-5` (Κλειδί Α)**.

### Διαδρομή Β — `puzzle-b` (Τύπος: `API_AUTHOR_BIRTH_YEAR`)
- **Στόχος:** Εντοπισμός του έτους γέννησης (`birth_date`) της συγγραφέως *"Mary Shelley"*.
- **Public API (Open Library Authors):**
    1. `GET https://openlibrary.org/search/authors.json?q=Mary+Shelley` → `key = OL26444A`
    2. `GET https://openlibrary.org/authors/OL26444A.json` → `birth_date` (`1797`)
- **Επικύρωση:** Δυναμική σύγκριση του έτους γέννησης (`1797`).
- **Ανταμοιβή:** Ξεκλειδώνει το **`evidence-6` (Κλειδί Β)**.

### Διαδρομή Γ — `puzzle-c` (Τύπος: `API_ENTITY_PROPERTY`)
- **Στόχος:** Εντοπισμός της πόλης γέννησης (`P19` — *place of birth*) του συγγραφέα Jules Verne (`Q33977`).
- **Public API (Wikidata):**
    1. `GET https://www.wikidata.org/w/api.php?action=wbgetentities&ids=Q33977&props=claims&format=json` → ιδιότητα `P19` (`Q12191`)
    2. `GET https://www.wikidata.org/w/api.php?action=wbgetentities&ids=Q12191&props=labels&languages=en&format=json` → label (`Nantes`)
- **Επικύρωση:** Case-insensitive σύγκριση με την πόλη γέννησης (`Nantes`).
- **Ανταμοιβή:** Ξεκλειδώνει το **`evidence-7` (Κλειδί Γ)**.

### Τελικό Στάδιο — `puzzle-d` (Τύπος: `COMBINED_CORRELATION`)
- **Προαπαιτούμενο:** `puzzle-a == SOLVED AND puzzle-b == SOLVED AND puzzle-c == SOLVED`.
- **Συσχέτιση Open Library + Wikidata:** Οι παίκτες συνδυάζουν την πόλη από τη Wikidata (`Nantes`) με τη διαφορά των δύο ετών από το Open Library (`1870 - 1797 = 73`).
- **Τελικός Κωδικός:** `NANTES-73` (case-insensitive).
- **Συνθήκη Απόδρασης (Escape Condition):** Η επιτυχής επίλυση του `puzzle-d` μεταβαίνει το `MissionSession` σε κατάσταση `COMPLETED` και καταγράφει τον χρόνο ολοκλήρωσης.

---

## 4. Κανόνες Ξεκλειδώματος (Unlock Rules)

| Στάδιο / Στοιχείο | Κατάσταση Έναρξης | Κανόνας Ξεκλειδώματος |
| :--- | :--- | :--- |
| **`evidence-1` έως `evidence-4`** | Unlocked | Διαθέσιμα αμέσως με την έναρξη του `MissionSession`. |
| **`puzzle-a`, `puzzle-b`, `puzzle-c`** | Unlocked | Διαθέσιμα παράλληλα από την έναρξη (μη γραμμική ροή). |
| **`evidence-5` (Κλειδί Α)** | Locked | Ξεκλειδώνει όταν `puzzle-a == SOLVED`. |
| **`evidence-6` (Κλειδί Β)** | Locked | Ξεκλειδώνει όταν `puzzle-b == SOLVED`. |
| **`evidence-7` (Κλειδί Γ)** | Locked | Ξεκλειδώνει όταν `puzzle-c == SOLVED`. |
| **`puzzle-d` (Χρηματοκιβώτιο)** | Locked | Ξεκλειδώνει **μόνο** όταν `puzzle-a AND puzzle-b AND puzzle-c == SOLVED`. |
| **Απόδραση (`COMPLETED`)** | Locked | Επιτυγχάνεται με την επίλυση του `puzzle-d`. |

---

## 5. Ρόλοι Ομάδας & Shared Diary
- **Team Leader:** Ξεκινά τη συνεδρία της αποστολής (`POST /api/sessions`) και συντονίζει την τελική υποβολή στο χρηματοκιβώτιο.
- **Investigators (Μέλη):** Μοιράζονται παράλληλα τις Διαδρομές Α, Β και Γ, καταγράφουν τα κλειδιά τους στο **Shared Diary** (`POST /api/diary`) και παρακολουθούν τα ευρήματα των συμπαικτών τους (`GET /api/diary/team/{teamId}`).