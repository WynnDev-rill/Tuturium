# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 107 files · ~70,048 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1395 nodes · 3283 edges · 96 communities (37 shown, 59 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 359 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `7f79565d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DocumentImporter.kt
- ReaderActions.kt
- DocumentImportTest.kt
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
- PdfPageImage
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- PdfWordSelection.kt
- Test
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- .write
- Audit Google dan pembaruan Tuturium 1.3.6
- PlaybackTest.kt
- Theme.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderContent
- LibraryScreenV2.kt
- profile-android.sh
- update-graph.sh
- Pass
- ReaderVoiceParityTest.kt
- Library
- ReadingService
- Supertonic adaptif · Tuturium 1.3.17
- Reader.kt
- isInLibraryFolder
- isCompatibleCloudAccount
- ListeningStats.kt
- Synchronized
- README.md
- AndroidJUnit4
- .highlightUsesOnlyCurrentPassAndIntersectingWords
- Test
- Updates.kt
- SettingsScreen.kt
- ReadingDocument
- .mapsRepeatedPdfWordsInReadingOrder
- isMatchingDriveAccount
- UtteranceProgressListener
- Alur kode

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
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (96 total, 59 thin omitted)

### Community 1 - "ReaderActions.kt"
Cohesion: 0.14
Nodes (10): AppearanceChoice(), AppearanceSwitch(), findReaderMatches(), NarrationPauseControl(), ReaderAppearanceSheet(), ReaderAudioSheet(), readerCursorColor(), ReaderMatch (+2 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.09
Nodes (27): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel(), voiceLabel() (+19 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

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
Cohesion: 0.06
Nodes (11): newReaderPace(), NarrationPaceSnapshot, PaceProfile, PaceSample, ReadingPace, estimatedReadingSpeed(), hasPosition(), libraryReadingLabel() (+3 more)

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.07
Nodes (7): OfflineAudio, Clip, Listener, OfflinePass, SupertonicBuffer, synthesisRanges(), ContinuousReadingTest

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
Nodes (6): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), TuturiumDialog()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.13
Nodes (8): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), main()

### Community 20 - "PdfPageImage"
Cohesion: 0.26
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 22 - "ListeningOptions"
Cohesion: 0.08
Nodes (22): documentWordEnds(), MediaReadingCaption, Cue, clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter() (+14 more)

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "PdfWordSelection.kt"
Cohesion: 0.20
Nodes (8): matchPdfWords(), nearestPdfWord(), ocrPdfWordHits(), pdfHighlightWords(), pdfKey(), PdfWordHit, readPdfWordHits(), PDFTextStripper

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.22
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".write"
Cohesion: 0.06
Nodes (17): refreshOfflineModels(), amplifyNarration(), NarrationAudioCache, narrationGain(), spokenPcmRange(), encodeNarrationOpus(), NarrationResampler, AudioCue (+9 more)

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 40 - "release-notes.md"
Cohesion: 0.29
Nodes (6): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22

### Community 45 - "ReaderContent"
Cohesion: 0.19
Nodes (12): Reader(), ReaderContent(), ReaderOptions(), RenameBookSheet(), ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet() (+4 more)

### Community 53 - "Pass"
Cohesion: 0.44
Nodes (4): Pass, selectSpokenWord(), WordSelection, ReaderWordSelectionTest

### Community 55 - "Library"
Cohesion: 0.05
Nodes (18): FolderStore, Book, Bookmark, Library, BookActions, countDocumentWords(), matchesLibraryFilter(), sortBooks() (+10 more)

### Community 57 - "ReadingService"
Cohesion: 0.06
Nodes (12): narrationBoostMb(), availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService, OfflineSession (+4 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 62 - "Reader.kt"
Cohesion: 0.08
Nodes (5): rate(), ReaderRow, AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 63 - "isInLibraryFolder"
Cohesion: 0.38
Nodes (3): isInLibraryFolder(), pdfWordOutsideViewport(), ReaderFolderRegressionTest

### Community 67 - "ListeningStats.kt"
Cohesion: 0.17
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 71 - "AndroidJUnit4"
Cohesion: 0.60
Nodes (3): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest

### Community 78 - "Updates.kt"
Cohesion: 0.10
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "ReadingDocument"
Cohesion: 0.06
Nodes (15): LibraryControlsTest, LibraryV2FlowTest, DriveArchive, SpeedControlSheet(), readingTimeline(), formatPlaybackSpeed(), formatPlaybackTime(), playbackTimeCaption() (+7 more)

### Community 90 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

## Knowledge Gaps
- **59 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `PlaybackTest.kt`, `ListeningScreens.kt`, `Synchronized`, `MainActivity.kt`, `OfflineAudio.kt`, `ReadingDocument`, `ListeningOptions`, `ReaderVoiceParityTest.kt`, `Alur kode`?**
  _High betweenness centrality (0.135) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImporter.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09475806451612903 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `DocumentImporter.kt`, `ReadingService`, `Updates.kt`, `Library`?**
  _High betweenness centrality (0.082) - this node is a cross-community bridge._
- **Are the 24 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Should `ReaderActions.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.13768115942028986 - nodes in this community are weakly interconnected._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._