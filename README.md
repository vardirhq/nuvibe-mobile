# Nuvibe — local music player for Android

A local-first "listening room" music player built with **Kotlin**, **Jetpack
Compose**, and **AndroidX Media3 (ExoPlayer)**. It scans the music on your
device and plays it — no accounts, no streaming, no mock data.

The UI follows the Nuvibe design: a dark (and warm-paper light) theme, serif
display type, gradient album art, an animated splash, a Now Playing sheet with
bars / waveform / lyrics views, and a floating mini player.

## Features

- **Real library from MediaStore** — songs, albums and artists scanned from the
  device. Album art is loaded when present, with a deterministic per-album
  gradient fallback that keeps the design's colourful look.
- **Media3 playback service** — background playback, lock-screen / notification
  controls, audio focus, headphone-unplug handling, and media-button support via
  a `MediaSessionService` + `MediaController`.
- **Home** — time-aware greeting, "Continue listening" (restored from your last
  session), recently-played albums, generated "Made for your room" mixes built
  from your own library, and library stats.
- **Search** — instant filtering across titles, artists and albums, with a
  browse grid when the query is empty.
- **Library** — Songs / Albums / Playlists. Shuffle all, album detail pages, and
  user playlists persisted with Room (create, add tracks, play).
- **Now Playing** — scrub bar, shuffle, repeat (off / all / one), an animated
  equaliser + waveform visualiser, and a live queue you can jump around in.
- **Settings** — Dark / Light theme, four accent colours, and real playback
  toggles (skip silence, pause on disconnect, respect other apps) that are
  applied live to the ExoPlayer. All preferences persist via DataStore.

## Tech / architecture

| Layer | Choice |
|------|--------|
| UI | Jetpack Compose, Material 3, custom design-token theme |
| Playback | Media3 ExoPlayer inside a `MediaSessionService`, driven by a `MediaController` |
| Library | `MediaStore` scan → in-memory `Library` snapshot (`StateFlow`) |
| Persistence | Room (playlists) + DataStore (settings, recents, resume position) |
| Images | Coil |
| DI | A small hand-rolled `AppContainer` on the `Application` |

Package layout under `dev.nuvibe.player`: `data/` (models, MediaStore scan, Room,
settings), `playback/` (service + controller), `ui/` (theme, components,
screens, player).

## Building

Open in Android Studio (Ladybug or newer) and run the `app` configuration, or:

```bash
./gradlew assembleDebug
```

- **compileSdk / targetSdk 35, minSdk 26.** JDK 17.
- On first launch, grant the audio permission (`READ_MEDIA_AUDIO` on Android 13+,
  `READ_EXTERNAL_STORAGE` below) so Nuvibe can build your library. Notification
  permission is requested on Android 13+ for the playback notification.

### A note on fonts

The design uses **Fraunces** (display) and **IBM Plex Sans** (body). To keep the
app fully offline with no bundled binaries, the theme falls back to the platform
serif / sans-serif families (`ui/theme/Type.kt`). To use the exact fonts, drop
the `.ttf` files into `app/src/main/res/font` and point `Display` / `Body` at
them, or wire up `androidx.compose.ui:ui-text-google-fonts`.

## Design source

The original design (HTML mock + screenshots) was used as the visual reference
for this Compose implementation.
