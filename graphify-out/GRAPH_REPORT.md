# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 97 files · ~63,539 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1277 nodes · 2929 edges · 100 communities (34 shown, 66 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 289 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `33b32a54`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ReadingService
- Updates
- LibraryV2
- ListeningScreens.kt
- Design System: Tuturium
- Tuturium.kt
- steps
- Tuturium
- with
- TuturiumCloud
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- GoogleAccountAccess.kt
- Reader.kt
- with
- benchmark-adaptive-supertonic.py
- TuturiumAccountScreen.kt
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- Pass
- Test
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- .install
- Audit Google dan pembaruan Tuturium 1.3.6
- LibraryV2FlowTest
- LongPdfImportTest.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderContent
- AndroidJUnit4
- profile-android.sh
- update-graph.sh
- Theme.kt
- file
- Library
- ReadingService.kt
- Supertonic adaptif · Tuturium 1.3.14
- .play
- isCompatibleCloudAccount
- ListeningStats.kt
- OfflineAudio.kt
- .write
- LibraryScreenV2.kt
- NarrationOpus.kt
- narrationGain
- DocumentImporter
- SettingsScreen.kt
- DriveArchive
- OfflineSynth
- ReadingDocument
- GoogleUpdateRegressionTest
- isMatchingDriveAccount
- Alur kode
- UtteranceProgressListener
- .endPage
- Test
- AutomaticSpeed

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 48 edges
2. `ReaderContent()` - 32 edges
3. `Library` - 31 edges
4. `LibraryV2()` - 30 edges
5. `ReadingDocument` - 28 edges
6. `Tuturium()` - 23 edges
7. `TuturiumCloud` - 23 edges
8. `Pass` - 22 edges
9. `Book` - 21 edges
10. `VoiceChoice` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `LibraryV2()` --calls--> `sortBooks()`  [INFERRED]
  app/src/main/java/id/tuturium/app/LibraryScreenV2.kt → app/src/main/java/id/tuturium/app/LibraryPresentation.kt

## Import Cycles
- None detected.

## Communities (100 total, 66 thin omitted)

### Community 0 - "ReadingService"
Cohesion: 0.16
Nodes (5): narrationBoostMb(), OfflineVoice, ReadingService, OfflineSession, Callback

### Community 1 - "Updates"
Cohesion: 0.23
Nodes (3): Release, UpdateCheckException, Updates

### Community 3 - "LibraryV2"
Cohesion: 0.19
Nodes (7): FolderStore, BookActions, FolderEditorV2(), LibraryV2(), SheetAction(), SheetHeadline(), ModernLibrary()

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.10
Nodes (28): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+20 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "Tuturium.kt"
Cohesion: 0.15
Nodes (3): ModernUpdateMenu(), PasteDialog(), Tuturium()

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

### Community 12 - "SupertonicBuffer"
Cohesion: 0.20
Nodes (4): SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.25
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
Cohesion: 0.13
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.15
Nodes (10): countDocumentWords(), forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), seekBySeconds() (+2 more)

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "Pass"
Cohesion: 0.07
Nodes (19): OriginalPdfPages(), PdfPageImage(), PdfPageSession, matchPdfWords(), nearestPdfWord(), ocrPdfWordHits(), pdfHighlightWords(), pdfKey() (+11 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.22
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.31
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".install"
Cohesion: 0.30
Nodes (5): refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 45 - "ReaderContent"
Cohesion: 0.05
Nodes (36): Book, estimateRemainingMinutes(), matchesLibraryFilter(), sortBooks(), bookRows(), PinnedBookTitle(), Reader(), readerCenterDelta() (+28 more)

### Community 57 - "ReadingService.kt"
Cohesion: 0.08
Nodes (4): availableVoices(), choice(), indonesianCandidates(), offlineVoices()

### Community 60 - "Supertonic adaptif · Tuturium 1.3.14"
Cohesion: 0.40
Nodes (5): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17

### Community 63 - ".play"
Cohesion: 0.30
Nodes (4): OfflineVoiceRuntimeTest, OfflineAudio, Clip, OfflinePass

### Community 67 - "ListeningStats.kt"
Cohesion: 0.20
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 71 - ".write"
Cohesion: 0.45
Nodes (4): NarrationAudioCache, AudioCue, synthesisRanges(), SynthesizedAudio

### Community 77 - "narrationGain"
Cohesion: 0.28
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.21
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 84 - "ReadingDocument"
Cohesion: 0.19
Nodes (4): LibraryControlsTest, ReadingDocument, Section, ReadingSettings

### Community 87 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

## Knowledge Gaps
- **66 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `ListeningScreens.kt`, `ReaderContent`, `ReadingDocument`, `ListeningOptions`, `Alur kode`, `file`, `ReadingService.kt`, `AutomaticSpeed`, `.play`?**
  _High betweenness centrality (0.100) - this node is a cross-community bridge._
- **Are the 4 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 4 INFERRED edges - model-reasoned connections that need verification._
- **Should `ListeningScreens.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09724238026124818 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `ReadingService`, `Updates`, `DocumentImporter`, `Library`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **Are the 19 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 19 INFERRED edges - model-reasoned connections that need verification._
- **Should `steps` be split into smaller, more focused modules?**
  _Cohesion score 0.05555555555555555 - nodes in this community are weakly interconnected._
- **Are the 7 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 7 INFERRED edges - model-reasoned connections that need verification._