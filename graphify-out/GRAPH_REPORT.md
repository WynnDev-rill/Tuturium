# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 83 files · ~50,665 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1050 nodes · 2359 edges · 76 communities (25 shown, 51 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 209 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `e20c88c0`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- SpeechControls.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- LibraryV2
- steps
- Tuturium
- with
- OfflineVoiceRuntimeTest.kt
- MainActivity.kt
- OfflineAudio.kt
- ModernScreens.kt
- Reader.kt
- Tuturium.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- ReaderVoiceParityTest.kt
- TuturiumMenus.kt
- StorageScreenV2.kt
- .sync
- SettingsScreen.kt
- ReaderFlowTest.kt
- PdfSourceStorageTest.kt
- MainActivity
- TuturiumCloud
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- color
- GoogleAccountAccess.kt
- profile-android.sh
- update-graph.sh
- ReadingDocument
- UtteranceProgressListener
- AndroidJUnit4
- isCompatibleCloudAccount
- GoogleAccessRegressionTest
- .localSpeechSurvivesHomeAndRespondsToNotificationTransportControls
- LibraryControlsTest

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
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (76 total, 51 thin omitted)

### Community 0 - "SpeechControls.kt"
Cohesion: 0.10
Nodes (13): ReaderPlaybackBar(), Pass, selectSpokenWord(), WordSelection, forPass(), ListeningOptions, PreparedSpeech, prepareSpeech() (+5 more)

### Community 1 - "Updates.kt"
Cohesion: 0.10
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.07
Nodes (10): Book, Bookmark, Library, countDocumentWords(), matchesLibraryFilter(), sortBooks(), readerCenterDelta(), formatPlaybackTime() (+2 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.12
Nodes (25): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+17 more)

### Community 5 - "LibraryV2"
Cohesion: 0.08
Nodes (25): FolderStore, BookActions, estimateRemainingMinutes(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2(), SheetAction() (+17 more)

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.05
Nodes (36): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan (+28 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 11 - "MainActivity.kt"
Cohesion: 0.05
Nodes (4): OfflineAudio, Clip, Listener, OfflinePass

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.10
Nodes (11): refreshOfflineModels(), AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio, ModelFile, OfflineCatalog, OfflineModel (+3 more)

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 16 - "Tuturium.kt"
Cohesion: 0.15
Nodes (3): ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 17 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "ReadingService.kt"
Cohesion: 0.07
Nodes (8): availableVoices(), choice(), offlineVoices(), ReadingService, OfflineSession, Callback, ReadingSettings, rampedSpeed()

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.12
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 25 - "TuturiumMenus.kt"
Cohesion: 0.17
Nodes (3): MenuFrameRate(), TuturiumDialog(), TuturiumMenu()

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.22
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - ".sync"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 35 - "ReaderFlowTest.kt"
Cohesion: 0.08
Nodes (3): DocumentImportTest, LibraryV2FlowTest, ReaderFlowTest

### Community 38 - "TuturiumCloud"
Cohesion: 0.32
Nodes (3): isMatchingDriveAccount(), TuturiumCloud, TuturiumCloudState

### Community 46 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 54 - "ReadingDocument"
Cohesion: 0.29
Nodes (3): DriveArchive, ReadingDocument, Section

## Knowledge Gaps
- **51 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `SpeechControls.kt`, `ListeningScreens.kt`, `LibraryV2`, `Tuturium`, `MainActivity.kt`, `OfflineAudio.kt`, `ReadingDocument`, `ReaderVoiceParityTest.kt`?**
  _High betweenness centrality (0.133) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `SpeechControls.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.09855072463768116 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0990990990990991 - nodes in this community are weakly interconnected._
- **Are the 12 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 12 INFERRED edges - model-reasoned connections that need verification._