# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 89 files · ~56,397 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1150 nodes · 2623 edges · 80 communities (27 shown, 53 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 250 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `dd6258a7`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ReadingService.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- TuturiumMenus.kt
- LibraryScreenV2.kt
- steps
- Tuturium
- with
- TuturiumCloud
- OfflineAudio.kt
- ModernScreens.kt
- LibraryV2
- GoogleAccountAccess.kt
- ListeningStats.kt
- with
- benchmark-supertonic.py
- TuturiumAccountScreen.kt
- ListeningOptions
- PlaybackTest.kt
- ReaderContent
- Test
- StorageScreenV2.kt
- Reader.kt
- file
- .write
- LibraryV2FlowTest
- instrumentationregistry
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderActions.kt
- ReadingDocument
- profile-android.sh
- update-graph.sh
- Theme.kt
- file
- LibraryControlsTest
- isCompatibleCloudAccount
- SettingsScreen.kt
- UtteranceProgressListener

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 46 edges
2. `ReaderContent()` - 33 edges
3. `Library` - 31 edges
4. `LibraryV2()` - 30 edges
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
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (80 total, 53 thin omitted)

### Community 0 - "ReadingService.kt"
Cohesion: 0.07
Nodes (11): OfflineVoice, availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService, OfflineSession (+3 more)

### Community 1 - "Updates.kt"
Cohesion: 0.12
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.06
Nodes (12): Book, Bookmark, Library, countDocumentWords(), estimateRemainingMinutes(), matchesLibraryFilter(), sortBooks(), readerCenterDelta() (+4 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.11
Nodes (27): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+19 more)

### Community 5 - "TuturiumMenus.kt"
Cohesion: 0.13
Nodes (8): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), MenuFrameRate(), TuturiumDialog(), TuturiumMenu()

### Community 6 - "LibraryScreenV2.kt"
Cohesion: 0.08
Nodes (3): bookRows(), FileArtwork(), toggleSelected()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.04
Nodes (42): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan (+34 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.34
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.08
Nodes (8): OfflineAudio, Clip, Listener, OfflinePass, SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 14 - "LibraryV2"
Cohesion: 0.16
Nodes (8): FolderStore, BookActions, FolderEditorV2(), LibraryV2(), SheetAction(), SheetHeadline(), ModernLibrary(), ReaderQueueSheet()

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.15
Nodes (4): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure(), GoogleAccessRegressionTest

### Community 17 - "ListeningStats.kt"
Cohesion: 0.20
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.13
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.12
Nodes (12): Pass, selectSpokenWord(), WordSelection, forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), safePreparedSpeech() (+4 more)

### Community 25 - "ReaderContent"
Cohesion: 0.21
Nodes (11): Reader(), ReaderContent(), ReaderOptions(), ReaderRow, ReaderMenuAction(), ReaderMenuSheet(), ReaderMenuToggle(), ReaderNotesSheet() (+3 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".write"
Cohesion: 0.07
Nodes (14): refreshOfflineModels(), amplifyNarration(), NarrationAudioCache, narrationGain(), AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio (+6 more)

### Community 45 - "ReaderActions.kt"
Cohesion: 0.12
Nodes (8): AppearanceChoice(), AppearanceSwitch(), ReaderAppearanceSheet(), ReaderAudioSheet(), readerCursorColor(), ReaderSearchBar(), readerTypeface(), RenameBookSheet()

### Community 46 - "ReadingDocument"
Cohesion: 0.22
Nodes (3): DriveArchive, ReadingDocument, Section

### Community 54 - "file"
Cohesion: 0.15
Nodes (4): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest, ReaderVoiceParityTest

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

## Knowledge Gaps
- **53 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `ListeningScreens.kt`, `Tuturium`, `OfflineAudio.kt`, `ReadingDocument`, `ListeningOptions`, `PlaybackTest.kt`, `file`?**
  _High betweenness centrality (0.087) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `ReadingService.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06506849315068493 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11553030303030302 - nodes in this community are weakly interconnected._
- **Why does `ReaderContent()` connect `ReaderContent` to `ReadingService.kt`, `DocumentImporter.kt`, `Library`, `ListeningScreens.kt`, `TuturiumMenus.kt`, `OfflineAudio.kt`, `ReaderActions.kt`, `ReadingDocument`, `LibraryV2`, `ListeningOptions`, `Reader.kt`, `.write`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._