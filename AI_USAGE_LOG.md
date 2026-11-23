# AI Usage Log

Καταγραφή σημαντικών περιπτώσεων χρήσης εργαλείων τεχνητής νοημοσύνης ανά Sprint.

## Sprint 0 & Sprint 1

### 1. Υλοποίηση Mission Catalog & Shared Diary API
* **Τι πρότεινε το AI:** Δημιουργία των `MissionController`, `DiaryController`, `DiaryEntry`, `DiaryEntryRepository` και `CreateDiaryEntryRequest` σε Spring Boot.
* **Απόφαση:** Έγινε αποδεκτό με προσαρμογές στα package names του project.
* **Επαλήθευση:** Έλεγχος μεταγλώττισης με `.\mvnw compile` (`BUILD SUCCESS`) και ανασκόπηση κώδικα πριν το merge (PR #12).

### 2. Ελλιπής πρόταση AI — Αρχικός σχεδιασμός `MISSION_DESIGN.md`
* **Τι πρότεινε το AI:** Το αρχικό προσχέδιο αποστολής περιλάμβανε μόνο **2 διαδρομές** διερεύνησης και χρήση **1 δημόσιου API** (Open Library).
* **Πρόβλημα:** Η πρόταση ήταν ελλιπής ως προς τις απαιτήσεις MVP της εκφώνησης, οι οποίες απαιτούν τουλάχιστον **3 μη γραμμικές διαδρομές**, **4 γρίφους** και συσχέτιση δεδομένων από τουλάχιστον **2 διαφορετικά δημόσια APIs**.
* **Πώς εντοπίστηκε & διορθώθηκε:** Εντοπίστηκε κατά τη διασταύρωση των παραδοτέων με την Ενότητα 3 της εκφώνησης. Καταγράφηκε στο backlog ώστε στο Sprint 2 να προστεθεί 3η διαδρομή και 2η δημόσια πηγή (Wikidata) με συσχέτιση ευρημάτων.

### ui
* ** Κατα την δημιουργια του ui το ai εφτιαξε μια περιπλοκη ιστοσελιδα με ai generated εικονες και δικα του σεναρια. Επειδη τα σεναρια θα φτιαχτουν απο εμας και οι εικονες θα ειναι stock photos η καλυτερη επιλογη ηταν η χρηση pre-existing ui σκελετου με αλλαγη χρωματος προσθεση σελιδων και λειτουργιων με css και js
* ** Xρηση ai για να συνδιασω τα 2 servers (frontend backend)


## Sprint 2

### 1. Υλοποίηση MissionSession, PostgreSQL/H2 & /actuator/info (Issue #18)
* **Τι πρότεινε το AI:** Υλοποίηση του `MissionSession` (entity, enum, repository, DTOs, service, controller, unit tests), ρύθμιση PostgreSQL/H2 προφίλ και προσθήκη Spring Boot Actuator (`/actuator/info`) με `git-commit-id-maven-plugin` (`9.2.0`) και σχετικές ιδιότητες στο `application.properties`.
* **Πρόβλημα:** Στην αρχική διαμόρφωση του `git-commit-id-maven-plugin` προτάθηκαν παράμετροι (`generateGitPropertiesImplementation`, `failOnUnableToExtractInfo`) που δεν αναγνωρίζονταν από την έκδοση `9.2.0` στο Maven sync, ενώ στην εκτέλεση `.\mvnw` από PowerShell χρειάστηκε είσοδος στον φάκελο `escapehub-backend` και ορισμός του `JAVA_HOME` σε εγκατεστημένο JDK (`openjdk-25`).
* **Πώς εντοπίστηκε & διορθώθηκε:** Τα σφάλματα εντοπίστηκαν στο **Problems** του IntelliJ και στο **Terminal** του PowerShell· αφαιρέθηκαν οι μη υποστηριζόμενες παράμετροι από το `<configuration>` του plugin, έγινε sync στο Maven και ορίστηκε το `$env:JAVA_HOME` στο `openjdk-25`.
* **Επαλήθευση:** Τοπική εκτέλεση `.\mvnw test "-Dspring.profiles.active=h2"` με επιτυχή ολοκλήρωση (`BUILD SUCCESS`, 5/5 tests passed) και άνοιγμα του PR #24 (`Closes #18`).
