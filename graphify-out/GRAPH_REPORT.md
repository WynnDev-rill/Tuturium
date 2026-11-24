# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 84 files · ~52,105 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1083 nodes · 2454 edges · 75 communities (23 shown, 52 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 221 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `9a208acd`
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
- GoogleAccountAccess.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- PlaybackTest.kt
- ReaderContent
- Test
- StorageScreenV2.kt
- file
- LibraryControlsTest
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
- .read
- file
- isCompatibleCloudAccount
- GoogleAccessRegressionTest

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
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `ReadingService` --calls--> `OfflineAudio`  [INFERRED]
  app/src/main/java/id/tuturium/app/ReadingService.kt → app/src/main/java/id/tuturium/app/OfflineAudio.kt

## Import Cycles
- None detected.

## Communities (75 total, 52 thin omitted)

### Community 0 - "OfflineAudio.kt"
Cohesion: 0.11
Nodes (12): ReaderRow, Pass, selectSpokenWord(), WordSelection, forPass(), ListeningOptions, PreparedSpeech, prepareSpeech() (+4 more)

### Community 1 - "Updates.kt"
Cohesion: 0.12
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.09
Nodes (4): Bookmark, Library, countDocumentWords(), seekBySeconds()

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.11
Nodes (27): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+19 more)

### Community 5 - "LibraryV2"
Cohesion: 0.12
Nodes (12): FolderStore, BookActions, estimateRemainingMinutes(), sortBooks(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2() (+4 more)

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
Cohesion: 0.31
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.05
Nodes (15): refreshOfflineModels(), OfflineAudio, Clip, Listener, OfflinePass, SupertonicBuffer, AudioCue, OfflineSynth (+7 more)

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 14 - "Tuturium.kt"
Cohesion: 0.11
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

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
Cohesion: 0.05
Nodes (12): UtteranceProgressListener, OfflineVoice, availableVoices(), choice(), indonesianCandidates(), offlineVoices(), ReadingService, OfflineSession (+4 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.11
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 25 - "ReaderContent"
Cohesion: 0.07
Nodes (26): Book, matchesLibraryFilter(), Reader(), readerCenterDelta(), ReaderContent(), ReaderOptions(), formatPlaybackTime(), ReaderPlaybackBar() (+18 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.32
Nodes (3): humanSize(), LocalStorage, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 46 - "ReadingDocument"
Cohesion: 0.23
Nodes (3): DriveArchive, ReadingDocument, Section

### Community 54 - "file"
Cohesion: 0.14
Nodes (4): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest, ReaderVoiceParityTest

## Knowledge Gaps
- **52 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `OfflineAudio.kt`, `ListeningScreens.kt`, `Tuturium`, `OfflineAudio.kt`, `ReadingDocument`, `file`, `PlaybackTest.kt`, `ReaderContent`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `OfflineAudio.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.10741971207087486 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.075) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11553030303030302 - nodes in this community are weakly interconnected._
- **Why does `Library` connect `Library` to `Tuturium`, `ReadingDocument`?**
  _High betweenness centrality (0.065) - this node is a cross-community bridge._