# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 107 files · ~70,056 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1395 nodes · 3283 edges · 115 communities (42 shown, 73 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 359 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b3f42ba7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DocumentImporter
- ReaderActions.kt
- LongPdfImportTest.kt
- ListeningScreens.kt
- Design System: Tuturium
- TuturiumMenus.kt
- steps
- Tuturium
- with
- TuturiumCloud
- MainActivity.kt
- OfflineAudio.kt
- ModernScreens.kt
- Riset Tuturium
- GoogleAccountAccess.kt
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- Pass
- ReadingService
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- ListeningScreens.kt
- Theme.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderMenuSheet
- LibraryScreenV2.kt
- profile-android.sh
- update-graph.sh
- NarrationOpus.kt
- ReaderVoiceParityTest.kt
- LibraryV2
- ReadingService.kt
- Supertonic adaptif · Tuturium 1.3.17
- Reader.kt
- isInLibraryFolder
- isCompatibleCloudAccount
- ListeningStats.kt
- BookActions
- README.md
- AndroidJUnit4
- GoogleUpdateRegressionTest
- .startOffline
- Updates.kt
- SettingsScreen.kt
- ReadingDocument
- Book
- isMatchingDriveAccount
- .write
- UtteranceProgressListener
- Alur kode
- .read
- Updates
- narrationGain
- NarrationFlow.kt
- spokenPcmRange
- LibraryControlsTest
- SynthesizedAudio
- synthesisRanges
- instrumentationregistry
- formatPlaybackTime
- .endPage
- Callback
- Playback
- AutomaticSpeed

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 55 edges
2. `ReadingDocument` - 39 edges
3. `ReaderContent()` - 33 edges
4. `Library` - 32 edges
5. `LibraryV2()` - 32 edges
6. `Book` - 28 edges
7. `Section` - 28 edges
8. `ListeningOptions` - 27 edges
9. `Tuturium()` - 24 edges
10. `TuturiumCloud` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `ModernTrash()` --calls--> `readingPosition()`  [INFERRED]
  app/src/main/java/id/tuturium/app/ModernScreens.kt → app/src/main/java/id/tuturium/app/ReadingTimePresentation.kt

## Import Cycles
- None detected.

## Communities (115 total, 73 thin omitted)

### Community 1 - "ReaderActions.kt"
Cohesion: 0.13
Nodes (12): Reader(), ReaderContent(), ReaderOptions(), AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl(), ReaderAppearanceSheet(), ReaderAudioSheet() (+4 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.16
Nodes (15): chooseVoice(), firstPassByPdfPage(), googleIndonesianVoices(), indonesianVoiceLabel(), isIndonesian(), isNewer(), pdfPageForPass(), pdfPageForSection() (+7 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "TuturiumMenus.kt"
Cohesion: 0.16
Nodes (4): MenuFrameRate(), TuturiumDialog(), TuturiumMenu(), TuturiumSheet()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.14
Nodes (14): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+6 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.28
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 11 - "MainActivity.kt"
Cohesion: 0.07
Nodes (6): estimatedReadingSpeed(), hasPosition(), libraryReadingLabel(), narrationMillisPerWord(), readingPosition(), readingSpeed()

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.07
Nodes (7): OfflineAudio, Clip, Listener, SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.18
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+8 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.29
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "Tuturium.kt"
Cohesion: 0.13
Nodes (5): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.13
Nodes (8): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), main()

### Community 20 - "Library"
Cohesion: 0.15
Nodes (3): Bookmark, Library, countDocumentWords()

### Community 22 - "ListeningOptions"
Cohesion: 0.05
Nodes (23): DocumentImportTest, documentWordEnds(), newReaderPace(), readingTimeline(), formatPlaybackSpeed(), playbackTimeCaption(), ReaderPlaybackBar(), NarrationPaceSnapshot (+15 more)

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "Pass"
Cohesion: 0.05
Nodes (22): MediaReadingCaption, Cue, OriginalPdfPages(), PdfPageImage(), PdfPageSession, matchPdfWords(), nearestPdfWord(), ocrPdfWordHits() (+14 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.24
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.23
Nodes (5): refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ListeningScreens.kt"
Cohesion: 0.24
Nodes (13): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+5 more)

### Community 40 - "release-notes.md"
Cohesion: 0.29
Nodes (6): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22

### Community 45 - "ReaderMenuSheet"
Cohesion: 0.39
Nodes (5): ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet(), ReaderVoiceSheet()

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.17
Nodes (3): encodeNarrationOpus(), NarrationResampler, synthesisThreadCount()

### Community 55 - "LibraryV2"
Cohesion: 0.11
Nodes (9): LibraryV2FlowTest, FolderStore, bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2(), PinnedBookTitle(), SheetAction() (+1 more)

### Community 57 - "ReadingService.kt"
Cohesion: 0.09
Nodes (4): availableVoices(), choice(), indonesianCandidates(), offlineVoices()

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 62 - "Reader.kt"
Cohesion: 0.12
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 67 - "ListeningStats.kt"
Cohesion: 0.17
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 74 - ".startOffline"
Cohesion: 0.32
Nodes (4): OfflineVoiceRuntimeTest, OfflinePass, OfflineSession, PreparedSpeech

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.35
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "ReadingDocument"
Cohesion: 0.22
Nodes (3): DriveArchive, ReadingDocument, Section

### Community 84 - "Book"
Cohesion: 0.26
Nodes (5): Book, isInLibraryFolder(), matchesLibraryFilter(), sortBooks(), LibraryPresentationTest

### Community 90 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

### Community 100 - "narrationGain"
Cohesion: 0.28
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 101 - "NarrationFlow.kt"
Cohesion: 0.39
Nodes (8): clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter(), normalizedForNarration(), pauseAfter(), withoutClosingPunctuation()

### Community 104 - "SynthesizedAudio"
Cohesion: 0.61
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 107 - "formatPlaybackTime"
Cohesion: 0.38
Nodes (3): readerCenterDelta(), formatPlaybackTime(), ReaderReturnRegressionTest

## Knowledge Gaps
- **73 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `ListeningScreens.kt`, `.startOffline`, `OfflineAudio.kt`, `Playback`, `AutomaticSpeed`, `ReadingDocument`, `ListeningOptions`, `.read`, `ReaderVoiceParityTest.kt`, `ReadingService.kt`, `Alur kode`, `Pass`?**
  _High betweenness centrality (0.108) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `ReaderActions.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.12962962962962962 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `DocumentImporter`, `ReadingService`, `Updates`, `Library`?**
  _High betweenness centrality (0.083) - this node is a cross-community bridge._
- **Are the 24 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImportTest.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11052631578947368 - nodes in this community are weakly interconnected._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._