# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 85 files · ~53,136 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1101 nodes · 2513 edges · 84 communities (28 shown, 56 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 233 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1d522be9`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- OfflineAudio.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- LibraryV2
- Reader.kt
- steps
- Tuturium
- with
- TuturiumCloud
- OfflineAudio.kt
- ModernScreens.kt
- Tuturium.kt
- GoogleAccountAccess.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- ReadingService
- .localSpeechSurvivesHomeAndRespondsToNotificationTransportControls
- ReaderContent
- Test
- StorageScreenV2.kt
- file
- LibraryV2FlowTest
- file
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
- OfflineVoices.kt
- .onCreate
- Callback
- isCompatibleCloudAccount
- GoogleAccessRegressionTest
- SettingsScreen.kt
- SupertonicBuffer
- OfflineAudio.kt
- UtteranceProgressListener

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 45 edges
2. `Library` - 31 edges
3. `LibraryV2()` - 30 edges
4. `ReaderContent()` - 29 edges
5. `ReadingDocument` - 27 edges
6. `TuturiumCloud` - 23 edges
7. `Tuturium()` - 22 edges
8. `Book` - 19 edges
9. `VoiceChoice` - 19 edges
10. `Section` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `LibraryV2()` --calls--> `matchesLibraryFilter()`  [INFERRED]
  app/src/main/java/id/tuturium/app/LibraryScreenV2.kt → app/src/main/java/id/tuturium/app/LibraryPresentation.kt

## Import Cycles
- None detected.

## Communities (84 total, 56 thin omitted)

### Community 0 - "OfflineAudio.kt"
Cohesion: 0.15
Nodes (8): forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 1 - "Updates.kt"
Cohesion: 0.09
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.06
Nodes (13): Book, Bookmark, Library, countDocumentWords(), estimateRemainingMinutes(), matchesLibraryFilter(), sortBooks(), readerCenterDelta() (+5 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.12
Nodes (27): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+19 more)

### Community 5 - "LibraryV2"
Cohesion: 0.08
Nodes (17): FolderStore, BookActions, FolderEditorV2(), LibraryV2(), SheetAction(), SheetHeadline(), ModernLibrary(), ReaderMenuAction() (+9 more)

### Community 6 - "Reader.kt"
Cohesion: 0.07
Nodes (3): bookRows(), FileArtwork(), toggleSelected()

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
Cohesion: 0.30
Nodes (3): TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.26
Nodes (3): OfflineAudio, Clip, OfflinePass

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
Cohesion: 0.08
Nodes (4): availableVoices(), choice(), indonesianCandidates(), offlineVoices()

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.13
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ReadingService"
Cohesion: 0.27
Nodes (3): Playback, ReadingService, OfflineSession

### Community 25 - "ReaderContent"
Cohesion: 0.17
Nodes (12): Reader(), ReaderContent(), ReaderOptions(), ReaderRow, ReaderAppearanceSheet(), ReaderAudioSheet(), ReaderSearchBar(), RenameBookSheet() (+4 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 35 - "LibraryV2FlowTest"
Cohesion: 0.17
Nodes (4): DriveArchiveTest, LibraryV2FlowTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest

### Community 46 - "ReadingDocument"
Cohesion: 0.22
Nodes (3): DriveArchive, ReadingDocument, Section

### Community 55 - "OfflineVoices.kt"
Cohesion: 0.10
Nodes (10): refreshOfflineModels(), AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio, ModelFile, OfflineCatalog, OfflineModel (+2 more)

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.25
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 82 - "SupertonicBuffer"
Cohesion: 0.26
Nodes (4): SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

## Knowledge Gaps
- **56 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `OfflineAudio.kt`, `ListeningScreens.kt`, `Tuturium`, `OfflineAudio.kt`, `ReadingDocument`, `ReadingService.kt`, `file`, `.localSpeechSurvivesHomeAndRespondsToNotificationTransportControls`, `.onCreate`?**
  _High betweenness centrality (0.109) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `OfflineAudio.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.14942528735632185 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `Updates.kt`, `DocumentImporter.kt`, `Library`, `ReadingService`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **Are the 7 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 7 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.08536585365853659 - nodes in this community are weakly interconnected._
- **Are the 18 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 18 INFERRED edges - model-reasoned connections that need verification._