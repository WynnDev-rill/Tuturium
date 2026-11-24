# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 105 files · ~69,121 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1368 nodes · 3201 edges · 100 communities (35 shown, 65 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 337 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ecb97117`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DocumentImporter
- Updates
- DocumentImportTest.kt
- DriveArchive
- ListeningScreens.kt
- Design System: Tuturium
- OfflineVoices.kt
- steps
- Tuturium
- with
- TuturiumCloud
- MainActivity.kt
- OfflineAudio.kt
- ModernScreens.kt
- Riset Tuturium
- GoogleAccountAccess.kt
- ListeningScreens.kt
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
- NarrationOpus.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- LibraryV2FlowTest
- Theme.kt
- MainActivity
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderContent
- NarrationFlow.kt
- profile-android.sh
- update-graph.sh
- OfflineSynth
- file
- Library
- ReadingService
- README.md
- Reader.kt
- isCompatibleCloudAccount
- ListeningStats.kt
- .endPage
- narration-flow.md
- .install
- GoogleUpdateRegressionTest
- Updates.kt
- SettingsScreen.kt
- ReadingDocument
- narrationGain
- .write
- synthesisRanges
- Test
- isMatchingDriveAccount
- LibraryControlsTest
- UtteranceProgressListener
- Alur kode

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 50 edges
2. `ReadingDocument` - 38 edges
3. `ReaderContent()` - 33 edges
4. `Library` - 32 edges
5. `LibraryV2()` - 32 edges
6. `Section` - 27 edges
7. `ListeningOptions` - 26 edges
8. `Book` - 26 edges
9. `TuturiumCloud` - 23 edges
10. `Tuturium()` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `TuturiumAccountScreen()` --calls--> `TuturiumCloudState`  [INFERRED]
  app/src/main/java/id/tuturium/app/TuturiumAccountScreen.kt → app/src/main/java/id/tuturium/app/TuturiumCloud.kt

## Import Cycles
- None detected.

## Communities (100 total, 65 thin omitted)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.08
Nodes (34): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+26 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "OfflineVoices.kt"
Cohesion: 0.20
Nodes (3): ModelFile, OfflineCatalog, OfflineVoice

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
Nodes (5): hasPosition(), libraryReadingLabel(), narrationMillisPerWord(), readingPosition(), readingSpeed()

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.08
Nodes (11): OfflineVoiceRuntimeTest, narrationBoostMb(), OfflineAudio, Clip, Listener, OfflinePass, SupertonicBuffer, findReaderMatches() (+3 more)

### Community 13 - "ModernScreens.kt"
Cohesion: 0.15
Nodes (19): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+11 more)

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
Cohesion: 0.16
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.07
Nodes (16): documentWordEnds(), readingTimeline(), PaceProfile, PaceSample, ReadingPace, UtteranceAudioLength, ReadingTimeline, timelineOptions() (+8 more)

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "Pass"
Cohesion: 0.06
Nodes (22): isInLibraryFolder(), OriginalPdfPages(), PdfPageImage(), PdfPageSession, pdfWordOutsideViewport(), matchPdfWords(), nearestPdfWord(), ocrPdfWordHits() (+14 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 40 - "release-notes.md"
Cohesion: 0.40
Nodes (4): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20

### Community 45 - "ReaderContent"
Cohesion: 0.06
Nodes (32): FolderStore, bookRows(), FolderEditorV2(), LibraryV2(), PinnedBookTitle(), SheetAction(), ModernLibrary(), Reader() (+24 more)

### Community 46 - "NarrationFlow.kt"
Cohesion: 0.26
Nodes (9): clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter(), normalizedForNarration(), pauseAfter(), spokenPcmRange() (+1 more)

### Community 54 - "file"
Cohesion: 0.10
Nodes (4): DriveArchiveTest, PlaybackNoCrashTest, PlaybackTest, ReaderVoiceParityTest

### Community 55 - "Library"
Cohesion: 0.06
Nodes (12): Book, Bookmark, Library, BookActions, countDocumentWords(), matchesLibraryFilter(), sortBooks(), readerCenterDelta() (+4 more)

### Community 57 - "ReadingService"
Cohesion: 0.06
Nodes (6): choice(), ReadingService, OfflineSession, Callback, ReadingSettings, AutomaticSpeed

### Community 60 - "README.md"
Cohesion: 0.18
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 67 - "ListeningStats.kt"
Cohesion: 0.17
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 74 - ".install"
Cohesion: 0.44
Nodes (3): refreshOfflineModels(), OfflineModel, OfflineModelStore

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.25
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "ReadingDocument"
Cohesion: 0.23
Nodes (3): ReaderFlowTest, ReadingDocument, Section

### Community 82 - "narrationGain"
Cohesion: 0.29
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 83 - ".write"
Cohesion: 0.61
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 90 - "Alur kode"
Cohesion: 0.40
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

## Knowledge Gaps
- **65 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `ListeningScreens.kt`, `OfflineVoices.kt`, `OfflineAudio.kt`, `ReaderContent`, `ReadingDocument`, `ListeningOptions`, `file`, `Alur kode`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImportTest.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09359605911330049 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `DocumentImporter`, `ReadingService`, `Updates`, `Library`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Are the 23 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 23 INFERRED edges - model-reasoned connections that need verification._
- **Should `ListeningScreens.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.07688492063492064 - nodes in this community are weakly interconnected._
- **Why does `Tuturium` connect `Alur kode` to `README.md`?**
  _High betweenness centrality (0.080) - this node is a cross-community bridge._