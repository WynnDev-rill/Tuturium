# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 116 files · ~82,340 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1527 nodes · 3687 edges · 122 communities (52 shown, 70 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 412 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `85c45046`
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
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- NarrationPipeline
- GoogleAccountAccess.kt
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- PreparedSpeech
- Benchmark Supertonic 3 · Tuturium 1.3.12
- file
- SynthesizedAudio
- Pemrosesan teks untuk narasi Tuturium
- OriginalPdfPages.kt
- file
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- ReaderPlaybackBar
- ReadingDocument
- MainActivity
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- ReaderSheetsV2.kt
- profile-android.sh
- update-graph.sh
- NarrationOpus.kt
- PlaybackTest.kt
- PdfPageImage
- ReadingService
- Supertonic adaptif · Tuturium 1.3.17
- isCompatibleCloudAccount
- ListeningStats.kt
- README.md
- Test
- TuturiumAccountScreen.kt
- SettingsScreen.kt
- VoiceChoice
- ListeningOptions
- isMatchingDriveAccount
- .write
- ReaderVoiceParityTest.kt
- ReaderContent
- ReadingTimePresentation.kt
- Pass
- DocumentImportTest
- ReadingTimeline
- DriveArchive
- Color
- StorageScreenV2.kt
- ReaderPlaybackBar
- DocumentImporter.kt
- Writer
- Context
- SynthesizedAudio
- LongPdfImportTest.kt
- isInLibraryFolder
- .highlightUsesOnlyCurrentPassAndIntersectingWords
- UtteranceAudioLength
- PdfSourceStorageTest.kt
- UtteranceProgressListener

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 57 edges
2. `ReadingDocument` - 53 edges
3. `ListeningOptions` - 35 edges
4. `Section` - 34 edges
5. `ReaderContent()` - 34 edges
6. `Library` - 33 edges
7. `LibraryV2()` - 32 edges
8. `Book` - 28 edges
9. `PreparedSpeech` - 25 edges
10. `TuturiumCloud` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Implementasi 1.3.24` --references--> `NarrationPipeline`  [INFERRED]
  docs/narration-text-processing.md → app/src/main/java/id/tuturium/app/NarrationPipeline.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `PdfNarrationPlan`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/PdfNarration.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt

## Import Cycles
- None detected.

## Communities (122 total, 70 thin omitted)

### Community 0 - ".play"
Cohesion: 0.10
Nodes (5): OfflineAudio, Clip, Listener, OfflinePass, PlaybackMetrics

### Community 1 - "Reader.kt"
Cohesion: 0.07
Nodes (3): toggleSelected(), rate(), readerTypeface()

### Community 2 - "PdfNarration.kt"
Cohesion: 0.13
Nodes (14): cells(), layoutLines(), PdfContentKind, DECORATION, FOOTER, FOOTNOTE, HEADER, MARGIN (+6 more)

### Community 3 - "ListeningScreens.kt"
Cohesion: 0.25
Nodes (13): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel(), VoiceLibraryContent() (+5 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.21
Nodes (8): chooseVoice(), firstPassByPdfPage(), isNewer(), pdfPageForPass(), pdfPageForSection(), splitSpeech(), versionParts(), ReadingModelTest

### Community 5 - "Design System: Tuturium"
Cohesion: 0.18
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

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
Cohesion: 0.24
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

### Community 15 - "NarrationPipeline"
Cohesion: 0.17
Nodes (8): excluding(), NarrationPipeline, MarginOverlay(), PdfMarginSheet(), buildPdfNarrationPlan(), PdfMargins, PdfMarginSettings, PdfNarrationTest

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "Tuturium.kt"
Cohesion: 0.14
Nodes (6): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), TuturiumDialog()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 20 - "Library"
Cohesion: 0.06
Nodes (11): Book, Bookmark, Library, BookActions, countDocumentWords(), matchesLibraryFilter(), sortBooks(), readerCenterDelta() (+3 more)

### Community 22 - "PreparedSpeech"
Cohesion: 0.20
Nodes (4): MediaReadingCaption, Cue, PreparedSpeech, MediaReadingCaptionTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "file"
Cohesion: 0.13
Nodes (10): alignPdfHits(), nearestPdfWord(), ocrPdfWordHits(), pdfHighlightWords(), pdfKey(), PdfWordHit, PdfWordStripper, readPdfWordHits() (+2 more)

### Community 26 - "SynthesizedAudio"
Cohesion: 0.21
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 27 - "Pemrosesan teks untuk narasi Tuturium"
Cohesion: 0.25
Nodes (7): ICU, CLDR, dan Revo Norm, Implementasi 1.3.24, Pemrosesan teks untuk narasi Tuturium, Penerapan pada pembaca offline, Pengujian host, Rujukan pembaca, Verifikasi Android dan pembaruan

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.23
Nodes (6): OfflineVoiceRuntimeTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ReaderPlaybackBar"
Cohesion: 0.24
Nodes (5): newReaderPace(), NarrationPaceSnapshot, PaceProfile, PaceSample, ReadingPace

### Community 36 - "ReadingDocument"
Cohesion: 0.06
Nodes (13): LibraryControlsTest, LibraryV2FlowTest, ReaderFlowTest, FolderStore, bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2() (+5 more)

### Community 40 - "release-notes.md"
Cohesion: 0.22
Nodes (8): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23, Tuturium 1.3.24

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 46 - "ReaderSheetsV2.kt"
Cohesion: 0.28
Nodes (8): RenameBookSheet(), ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet(), ReaderTimerSheet(), ReaderVoiceSheet(), TuturiumSheet()

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.16
Nodes (4): encodeNarrationOpus(), NarrationResampler, synthesisRanges(), synthesisThreadCount()

### Community 55 - "PdfPageImage"
Cohesion: 0.28
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 57 - "ReadingService"
Cohesion: 0.06
Nodes (12): narrationBoostMb(), availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService, OfflineSession (+4 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 67 - "ListeningStats.kt"
Cohesion: 0.11
Nodes (14): clockWords(), dateWords(), indonesianNumberReplacements(), romanWords(), spellDigits(), spellInteger(), spellNumber(), underThousand() (+6 more)

### Community 78 - "TuturiumAccountScreen.kt"
Cohesion: 0.16
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "VoiceChoice"
Cohesion: 0.37
Nodes (7): googleIndonesianVoices(), indonesianVoiceLabel(), isIndonesian(), resolveIndonesianVoice(), VoiceChoice, activateIndonesianVoice(), GoogleVoiceCatalogTest

### Community 82 - "ListeningOptions"
Cohesion: 0.21
Nodes (7): forPass(), ListeningOptions, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 90 - "ReaderContent"
Cohesion: 0.23
Nodes (9): Reader(), ReaderContent(), ReaderOptions(), ReaderRow, AppearanceChoice(), AppearanceSwitch(), ReaderAppearanceSheet(), readerCursorColor() (+1 more)

### Community 91 - "ReadingTimePresentation.kt"
Cohesion: 0.24
Nodes (6): estimatedReadingSpeed(), hasPosition(), libraryReadingLabel(), narrationMillisPerWord(), readingPosition(), readingSpeed()

### Community 94 - "Pass"
Cohesion: 0.26
Nodes (6): matchPdfWords(), Pass, selectSpokenWord(), WordSelection, ReaderUpdateTest, ReaderWordSelectionTest

### Community 97 - "ReadingTimeline"
Cohesion: 0.21
Nodes (5): documentWordEnds(), readingTimeline(), ReadingTimeline, timelineOptions(), ReadingTimelineTest

### Community 98 - "DriveArchive"
Cohesion: 0.29
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, DriveArchive

### Community 100 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 101 - "ReaderPlaybackBar"
Cohesion: 0.23
Nodes (8): SpeedControlSheet(), VerticalSpeedSlider(), formatPlaybackSpeed(), formatPlaybackTime(), playbackTimeCaption(), ReaderPlaybackBar(), ReadingPosition, seekBySeconds()

### Community 102 - "DocumentImporter.kt"
Cohesion: 0.05
Nodes (6): GoogleUpdateRegressionTest, DocumentImporter, PDFTextStripper, Release, UpdateCheckException, Updates

### Community 110 - "Context"
Cohesion: 0.29
Nodes (6): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, PdfMarginStore

### Community 111 - "SynthesizedAudio"
Cohesion: 0.53
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 113 - "isInLibraryFolder"
Cohesion: 0.38
Nodes (3): isInLibraryFolder(), pdfWordOutsideViewport(), ReaderFolderRegressionTest

## Knowledge Gaps
- **6 isolated node(s):** `DECORATION`, `FOOTER`, `FOOTNOTE`, `HEADER`, `MARGIN` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 404 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **70 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingDocument` connect `ReadingDocument` to `ReadingTimeline`, `DriveArchive`, `PdfNarration.kt`, `ListeningScreens.kt`, `ReaderPlaybackBar`, `DocumentImporter.kt`, `Test`, `SupertonicBuffer`, `NarrationPipeline`, `ListeningOptions`, `Library`, `.write`, `file`, `ReaderContent`, `ReadingTimePresentation.kt`, `Pass`, `ReadingService`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DECORATION`, `FOOTER`, `FOOTNOTE` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.10084033613445378 - nodes in this community are weakly interconnected._
- **Why does `ReadingService` connect `ReadingService` to `.play`, `ReaderPlaybackBar`, `ReadingDocument`, `Context`, `NarrationPipeline`, `VoiceChoice`, `ListeningOptions`, `PreparedSpeech`, `UtteranceAudioLength`, `PlaybackTest.kt`?**
  _High betweenness centrality (0.111) - this node is a cross-community bridge._
- **Are the 30 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 30 INFERRED edges - model-reasoned connections that need verification._
- **Should `Reader.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06708595387840671 - nodes in this community are weakly interconnected._