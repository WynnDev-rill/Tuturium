# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 116 files · ~81,560 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1523 nodes · 3672 edges · 119 communities (45 shown, 74 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 404 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3bdb9dac`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .play
- Reader.kt
- PdfNarration.kt
- PdfSourceStorageTest.kt
- ListeningScreens.kt
- Design System: Tuturium
- TuturiumMenus.kt
- steps
- Tuturium
- with
- TuturiumCloud
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- PlaybackTest.kt
- GoogleAccountAccess.kt
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- file
- narrationGain
- Pemrosesan teks untuk narasi Tuturium
- OriginalPdfPages.kt
- file
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- ReadingTimePresentation.kt
- Theme.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- ReaderSheetsV2.kt
- profile-android.sh
- update-graph.sh
- NarrationOpus.kt
- ReaderVoiceParityTest.kt
- LibraryScreenV2.kt
- ReadingService
- DocumentImportTest.kt
- Supertonic adaptif · Tuturium 1.3.17
- TuturiumAccountScreen.kt
- isCompatibleCloudAccount
- ListeningStats.kt
- README.md
- PdfMarginSettings
- Updates.kt
- SettingsScreen.kt
- ReadingDocument
- isMatchingDriveAccount
- .write
- UtteranceProgressListener
- DocumentImporter
- Pass
- LibraryV2FlowTest
- GoogleUpdateRegressionTest
- DriveArchive
- PdfPageImage
- StorageScreenV2.kt
- DocumentImportTest
- Updates
- OfflineVoices.kt
- SynthesizedAudio
- formatPlaybackTime
- PdfContentKind
- Alur kode
- pdfWordOutsideViewport
- .findsNearestWordOnSameLineButNotAcrossPage
- LibraryControlsTest
- Writer
- .highlightUsesOnlyCurrentPassAndIntersectingWords

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 57 edges
2. `ReadingDocument` - 52 edges
3. `ListeningOptions` - 34 edges
4. `ReaderContent()` - 34 edges
5. `Library` - 33 edges
6. `Section` - 33 edges
7. `LibraryV2()` - 32 edges
8. `Book` - 28 edges
9. `PreparedSpeech` - 25 edges
10. `TuturiumCloud` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (119 total, 74 thin omitted)

### Community 0 - ".play"
Cohesion: 0.10
Nodes (5): OfflineAudio, Clip, Listener, OfflinePass, PlaybackMetrics

### Community 1 - "Reader.kt"
Cohesion: 0.13
Nodes (12): Reader(), ReaderContent(), ReaderOptions(), AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl(), ReaderAppearanceSheet(), ReaderAudioSheet() (+4 more)

### Community 2 - "PdfNarration.kt"
Cohesion: 0.17
Nodes (6): cells(), PdfContentSpan, PdfMarginStore, PdfNarrationPlan, PdfNarrationStore, Key

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.10
Nodes (28): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+20 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.18
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "TuturiumMenus.kt"
Cohesion: 0.16
Nodes (4): MenuFrameRate(), TuturiumDialog(), TuturiumMenu(), TuturiumSheet()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.13
Nodes (15): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+7 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.26
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "SupertonicBuffer"
Cohesion: 0.20
Nodes (4): SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.17
Nodes (18): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernLibrary() (+10 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.25
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "Tuturium.kt"
Cohesion: 0.14
Nodes (5): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 20 - "Library"
Cohesion: 0.06
Nodes (19): FolderStore, Book, Bookmark, Library, BookActions, countDocumentWords(), isInLibraryFolder(), matchesLibraryFilter() (+11 more)

### Community 22 - "ListeningOptions"
Cohesion: 0.07
Nodes (14): MediaReadingCaption, Cue, forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech() (+6 more)

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "file"
Cohesion: 0.16
Nodes (12): PDFTextStripper, layoutLines(), PdfLine, alignPdfHits(), matchPdfWords(), ocrPdfWordHits(), pdfHighlightWords(), pdfKey() (+4 more)

### Community 26 - "narrationGain"
Cohesion: 0.21
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 27 - "Pemrosesan teks untuk narasi Tuturium"
Cohesion: 0.40
Nodes (4): ICU, CLDR, dan Revo Norm, Pemrosesan teks untuk narasi Tuturium, Penerapan pada pembaca offline, Rujukan pembaca

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.29
Nodes (4): OfflineVoiceRuntimeTest, refreshOfflineModels(), OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ReadingTimePresentation.kt"
Cohesion: 0.07
Nodes (17): documentWordEnds(), newReaderPace(), formatPlaybackSpeed(), playbackTimeCaption(), ReaderPlaybackBar(), NarrationPaceSnapshot, PaceProfile, PaceSample (+9 more)

### Community 40 - "release-notes.md"
Cohesion: 0.22
Nodes (8): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23, Tuturium 1.3.24

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 46 - "ReaderSheetsV2.kt"
Cohesion: 0.25
Nodes (7): ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet(), ReaderQueueSheet(), ReaderTimerSheet(), ReaderVoiceSheet()

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.16
Nodes (4): encodeNarrationOpus(), NarrationResampler, synthesisRanges(), synthesisThreadCount()

### Community 54 - "ReaderVoiceParityTest.kt"
Cohesion: 0.14
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, ReaderVoiceParityTest

### Community 57 - "ReadingService"
Cohesion: 0.05
Nodes (12): narrationBoostMb(), availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService, OfflineSession (+4 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 62 - "TuturiumAccountScreen.kt"
Cohesion: 0.11
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 67 - "ListeningStats.kt"
Cohesion: 0.11
Nodes (14): clockWords(), dateWords(), indonesianNumberReplacements(), romanWords(), spellDigits(), spellInteger(), spellNumber(), underThousand() (+6 more)

### Community 71 - "PdfMarginSettings"
Cohesion: 0.20
Nodes (8): excluding(), NarrationPipeline, MarginOverlay(), PdfMarginSheet(), buildPdfNarrationPlan(), PdfMargins, PdfMarginSettings, PdfNarrationTest

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 94 - "Pass"
Cohesion: 0.23
Nodes (6): ReaderRow, Pass, selectSpokenWord(), WordSelection, ReaderUpdateTest, ReaderWordSelectionTest

### Community 99 - "PdfPageImage"
Cohesion: 0.31
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 100 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 104 - "SynthesizedAudio"
Cohesion: 0.53
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 106 - "formatPlaybackTime"
Cohesion: 0.36
Nodes (3): readerCenterDelta(), formatPlaybackTime(), ReaderReturnRegressionTest

### Community 107 - "PdfContentKind"
Cohesion: 0.29
Nodes (7): PdfContentKind, DECORATION, FOOTER, FOOTNOTE, HEADER, MARGIN, TABLE

### Community 108 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

## Knowledge Gaps
- **6 isolated node(s):** `MARGIN`, `HEADER`, `FOOTER`, `FOOTNOTE`, `TABLE` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 397 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **74 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `.play`, `ReadingTimePresentation.kt`, `ListeningScreens.kt`, `PdfMarginSettings`, `Alur kode`, `PlaybackTest.kt`, `ReadingDocument`, `ListeningOptions`?**
  _High betweenness centrality (0.137) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `MARGIN`, `HEADER`, `FOOTER` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.10080645161290322 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `ReadingService`, `DocumentImporter`, `Library`, `Updates`?**
  _High betweenness centrality (0.113) - this node is a cross-community bridge._
- **Are the 29 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 29 INFERRED edges - model-reasoned connections that need verification._
- **Should `Reader.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.12962962962962962 - nodes in this community are weakly interconnected._