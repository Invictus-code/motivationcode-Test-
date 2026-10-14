# SPEC – Tagesmotivation

**Ziel:** Eine Android-App, die jeden Tag motivierende Zitate auf Deutsch zeigt, ähnlich wie „Motivation – Tägliche Zitate“, aber werbefrei, offline und ohne Konto.

## User Stories
1. Als Nutzer wische ich durch Zitate im Vollbild, eins nach dem anderen.
2. Als Nutzer wähle ich eine Kategorie (z. B. Mut, Gelassenheit), damit nur passende Zitate kommen.
3. Als Nutzer markiere ich Zitate mit einem Herz und sehe meine Favoriten gesammelt.
4. Als Nutzer wähle ich ein Theme (Hintergrund + Schrift), das mir gefällt.
5. Als Nutzer teile ich ein Zitat als Bild mit Freunden.
6. Als Nutzer bekomme ich X Zitate pro Tag als Benachrichtigung, zwischen Start- und Endzeit.
7. Als Nutzer sehe ich ein Zitat im Widget auf dem Startbildschirm.
8. Als Nutzer lege ich eigene Sprüche an, bearbeite und lösche sie; sie erscheinen wie die anderen Zitate.

## MVP (umgesetzt)
- Swipe-Feed mit Kategorien und Favoriten
- 12 Themes, Teilen als Bild (PNG, 1080 × 1350)
- Erinnerungen: 1–10 pro Tag, gleichmäßig zwischen Start und Ende (auch über Mitternacht)
- Glance-Widget, wechselt alle 3 Stunden oder per Tipp auf ↻
- 131 kuratierte Zitate in 9 Kategorien, mitgeliefert, offline; davon 46 in „Sportmotivation“ (jedes einzeln mit Quelle geprüft)
- Eigene Zitate (Text + optionaler Autor): Kategorie „Meine Zitate“, zusätzlich in „Alle Zitate“, Widget und Erinnerungen; Verwalten über Kategorien → „Meine Zitate“ → Verwalten; Löschen mit „Rückgängig“

## Bewusst nicht drin
- Konto, Cloud-Sync, Werbung, Abo/Premium
- Online-Quelle für Zitate, Text-to-Speech, Streaks

## Screens
Feed (Start) · Kategorien · Themes · Einstellungen · Eigene Zitate

## Datenmodell
- `Quote(id, text, author, source?, translated, categories)` aus `assets/quotes.json`
- `UserPreferences` in DataStore: Favoriten-IDs, Feed-Filter, Theme-ID, Erinnerungs-Einstellungen
- `OwnQuote(id, text, author?, createdAt)` als JSON in DataStore (`files/datastore/own_quotes.json`, wird mitgesichert)

## Offene Punkte
- Urheberrecht: Einige Autoren sind keine 70 Jahre tot (z. B. Kästner, Hesse, Ende, Camus, Keller, King, Niebuhr, Brecht bis Ende 2026). Vor einer Play-Store-Veröffentlichung prüfen, ob die Kurz-Zitate unbedenklich sind.
- Package-Name `de.motivationcode.app` vor dem ersten Play-Upload final festlegen (danach nicht mehr änderbar).
- App-Name „Tagesmotivation“ ist ein Platzhalter.

## End-to-End-Prüfung
1. App installieren → Feed zeigt Zitate im Theme „Nacht“; nach oben wischen wechselt das Zitat.
2. Herz antippen → Kategorien → „Favoriten“ zeigt genau dieses Zitat.
3. Themes → „Papier“ → Feed hat hellen Hintergrund, Statusleiste dunkle Symbole.
4. Teilen → Teilen-Menü öffnet sich mit Bild.
5. Einstellungen → Erinnerungen an, 10 pro Tag, Fenster um die aktuelle Uhrzeit legen → Benachrichtigung kommt.
6. Widget hinzufügen → Zitat erscheint; ↻ wechselt es.
7. Kategorien → „Meine Zitate“ → Verwalten → Zitat ohne Autor anlegen → zurück, „Meine Zitate“ wählen → Zitat erscheint mit „Eigenes Zitat“; löschen → „Rückgängig“ holt es zurück.
