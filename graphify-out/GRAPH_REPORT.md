# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 81 files · ~50,912 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1006 nodes · 2212 edges · 72 communities (25 shown, 47 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 175 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0a85c69b`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- SpeechControls.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- LibraryV2
- ReaderFlowTest.kt
- steps
- Tuturium
- with
- ReaderContent
- OfflineSpeech.kt
- ModernScreens.kt
- Tuturium.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- PlaybackTest.kt
- LibraryScreenV2.kt
- DocumentImportTest
- StorageScreenV2.kt
- ReadingDocument
- .sync
- SettingsScreen.kt
- LibraryV2FlowTest
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
- OfflineVoiceRuntimeTest.kt
- UtteranceProgressListener
- .localSpeechSurvivesHomeAndRespondsToNotificationTransportControls
- isCompatibleCloudAccount
- GoogleAccessRegressionTest

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 40 edges
2. `Library` - 31 edges
3. `LibraryV2()` - 26 edges
4. `ReaderContent()` - 23 edges
5. `ReadingDocument` - 22 edges
6. `Tuturium()` - 20 edges
7. `Book` - 19 edges
8. `VoiceChoice` - 17 edges
9. `Section` - 16 edges
10. `TuturiumCloud` - 15 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `TuturiumAccountScreen()` --calls--> `TuturiumCloudState`  [INFERRED]
  app/src/main/java/id/tuturium/app/TuturiumAccountScreen.kt → app/src/main/java/id/tuturium/app/TuturiumCloud.kt

## Import Cycles
- None detected.

## Communities (72 total, 47 thin omitted)

### Community 0 - "SpeechControls.kt"
Cohesion: 0.14
Nodes (8): forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 1 - "Updates.kt"
Cohesion: 0.09
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.09
Nodes (4): Bookmark, Library, countDocumentWords(), seekBySeconds()

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.19
Nodes (18): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), VoiceLibraryContent() (+10 more)

### Community 5 - "LibraryV2"
Cohesion: 0.15
Nodes (8): LibraryControlsTest, FolderStore, BookActions, FolderEditorV2(), LibraryV2(), SheetAction(), SheetHeadline(), ModernLibrary()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.05
Nodes (35): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan (+27 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "ReaderContent"
Cohesion: 0.09
Nodes (22): Book, estimateRemainingMinutes(), matchesLibraryFilter(), sortBooks(), Reader(), readerCenterDelta(), ReaderContent(), ReaderOptions() (+14 more)

### Community 12 - "OfflineSpeech.kt"
Cohesion: 0.08
Nodes (12): arboIpa(), AudioCue, OfflineSynth, Pcm, resolvePepet(), synthesisRanges(), ModelFile, OfflineCatalog (+4 more)

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 16 - "Tuturium.kt"
Cohesion: 0.15
Nodes (3): ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 17 - "ListeningStats.kt"
Cohesion: 0.20
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "ReadingService.kt"
Cohesion: 0.07
Nodes (7): availableVoices(), choice(), indonesianCandidates(), offlineVoices(), ReadingService, Callback, ReadingSettings

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.13
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 25 - "LibraryScreenV2.kt"
Cohesion: 0.12
Nodes (3): bookRows(), FileArtwork(), toggleSelected()

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.23
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 28 - "ReadingDocument"
Cohesion: 0.11
Nodes (12): DriveArchive, ReaderRow, isNewer(), Pass, ReadingDocument, Section, selectSpokenWord(), splitSpeech() (+4 more)

### Community 29 - ".sync"
Cohesion: 0.19
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "SettingsScreen.kt"
Cohesion: 0.23
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 38 - "TuturiumCloud"
Cohesion: 0.36
Nodes (3): isMatchingDriveAccount(), TuturiumCloud, TuturiumCloudState

### Community 46 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 66 - ".localSpeechSurvivesHomeAndRespondsToNotificationTransportControls"
Cohesion: 0.43
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, PlaybackTest

## Knowledge Gaps
- **47 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `SpeechControls.kt`, `ListeningScreens.kt`, `Tuturium`, `ReaderContent`, `PlaybackTest.kt`, `ReadingDocument`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Should `SpeechControls.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.1425287356321839 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Are the 12 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 12 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0859465737514518 - nodes in this community are weakly interconnected._
- **Should `DocumentImporter.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.08367071524966262 - nodes in this community are weakly interconnected._