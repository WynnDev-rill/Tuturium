# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 101 files · ~67,391 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1321 nodes · 3055 edges · 103 communities (42 shown, 61 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 316 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5bef3003`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ReadingService
- Updates
- DocumentImportTest.kt
- LibraryV2
- ListeningScreens.kt
- Design System: Tuturium
- Tuturium.kt
- steps
- Tuturium
- with
- TuturiumCloud
- OfflineAudio.kt
- ModernScreens.kt
- Riset Tuturium
- GoogleAccountAccess.kt
- Reader.kt
- with
- benchmark-adaptive-supertonic.py
- TuturiumAccountScreen.kt
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- PdfPageImage
- Test
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- .install
- Audit Google dan pembaruan Tuturium 1.3.6
- LibraryV2FlowTest
- instrumentationregistry
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- TuturiumMenus.kt
- AndroidJUnit4
- profile-android.sh
- update-graph.sh
- Theme.kt
- file
- Library
- ReadingService.kt
- Supertonic adaptif · Tuturium 1.3.14
- ReaderActions.kt
- PdfWordSelection.kt
- isCompatibleCloudAccount
- ListeningStats.kt
- Updates.kt
- README.md
- PdfWordHit
- .write
- ReaderSheetsV2.kt
- NarrationOpus.kt
- narrationGain
- DocumentImporter
- SettingsScreen.kt
- Pass
- .onCreate
- OfflineSynth
- ReadingDocument
- GoogleUpdateRegressionTest
- isMatchingDriveAccount
- Alur kode
- UtteranceProgressListener
- .endPage
- Test
- AutomaticSpeed
- NarrationFlow.kt
- Test
- .mapsRepeatedPdfWordsInReadingOrder

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 48 edges
2. `ReaderContent()` - 33 edges
3. `ReadingDocument` - 33 edges
4. `LibraryV2()` - 32 edges
5. `Library` - 31 edges
6. `Tuturium()` - 24 edges
7. `Section` - 24 edges
8. `TuturiumCloud` - 23 edges
9. `Pass` - 22 edges
10. `ListeningOptions` - 21 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `bookRows()` --calls--> `estimateRemainingMinutes()`  [INFERRED]
  app/src/main/java/id/tuturium/app/LibraryScreenV2.kt → app/src/main/java/id/tuturium/app/LibraryPresentation.kt

## Import Cycles
- None detected.

## Communities (103 total, 61 thin omitted)

### Community 0 - "ReadingService"
Cohesion: 0.24
Nodes (4): narrationBoostMb(), OfflineVoice, ReadingService, OfflineSession

### Community 3 - "LibraryV2"
Cohesion: 0.11
Nodes (12): FolderStore, BookActions, isInLibraryFolder(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2(), PinnedBookTitle() (+4 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.09
Nodes (28): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+20 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "Tuturium.kt"
Cohesion: 0.15
Nodes (5): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.15
Nodes (13): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+5 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.33
Nodes (3): TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.08
Nodes (9): OfflineAudio, Clip, Listener, OfflinePass, SupertonicBuffer, findReaderMatches(), ReaderMatch, PreparedSpeech (+1 more)

### Community 13 - "ModernScreens.kt"
Cohesion: 0.18
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+8 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.29
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.13
Nodes (8): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), main()

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.11
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.18
Nodes (7): forPass(), ListeningOptions, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "PdfPageImage"
Cohesion: 0.26
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".install"
Cohesion: 0.20
Nodes (5): refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 40 - "release-notes.md"
Cohesion: 0.50
Nodes (3): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19

### Community 45 - "TuturiumMenus.kt"
Cohesion: 0.16
Nodes (4): MenuFrameRate(), TuturiumDialog(), TuturiumMenu(), TuturiumSheet()

### Community 46 - "AndroidJUnit4"
Cohesion: 0.33
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, DriveArchive

### Community 54 - "file"
Cohesion: 0.09
Nodes (3): OfflineVoiceRuntimeTest, PlaybackTest, ReaderVoiceParityTest

### Community 55 - "Library"
Cohesion: 0.06
Nodes (13): Book, Bookmark, Library, countDocumentWords(), matchesLibraryFilter(), sortBooks(), readerCenterDelta(), formatPlaybackSpeed() (+5 more)

### Community 57 - "ReadingService.kt"
Cohesion: 0.09
Nodes (4): availableVoices(), choice(), indonesianCandidates(), offlineVoices()

### Community 60 - "Supertonic adaptif · Tuturium 1.3.14"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 62 - "ReaderActions.kt"
Cohesion: 0.13
Nodes (12): Reader(), ReaderContent(), ReaderOptions(), AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl(), ReaderAppearanceSheet(), ReaderAudioSheet() (+4 more)

### Community 63 - "PdfWordSelection.kt"
Cohesion: 0.19
Nodes (5): matchPdfWords(), ocrPdfWordHits(), pdfKey(), readPdfWordHits(), PDFTextStripper

### Community 67 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 70 - "PdfWordHit"
Cohesion: 0.19
Nodes (6): estimateRemainingMinutes(), nearestPdfWord(), pdfHighlightWords(), PdfWordHit, ReaderAudioSelectionTest, ReaderLibraryUpdateTest

### Community 71 - ".write"
Cohesion: 0.35
Nodes (5): NarrationAudioCache, AudioCue, synthesisRanges(), synthesisThreadCount(), SynthesizedAudio

### Community 74 - "ReaderSheetsV2.kt"
Cohesion: 0.25
Nodes (7): ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet(), ReaderQueueSheet(), ReaderTimerSheet(), ReaderVoiceSheet()

### Community 77 - "narrationGain"
Cohesion: 0.17
Nodes (4): amplifyNarration(), narrationGain(), spokenPcmRange(), NarrationAudioTest

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.25
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "Pass"
Cohesion: 0.44
Nodes (4): Pass, selectSpokenWord(), WordSelection, ReaderWordSelectionTest

### Community 84 - "ReadingDocument"
Cohesion: 0.24
Nodes (3): ReadingDocument, Section, ReadingSettings

### Community 87 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

### Community 100 - "NarrationFlow.kt"
Cohesion: 0.39
Nodes (8): clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter(), normalizedForNarration(), pauseAfter(), withoutClosingPunctuation()

## Knowledge Gaps
- **61 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `LibraryV2`, `ListeningScreens.kt`, `OfflineAudio.kt`, `.onCreate`, `ReadingDocument`, `ListeningOptions`, `Alur kode`, `file`, `ReadingService.kt`, `AutomaticSpeed`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImportTest.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11231884057971014 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `ReadingService`, `Updates`, `DocumentImporter`, `Library`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `.add()` and `SkipOptionsSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._
- **Should `LibraryV2` be split into smaller, more focused modules?**
  _Cohesion score 0.11447811447811448 - nodes in this community are weakly interconnected._
- **Why does `ReadingDocument` connect `ReadingDocument` to `ReadingService`, `LibraryV2`, `ListeningScreens.kt`, `Test`, `OfflineAudio.kt`, `DocumentImporter`, `AndroidJUnit4`, `Pass`, `OfflineSynth`, `ListeningOptions`, `Library`, `ReaderActions.kt`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._