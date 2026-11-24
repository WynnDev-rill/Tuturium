# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 81 files · ~50,709 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1001 nodes · 2199 edges · 64 communities (23 shown, 41 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 172 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `90ce1ad6`
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
- MainActivity.kt
- OfflineSpeech.kt
- ModernScreens.kt
- Reader.kt
- Tuturium.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- .get
- ReaderVoiceParityTest.kt
- LibraryScreenV2.kt
- PlaybackTest.kt
- StorageScreenV2.kt
- ReadingDocument
- TuturiumCloud
- SettingsScreen.kt
- color
- Pass
- DriveArchive.kt
- isCompatibleCloudAccount
- PdfSourceStorageTest.kt
- ReaderPlaybackBar
- .get
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderVoiceParityTest.kt
- .aVoiceThatPreviewsAlsoStartsReaderPlayback
- profile-android.sh
- update-graph.sh
- OfflineVoiceRuntimeTest.kt
- ReadingService
- UtteranceProgressListener
- Playback

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
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (64 total, 41 thin omitted)

### Community 0 - "SpeechControls.kt"
Cohesion: 0.16
Nodes (8): forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 1 - "Updates.kt"
Cohesion: 0.11
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 3 - "Library"
Cohesion: 0.09
Nodes (4): Bookmark, Library, countDocumentWords(), seekBySeconds()

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.12
Nodes (22): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), VoiceLibraryContent() (+14 more)

### Community 5 - "LibraryV2"
Cohesion: 0.12
Nodes (12): FolderStore, BookActions, estimateRemainingMinutes(), sortBooks(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2() (+4 more)

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
Cohesion: 0.23
Nodes (12): Book, Reader(), ReaderContent(), ReaderOptions(), ReaderRow, ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle() (+4 more)

### Community 11 - "MainActivity.kt"
Cohesion: 0.06
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 12 - "OfflineSpeech.kt"
Cohesion: 0.08
Nodes (12): arboIpa(), AudioCue, OfflineSynth, Pcm, resolvePepet(), synthesisRanges(), ModelFile, OfflineCatalog (+4 more)

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 16 - "Tuturium.kt"
Cohesion: 0.14
Nodes (5): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium()

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
Cohesion: 0.12
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 28 - "ReadingDocument"
Cohesion: 0.29
Nodes (3): LibraryControlsTest, ReadingDocument, Section

### Community 29 - "TuturiumCloud"
Cohesion: 0.07
Nodes (13): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), driveAuthorization(), googleAccessMessage(), isAccountReauthFailure(), MainActivity, CloudAccessException (+5 more)

### Community 31 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 33 - "Pass"
Cohesion: 0.44
Nodes (4): Pass, selectSpokenWord(), WordSelection, ReaderWordSelectionTest

### Community 37 - "ReaderPlaybackBar"
Cohesion: 0.21
Nodes (5): matchesLibraryFilter(), readerCenterDelta(), formatPlaybackTime(), ReaderPlaybackBar(), ReaderReturnRegressionTest

## Knowledge Gaps
- **41 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `SpeechControls.kt`, `PlaybackTest.kt`, `ListeningScreens.kt`, `Tuturium`, `ReaderVoiceParityTest.kt`, `.aVoiceThatPreviewsAlsoStartsReaderPlayback`, `ReadingService.kt`, `.get`, `.notification`, `Playback`, `ReadingDocument`?**
  _High betweenness centrality (0.122) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11051693404634581 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.089) - this node is a cross-community bridge._
- **Are the 12 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 12 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImporter.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0636734693877551 - nodes in this community are weakly interconnected._
- **Should `Library` be split into smaller, more focused modules?**
  _Cohesion score 0.08846153846153847 - nodes in this community are weakly interconnected._