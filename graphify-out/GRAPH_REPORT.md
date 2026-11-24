# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 83 files · ~50,675 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1049 nodes · 2358 edges · 78 communities (24 shown, 54 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 209 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `45c30e79`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OfflineAudio.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- LibraryV2
- steps
- Tuturium
- with
- TuturiumCloud
- .write
- ModernScreens.kt
- Reader.kt
- GoogleAccountAccess.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- ReaderMenuSheet
- ReaderVoiceParityTest.kt
- TuturiumMenus.kt
- Test
- StorageScreenV2.kt
- Reader.kt
- file
- SettingsScreen.kt
- LibraryV2FlowTest
- PdfSourceStorageTest.kt
- MainActivity
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- color
- profile-android.sh
- update-graph.sh
- DriveArchive
- UtteranceProgressListener
- OfflineVoiceRuntimeTest.kt
- isCompatibleCloudAccount
- GoogleAccessRegressionTest

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 45 edges
2. `Library` - 31 edges
3. `LibraryV2()` - 29 edges
4. `ReadingDocument` - 24 edges
5. `ReaderContent()` - 24 edges
6. `Tuturium()` - 23 edges
7. `Book` - 19 edges
8. `VoiceChoice` - 19 edges
9. `ListeningOptions` - 17 edges
10. `Section` - 16 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `ReadingService` --calls--> `OfflineAudio`  [INFERRED]
  app/src/main/java/id/tuturium/app/ReadingService.kt → app/src/main/java/id/tuturium/app/OfflineAudio.kt

## Import Cycles
- None detected.

## Communities (78 total, 54 thin omitted)

### Community 0 - "OfflineAudio.kt"
Cohesion: 0.07
Nodes (11): OfflineAudio, Clip, Listener, OfflinePass, forPass(), ListeningOptions, PreparedSpeech, prepareSpeech() (+3 more)

### Community 1 - "Updates.kt"
Cohesion: 0.06
Nodes (9): GoogleUpdateRegressionTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore, Release, UpdateCheckException (+1 more)

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.06
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.07
Nodes (10): Book, Bookmark, Library, countDocumentWords(), matchesLibraryFilter(), readerCenterDelta(), formatPlaybackTime(), ReaderPlaybackBar() (+2 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.10
Nodes (28): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+20 more)

### Community 5 - "LibraryV2"
Cohesion: 0.10
Nodes (14): LibraryControlsTest, FolderStore, BookActions, estimateRemainingMinutes(), sortBooks(), bookRows(), FileArtwork(), FolderEditorV2() (+6 more)

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.05
Nodes (36): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan (+28 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.23
Nodes (3): isMatchingDriveAccount(), TuturiumCloud, TuturiumCloudState

### Community 12 - ".write"
Cohesion: 0.18
Nodes (5): AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio, OfflineVoiceCatalogTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (15): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+7 more)

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "ListeningStats.kt"
Cohesion: 0.17
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "ReadingService.kt"
Cohesion: 0.06
Nodes (15): DriveArchive, OfflineVoice, ReaderTimerSheet(), ReadingDocument, Section, availableVoices(), choice(), indonesianCandidates() (+7 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.13
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ReaderMenuSheet"
Cohesion: 0.39
Nodes (5): ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet(), ReaderVoiceSheet()

### Community 25 - "TuturiumMenus.kt"
Cohesion: 0.12
Nodes (9): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), MenuFrameRate(), TuturiumDialog(), TuturiumMenu() (+1 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.29
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 28 - "Reader.kt"
Cohesion: 0.14
Nodes (4): Reader(), ReaderContent(), ReaderOptions(), ReaderRow

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "SettingsScreen.kt"
Cohesion: 0.23
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 54 - "DriveArchive"
Cohesion: 0.36
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, PlaybackTest

## Knowledge Gaps
- **54 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `Tuturium`, `OfflineAudio.kt`, `ListeningScreens.kt`, `ReaderVoiceParityTest.kt`?**
  _High betweenness centrality (0.106) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `OfflineAudio.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0746606334841629 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.060285563194077206 - nodes in this community are weakly interconnected._
- **Are the 12 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 12 INFERRED edges - model-reasoned connections that need verification._