# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 109 files · ~75,471 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1424 nodes · 3364 edges · 102 communities (37 shown, 65 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 362 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `beaf27d5`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OfflineAudio.kt
- LibraryScreenV2.kt
- DocumentImportTest.kt
- file
- ListeningScreens.kt
- Design System: Tuturium
- ReaderContent
- steps
- Tuturium
- with
- TuturiumCloud
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- GoogleAccountAccess.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- Pass
- LibraryV2FlowTest
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- Playback
- Theme.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- Reader.kt
- profile-android.sh
- update-graph.sh
- NarrationOpus.kt
- ReaderVoiceParityTest.kt
- LibraryV2
- ReadingService
- Supertonic adaptif · Tuturium 1.3.17
- Reader.kt
- isCompatibleCloudAccount
- ListeningStats.kt
- README.md
- DriveArchive
- .play
- Updates.kt
- SettingsScreen.kt
- ReadingDocument
- DocumentImporter.kt
- isMatchingDriveAccount
- .write
- UtteranceProgressListener
- narrationGain
- SynthesizedAudio
- LongPdfImportTest.kt

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 55 edges
2. `ReadingDocument` - 39 edges
3. `ReaderContent()` - 33 edges
4. `Library` - 32 edges
5. `LibraryV2()` - 32 edges
6. `Section` - 28 edges
7. `Book` - 28 edges
8. `ListeningOptions` - 27 edges
9. `TuturiumCloud` - 23 edges
10. `Tuturium()` - 23 edges

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

## Communities (102 total, 65 thin omitted)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.15
Nodes (15): chooseVoice(), firstPassByPdfPage(), googleIndonesianVoices(), indonesianVoiceLabel(), isIndonesian(), isNewer(), pdfPageForPass(), pdfPageForSection() (+7 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "ReaderContent"
Cohesion: 0.07
Nodes (23): Reader(), ReaderContent(), ReaderOptions(), ReaderRow, AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl(), ReaderAppearanceSheet() (+15 more)

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
Cohesion: 0.31
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "SupertonicBuffer"
Cohesion: 0.17
Nodes (5): SupertonicBuffer, synthesisThreadCount(), findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.14
Nodes (20): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+12 more)

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
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 20 - "Library"
Cohesion: 0.06
Nodes (14): Book, Bookmark, Library, countDocumentWords(), matchesLibraryFilter(), sortBooks(), readingTimeline(), readerCenterDelta() (+6 more)

### Community 22 - "ListeningOptions"
Cohesion: 0.06
Nodes (26): documentWordEnds(), MediaReadingCaption, Cue, clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter() (+18 more)

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "Pass"
Cohesion: 0.06
Nodes (19): OriginalPdfPages(), PdfPageImage(), PdfPageSession, matchPdfWords(), nearestPdfWord(), ocrPdfWordHits(), pdfHighlightWords(), pdfKey() (+11 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.18
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.26
Nodes (5): refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "Playback"
Cohesion: 0.09
Nodes (28): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+20 more)

### Community 40 - "release-notes.md"
Cohesion: 0.25
Nodes (7): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (10): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+2 more)

### Community 55 - "LibraryV2"
Cohesion: 0.12
Nodes (13): FolderStore, BookActions, isInLibraryFolder(), bookRows(), FolderEditorV2(), LibraryV2(), PinnedBookTitle(), SheetAction() (+5 more)

### Community 57 - "ReadingService"
Cohesion: 0.06
Nodes (11): narrationBoostMb(), availableVoices(), choice(), indonesianCandidates(), offlineVoices(), ReadingService, OfflineSession, Callback (+3 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 62 - "Reader.kt"
Cohesion: 0.20
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 67 - "ListeningStats.kt"
Cohesion: 0.20
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 71 - "DriveArchive"
Cohesion: 0.33
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, DriveArchive

### Community 74 - ".play"
Cohesion: 0.30
Nodes (4): OfflineVoiceRuntimeTest, OfflineAudio, OfflinePass, synthesisRanges()

### Community 78 - "Updates.kt"
Cohesion: 0.11
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "ReadingDocument"
Cohesion: 0.17
Nodes (3): PlaybackTest, ReadingDocument, Section

### Community 82 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (7): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, DocumentImporter, PDFTextStripper

### Community 100 - "narrationGain"
Cohesion: 0.28
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 104 - "SynthesizedAudio"
Cohesion: 0.61
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

## Knowledge Gaps
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `Playback`, `ListeningScreens.kt`, `.play`, `ReadingDocument`, `DocumentImporter.kt`, `ListeningOptions`, `ReaderVoiceParityTest.kt`, `Pass`?**
  _High betweenness centrality (0.138) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `OfflineAudio.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09782608695652174 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `DocumentImporter.kt` to `ReadingService`, `Library`, `Updates.kt`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Are the 24 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Should `LibraryScreenV2.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09782608695652174 - nodes in this community are weakly interconnected._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._