# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 117 files · ~84,137 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1545 nodes · 3748 edges · 122 communities (49 shown, 73 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 424 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `b9dca213`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .play
- Reader.kt
- PdfNarration.kt
- ListeningScreens.kt
- LibraryV2
- Design System: Tuturium
- TuturiumMenus.kt
- steps
- Tuturium
- with
- LibraryV2FlowTest
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- NarrationPipeline
- BookActions
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- PreparedSpeech
- Benchmark Supertonic 3 · Tuturium 1.3.12
- PdfWordSelection.kt
- SynthesizedAudio
- Pemrosesan teks untuk narasi Tuturium
- OriginalPdfPages.kt
- TuturiumCloud
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- ReaderPlaybackBar
- ReadingDocument
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- ReaderSheetsV2.kt
- profile-android.sh
- update-graph.sh
- DocumentImporter
- NarrationOpus.kt
- GoogleUpdateRegressionTest
- PdfPageImage
- ReadingService
- DocumentImportTest.kt
- Supertonic adaptif · Tuturium 1.3.17
- ListeningStats.kt
- README.md
- Test
- .highRatesPlayEveryQueuedPassThroughToItsLastCue
- TuturiumAccountScreen.kt
- SettingsScreen.kt
- countDocumentWords
- ListeningOptions
- PdfContentKind
- .write
- ReaderVoiceParityTest.kt
- .findsNearestWordOnSameLineButNotAcrossPage
- Book
- Pass
- DocumentImportTest
- ReadingTimeline.kt
- LibraryControlsTest
- Color
- StorageScreenV2.kt
- formatPlaybackTime
- Updates.kt
- Writer
- PdfNarration.kt
- Context
- Rujukan audit transisi dan kelancaran Tuturium
- isInLibraryFolder
- .highlightUsesOnlyCurrentPassAndIntersectingWords
- UtteranceProgressListener

## God Nodes (most connected - your core abstractions)
1. `ReadingDocument` - 57 edges
2. `ReadingService` - 57 edges
3. `Section` - 37 edges
4. `ListeningOptions` - 35 edges
5. `Library` - 34 edges
6. `ReaderContent()` - 33 edges
7. `LibraryV2()` - 32 edges
8. `Book` - 28 edges
9. `PreparedSpeech` - 25 edges
10. `Tuturium()` - 24 edges

## Surprising Connections (you probably didn't know these)
- `Implementasi 1.3.24` --references--> `NarrationPipeline`  [INFERRED]
  docs/narration-text-processing.md → app/src/main/java/id/tuturium/app/NarrationPipeline.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `NarrationPipeline`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/NarrationPipeline.kt

## Import Cycles
- None detected.

## Communities (122 total, 73 thin omitted)

### Community 0 - ".play"
Cohesion: 0.10
Nodes (5): OfflineAudio, Clip, Listener, OfflinePass, PlaybackMetrics

### Community 1 - "Reader.kt"
Cohesion: 0.09
Nodes (5): AppearanceChoice(), AppearanceSwitch(), ReaderAppearanceSheet(), readerCursorColor(), readerTypeface()

### Community 2 - "PdfNarration.kt"
Cohesion: 0.26
Nodes (4): PdfContentSpan, PdfNarrationPlan, PdfNarrationStore, Key

### Community 3 - "ListeningScreens.kt"
Cohesion: 0.21
Nodes (15): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+7 more)

### Community 4 - "LibraryV2"
Cohesion: 0.44
Nodes (3): FolderStore, LibraryV2(), ModernLibrary()

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "TuturiumMenus.kt"
Cohesion: 0.13
Nodes (8): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), MenuFrameRate(), TuturiumDialog(), TuturiumMenu()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.13
Nodes (15): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+7 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 12 - "SupertonicBuffer"
Cohesion: 0.20
Nodes (5): SupertonicBuffer, synthesisThreadCount(), findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.18
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+8 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.29
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 15 - "NarrationPipeline"
Cohesion: 0.20
Nodes (11): excluding(), NarrationPipeline, buildPdfNarrationPlan(), layoutLines(), PdfLine, PdfMargins, PdfMarginSettings, alignPdfHits() (+3 more)

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 22 - "PreparedSpeech"
Cohesion: 0.20
Nodes (4): MediaReadingCaption, Cue, PreparedSpeech, MediaReadingCaptionTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "PdfWordSelection.kt"
Cohesion: 0.15
Nodes (5): PDFTextStripper, pdfKey(), PdfWordStripper, readPdfWordHits(), readPdfWords()

### Community 26 - "SynthesizedAudio"
Cohesion: 0.28
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 27 - "Pemrosesan teks untuk narasi Tuturium"
Cohesion: 0.29
Nodes (7): ICU, CLDR, dan Revo Norm, Implementasi 1.3.24, Pemrosesan teks untuk narasi Tuturium, Penerapan pada pembaca offline, Pengujian host, Rujukan pembaca, Verifikasi Android dan pembaruan

### Community 29 - "TuturiumCloud"
Cohesion: 0.05
Nodes (16): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), driveAuthorization(), googleAccessMessage(), isAccountReauthFailure(), MainActivity, CloudAccessException (+8 more)

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.25
Nodes (6): OfflineVoiceRuntimeTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ReaderPlaybackBar"
Cohesion: 0.12
Nodes (13): newReaderPace(), formatPlaybackSpeed(), playbackTimeCaption(), ReaderPlaybackBar(), NarrationPaceSnapshot, PaceProfile, PaceSample, ReadingPace (+5 more)

### Community 36 - "ReadingDocument"
Cohesion: 0.14
Nodes (5): DriveArchiveTest, DriveArchive, RestoreBook, ReadingDocument, Section

### Community 40 - "release-notes.md"
Cohesion: 0.20
Nodes (9): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23, Tuturium 1.3.24 (+1 more)

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 46 - "ReaderSheetsV2.kt"
Cohesion: 0.19
Nodes (15): MarginOverlay(), PdfMarginSheet(), Reader(), ReaderContent(), ReaderOptions(), ReaderSearchBar(), RenameBookSheet(), ReaderMenuAction() (+7 more)

### Community 55 - "PdfPageImage"
Cohesion: 0.26
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 57 - "ReadingService"
Cohesion: 0.05
Nodes (13): narrationBoostMb(), UtteranceAudioLength, availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService (+5 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 67 - "ListeningStats.kt"
Cohesion: 0.10
Nodes (15): clockWords(), dateWords(), indonesianNumberReplacements(), romanWords(), spellDigits(), spellInteger(), spellNumber(), underThousand() (+7 more)

### Community 71 - "Test"
Cohesion: 0.21
Nodes (3): ReadingTimeline, IndonesianNormalizationTest, ReadingTimelineTest

### Community 78 - "TuturiumAccountScreen.kt"
Cohesion: 0.11
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.35
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "countDocumentWords"
Cohesion: 0.36
Nodes (3): countDocumentWords(), sortBooks(), LibraryPresentationTest

### Community 82 - "ListeningOptions"
Cohesion: 0.21
Nodes (7): forPass(), ListeningOptions, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 86 - "PdfContentKind"
Cohesion: 0.29
Nodes (7): PdfContentKind, DECORATION, FOOTER, FOOTNOTE, HEADER, MARGIN, TABLE

### Community 87 - ".write"
Cohesion: 0.24
Nodes (5): NarrationAudioCache, AudioCue, OfflineSynth, SynthesizedAudio, OfflineVoice

### Community 88 - "ReaderVoiceParityTest.kt"
Cohesion: 0.11
Nodes (3): PlaybackNoCrashTest, PlaybackTest, ReaderVoiceParityTest

### Community 91 - "Book"
Cohesion: 0.19
Nodes (10): Book, bookRows(), FileArtwork(), FolderEditorV2(), PinnedBookTitle(), SheetAction(), readingTimeline(), estimatedReadingSpeed() (+2 more)

### Community 94 - "Pass"
Cohesion: 0.09
Nodes (24): matchPdfWords(), buildReaderLayout(), ReaderLayout, ReaderRow, chooseVoice(), firstPassByPdfPage(), googleIndonesianVoices(), indonesianVoiceLabel() (+16 more)

### Community 100 - "StorageScreenV2.kt"
Cohesion: 0.22
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 101 - "formatPlaybackTime"
Cohesion: 0.31
Nodes (4): matchesLibraryFilter(), readerCenterDelta(), formatPlaybackTime(), ReaderReturnRegressionTest

### Community 102 - "Updates.kt"
Cohesion: 0.19
Nodes (3): Release, UpdateCheckException, Updates

### Community 110 - "Context"
Cohesion: 0.19
Nodes (6): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, PdfMarginStore

### Community 111 - "Rujukan audit transisi dan kelancaran Tuturium"
Cohesion: 0.40
Nodes (4): Cara mengukur dan kriteria penerimaan, Pola dari produk dan panduan platform, Rujukan audit transisi dan kelancaran Tuturium, Titik audit di Tuturium

### Community 113 - "isInLibraryFolder"
Cohesion: 0.38
Nodes (3): isInLibraryFolder(), pdfWordOutsideViewport(), ReaderFolderRegressionTest

## Knowledge Gaps
- **6 isolated node(s):** `DECORATION`, `FOOTER`, `FOOTNOTE`, `HEADER`, `MARGIN` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 406 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **73 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Alur kode` connect `Context` to `PdfNarration.kt`, `Updates.kt`, `NarrationPipeline`, `DocumentImporter`, `Library`, `ReadingService`?**
  _High betweenness centrality (0.140) - this node is a cross-community bridge._
- **Are the 34 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.invalidLaterBookDoesNotRestoreEarlierBookOrApplyDeletion()`) actually correct?**
  _`ReadingDocument` has 34 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DECORATION`, `FOOTER`, `FOOTNOTE` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.09982174688057041 - nodes in this community are weakly interconnected._
- **Why does `ReadingDocument` connect `ReadingDocument` to `PdfNarration.kt`, `ReaderPlaybackBar`, `LibraryV2`, `Test`, `ReaderSheetsV2.kt`, `NarrationPipeline`, `ListeningOptions`, `Library`, `DocumentImporter`, `.write`, `ReadingService`, `DocumentImportTest.kt`, `Book`, `Pass`?**
  _High betweenness centrality (0.101) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `Reader.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09359605911330049 - nodes in this community are weakly interconnected._