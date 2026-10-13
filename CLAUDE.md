# CLAUDE.md – Tagesmotivation

## What this app is
Android app that shows motivational quotes in German as a full-screen swipe feed, with categories, favorites, themes, share-as-image, daily reminder notifications and a home-screen widget. Solo hobby project, single app module. Scope: see SPEC.md.

## Commands
- Build: ./gradlew assembleDebug
- Unit tests: ./gradlew testDebugUnitTest
- Lint: ./gradlew lintDebug
- Install on device: ./gradlew installDebug
- Release build (R8 check): ./gradlew assembleRelease

## Stack (ask before changing)
- Kotlin, Jetpack Compose, Material 3, Navigation 3
- AGP 9 with built-in Kotlin (no `kotlin-android` plugin applied in app/)
- minSdk 26, targetSdk 36, compileSdk 37 (AndroidX libs require 37; targetSdk stays at the Play minimum)
- Versions only in gradle/libs.versions.toml
- DI: manual (`di/AppContainer.kt`) · Storage: Preferences DataStore (no Room) · Network: none
- Background work: WorkManager · Widget: Glance

## Architecture
- Composables are stateless where possible; each screen has a `XxxRoute` (collects the ViewModel) and a stateless `XxxScreen(uiState, callbacks)` with previews
- One ViewModel per screen (ViewModel, not AndroidViewModel) exposing uiState as a StateFlow; the UI collects it with collectAsStateWithLifecycle()
- No Context in ViewModels; ViewModels talk to repositories, repositories own the data
- Settings are the single source of truth for reminders: ViewModels only write settings, `MotivationApp` reacts and (re)schedules work
- Pure logic (feed building, reminder times, contrast) lives in plain Kotlin functions with unit tests

## Quotes (`app/src/main/assets/quotes.json`)
- Only real, verifiable quotes with correct attribution. Never invent or "improve" quotes.
- Known misattributions stay out (e.g. Einstein "insanity", Gandhi "be the change", Aristotle "we are what we repeatedly do").
- Non-German originals get `"translated": true` and a `source`. Text without surrounding quotation marks.
- `QuotesDataTest` checks the file; run the unit tests after every edit.

## How to work with me
- I'm a hobby developer. After each change, explain briefly in German what you changed and why.
- Ask before adding a library or changing the architecture.
- Before saying a task is done: build and run the relevant tests.
- API keys go into local.properties (git-ignored), never into code.
