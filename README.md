# Tagesmotivation

Android-App mit motivierenden Zitaten auf Deutsch: Vollbild-Feed zum Wischen, Kategorien, Favoriten, eigene Zitate, Themes, Teilen als Bild, tägliche Erinnerungen und ein Homescreen-Widget. Alles offline, ohne Konto und ohne Werbung.

## Bauen und installieren

Voraussetzung: Android Studio (aktuelle Version) oder JDK 17+ mit Android SDK.

```bash
./gradlew assembleDebug        # APK unter app/build/outputs/apk/debug/
./gradlew installDebug         # direkt aufs verbundene Handy
./gradlew testDebugUnitTest    # Unit-Tests
```

## Aufbau

```
app/src/main/
├── assets/quotes.json              Zitate (Quelle der Wahrheit)
└── java/de/motivationcode/app/
    ├── data/                       Modelle, Repositories, DataStore, Themes, eigene Zitate (data/own)
    ├── ui/                         Compose-Screens + ViewModels (feed, categories, themes, settings, own)
    ├── reminders/                  Erinnerungen mit WorkManager
    ├── widget/                     Glance-Widget
    └── share/                      Zitat als Bild teilen
```

Mehr zum Umfang: [SPEC.md](SPEC.md) · Regeln für Claude Code: [CLAUDE.md](CLAUDE.md)

## Zitate ergänzen

Neue Einträge in `app/src/main/assets/quotes.json` eintragen und `./gradlew testDebugUnitTest` laufen lassen: Der Test prüft IDs, Kategorien und Pflichtfelder. Nur echte, belegbare Zitate aufnehmen; Übersetzungen mit `"translated": true` markieren.
