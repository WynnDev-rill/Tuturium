# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 116 files · ~82,055 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1525 nodes · 3675 edges · 115 communities (48 shown, 67 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 405 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c750b05e`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .play
- Reader.kt
- PdfNarration.kt
- ListeningScreens.kt
- ListeningScreens.kt
- Design System: Tuturium
- TuturiumMenus.kt
- steps
- Tuturium
- with
- TuturiumCloud
- MainActivity.kt
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- Test
- GoogleAccountAccess.kt
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- PreparedSpeech
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
- isCompatibleCloudAccount
- ListeningStats.kt
- README.md
- PdfMarginSettings
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
- SynthesizedAudio
- PdfContentKind
- Alur kode
- .endPage
- pdfWordOutsideViewport
- .findsNearestWordOnSameLineButNotAcrossPage
- LibraryControlsTest
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
- `Implementasi 1.3.24` --references--> `NarrationPipeline`  [INFERRED]
  docs/narration-text-processing.md → app/src/main/java/id/tuturium/app/NarrationPipeline.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt

## Import Cycles
- None detected.

## Communities (115 total, 67 thin omitted)

### Community 0 - ".play"
Cohesion: 0.09
Nodes (6): OfflineAudio, Clip, Listener, OfflinePass, PlaybackMetrics, synthesisRanges()

### Community 1 - "Reader.kt"
Cohesion: 0.14
Nodes (9): AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl(), ReaderAppearanceSheet(), ReaderAudioSheet(), readerCursorColor(), ReaderSearchBar(), readerTypeface() (+1 more)

### Community 2 - "PdfNarration.kt"
Cohesion: 0.17
Nodes (6): cells(), PdfContentSpan, PdfMarginStore, PdfNarrationPlan, PdfNarrationStore, Key

### Community 3 - "ListeningScreens.kt"
Cohesion: 0.21
Nodes (13): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+5 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.15
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
Cohesion: 0.13
Nodes (15): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+7 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.26
Nodes (3): TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 11 - "MainActivity.kt"
Cohesion: 0.06
Nodes (6): estimatedReadingSpeed(), hasPosition(), libraryReadingLabel(), narrationMillisPerWord(), readingPosition(), readingSpeed()

### Community 12 - "SupertonicBuffer"
Cohesion: 0.20
Nodes (4): SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.18
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+8 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.25
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
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 20 - "Library"
Cohesion: 0.06
Nodes (18): FolderStore, Book, Bookmark, Library, BookActions, countDocumentWords(), matchesLibraryFilter(), sortBooks() (+10 more)

### Community 22 - "PreparedSpeech"
Cohesion: 0.20
Nodes (4): MediaReadingCaption, Cue, PreparedSpeech, MediaReadingCaptionTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "file"
Cohesion: 0.13
Nodes (6): ocrPdfWordHits(), pdfKey(), PdfWordStripper, readPdfWordHits(), readPdfWords(), Writer

### Community 26 - "narrationGain"
Cohesion: 0.28
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 27 - "Pemrosesan teks untuk narasi Tuturium"
Cohesion: 0.29
Nodes (6): ICU, CLDR, dan Revo Norm, Implementasi 1.3.24, Pemrosesan teks untuk narasi Tuturium, Penerapan pada pembaca offline, Pengujian host, Rujukan pembaca

### Community 28 - "OriginalPdfPages.kt"
Cohesion: 0.05
Nodes (5): NestedScrollConnection, rate(), AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.30
Nodes (6): OfflineVoiceRuntimeTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ReadingTimePresentation.kt"
Cohesion: 0.05
Nodes (24): documentWordEnds(), newReaderPace(), readingTimeline(), formatPlaybackSpeed(), playbackTimeCaption(), ReaderPlaybackBar(), NarrationPaceSnapshot, PaceProfile (+16 more)

### Community 40 - "release-notes.md"
Cohesion: 0.22
Nodes (8): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23, Tuturium 1.3.24

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 46 - "ReaderSheetsV2.kt"
Cohesion: 0.28
Nodes (10): Reader(), ReaderContent(), ReaderOptions(), ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet(), ReaderQueueSheet() (+2 more)

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.17
Nodes (3): encodeNarrationOpus(), NarrationResampler, synthesisThreadCount()

### Community 54 - "ReaderVoiceParityTest.kt"
Cohesion: 0.10
Nodes (4): DriveArchiveTest, PlaybackNoCrashTest, PlaybackTest, ReaderVoiceParityTest

### Community 57 - "ReadingService"
Cohesion: 0.05
Nodes (12): narrationBoostMb(), availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService, OfflineSession (+4 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 67 - "ListeningStats.kt"
Cohesion: 0.11
Nodes (15): clockWords(), dateWords(), indonesianNumberReplacements(), romanWords(), spellDigits(), spellInteger(), spellNumber(), underThousand() (+7 more)

### Community 71 - "PdfMarginSettings"
Cohesion: 0.19
Nodes (12): excluding(), NarrationPipeline, MarginOverlay(), PdfMarginSheet(), buildPdfNarrationPlan(), layoutLines(), PdfLine, PdfMargins (+4 more)

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 94 - "Pass"
Cohesion: 0.22
Nodes (7): matchPdfWords(), ReaderRow, Pass, selectSpokenWord(), WordSelection, ReaderUpdateTest, ReaderWordSelectionTest

### Community 99 - "PdfPageImage"
Cohesion: 0.31
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 100 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 102 - "Updates"
Cohesion: 0.23
Nodes (3): Release, UpdateCheckException, Updates

### Community 104 - "SynthesizedAudio"
Cohesion: 0.53
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 107 - "PdfContentKind"
Cohesion: 0.29
Nodes (7): PdfContentKind, DECORATION, FOOTER, FOOTNOTE, HEADER, MARGIN, TABLE

### Community 108 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

### Community 110 - "pdfWordOutsideViewport"
Cohesion: 0.38
Nodes (3): isInLibraryFolder(), pdfWordOutsideViewport(), ReaderFolderRegressionTest

## Knowledge Gaps
- **6 isolated node(s):** `DECORATION`, `FOOTER`, `FOOTNOTE`, `HEADER`, `MARGIN` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 398 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **67 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `.play`, `ReadingTimePresentation.kt`, `ListeningScreens.kt`, `PdfMarginSettings`, `Alur kode`, `ReadingDocument`, `PreparedSpeech`, `ReaderVoiceParityTest.kt`?**
  _High betweenness centrality (0.123) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DECORATION`, `FOOTER`, `FOOTNOTE` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.08961593172119488 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `ReadingService`, `DocumentImporter`, `Library`, `Updates`?**
  _High betweenness centrality (0.092) - this node is a cross-community bridge._
- **Are the 29 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 29 INFERRED edges - model-reasoned connections that need verification._
- **Should `Reader.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.1422924901185771 - nodes in this community are weakly interconnected._