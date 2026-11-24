# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 84 files · ~52,122 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1083 nodes · 2454 edges · 82 communities (25 shown, 57 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 221 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c69d8155`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OfflineAudio.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- LibraryV2
- LibraryScreenV2.kt
- steps
- Tuturium
- with
- TuturiumCloud
- OfflineAudio.kt
- ModernScreens.kt
- Tuturium.kt
- TuturiumCloud.kt
- GoogleAccountAccess.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- LibraryControlsTest
- ReaderContent
- Test
- StorageScreenV2.kt
- file
- LibraryV2FlowTest
- PdfSourceStorageTest.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- color
- ReadingDocument
- profile-android.sh
- update-graph.sh
- file
- file
- isCompatibleCloudAccount
- GoogleAccessRegressionTest
- SettingsScreen.kt
- SupertonicBuffer
- OfflineAudio.kt
- AndroidJUnit4
- Test
- UtteranceProgressListener

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 45 edges
2. `Library` - 31 edges
3. `LibraryV2()` - 29 edges
4. `ReadingDocument` - 26 edges
5. `ReaderContent()` - 25 edges
6. `Tuturium()` - 23 edges
7. `TuturiumCloud` - 21 edges
8. `VoiceChoice` - 19 edges
9. `Book` - 19 edges
10. `Section` - 18 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `TuturiumAccountScreen()` --calls--> `TuturiumCloudState`  [INFERRED]
  app/src/main/java/id/tuturium/app/TuturiumAccountScreen.kt → app/src/main/java/id/tuturium/app/TuturiumCloud.kt

## Import Cycles
- None detected.

## Communities (82 total, 57 thin omitted)

### Community 0 - "OfflineAudio.kt"
Cohesion: 0.13
Nodes (12): Pass, selectSpokenWord(), WordSelection, forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), safePreparedSpeech() (+4 more)

### Community 1 - "Updates.kt"
Cohesion: 0.07
Nodes (9): GoogleUpdateRegressionTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore, Release, UpdateCheckException (+1 more)

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.09
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.06
Nodes (11): Book, Bookmark, Library, countDocumentWords(), estimateRemainingMinutes(), matchesLibraryFilter(), sortBooks(), bookRows() (+3 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.11
Nodes (28): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+20 more)

### Community 5 - "LibraryV2"
Cohesion: 0.18
Nodes (7): FolderStore, BookActions, FolderEditorV2(), LibraryV2(), SheetAction(), SheetHeadline(), ModernLibrary()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.05
Nodes (37): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan (+29 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.34
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.24
Nodes (4): OfflineAudio, Clip, OfflinePass, synthesisRanges()

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 14 - "Tuturium.kt"
Cohesion: 0.16
Nodes (6): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), TuturiumDialog()

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "ReadingService.kt"
Cohesion: 0.06
Nodes (10): OfflineVoice, availableVoices(), choice(), offlineVoices(), Playback, ReadingService, OfflineSession, Callback (+2 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.09
Nodes (5): formatPlaybackTime(), ReaderPlaybackBar(), AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 25 - "ReaderContent"
Cohesion: 0.10
Nodes (14): Reader(), ReaderContent(), ReaderOptions(), ReaderRow, ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet() (+6 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.22
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.21
Nodes (4): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException

### Community 46 - "ReadingDocument"
Cohesion: 0.27
Nodes (3): DriveArchive, ReadingDocument, Section

### Community 55 - "file"
Cohesion: 0.20
Nodes (3): AudioCue, OfflineSynth, SynthesizedAudio

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.21
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 85 - "AndroidJUnit4"
Cohesion: 0.43
Nodes (3): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest

## Knowledge Gaps
- **57 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `OfflineAudio.kt`, `ListeningScreens.kt`, `Tuturium`, `OfflineAudio.kt`, `ReadingDocument`, `file`?**
  _High betweenness centrality (0.120) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `OfflineAudio.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.12550607287449392 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.099) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0707070707070707 - nodes in this community are weakly interconnected._
- **Are the 14 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 14 INFERRED edges - model-reasoned connections that need verification._