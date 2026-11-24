# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 83 files · ~50,708 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1049 nodes · 2359 edges · 82 communities (24 shown, 58 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 210 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `790606ed`
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
- OfflineAudio.kt
- ModernScreens.kt
- Reader.kt
- GoogleAccountAccess.kt
- ListeningStats.kt
- with
- ReadingService.kt
- TuturiumAccountScreen.kt
- Pass
- ReaderVoiceParityTest.kt
- ReaderContent
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
- DriveArchive
- profile-android.sh
- update-graph.sh
- file
- UtteranceProgressListener
- isCompatibleCloudAccount
- GoogleAccessRegressionTest
- Tuturium

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

## Communities (82 total, 58 thin omitted)

### Community 0 - "OfflineAudio.kt"
Cohesion: 0.16
Nodes (6): forPass(), ListeningOptions, prepareSpeech(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 1 - "Updates.kt"
Cohesion: 0.11
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.08
Nodes (4): DocumentImporter, OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.09
Nodes (4): Bookmark, Library, countDocumentWords(), seekBySeconds()

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.12
Nodes (24): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel() (+16 more)

### Community 5 - "LibraryV2"
Cohesion: 0.10
Nodes (13): LibraryControlsTest, FolderStore, BookActions, estimateRemainingMinutes(), sortBooks(), bookRows(), FileArtwork(), FolderEditorV2() (+5 more)

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
Cohesion: 0.19
Nodes (3): isMatchingDriveAccount(), TuturiumCloud, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.06
Nodes (14): refreshOfflineModels(), OfflineAudio, Clip, Listener, OfflinePass, AudioCue, OfflineSynth, synthesisRanges() (+6 more)

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
Nodes (13): OfflineVoice, ReadingDocument, Section, availableVoices(), choice(), indonesianCandidates(), offlineVoices(), ReadingService (+5 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.12
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "Pass"
Cohesion: 0.34
Nodes (5): ReaderRow, Pass, selectSpokenWord(), WordSelection, ReaderWordSelectionTest

### Community 25 - "ReaderContent"
Cohesion: 0.08
Nodes (23): Book, matchesLibraryFilter(), Reader(), readerCenterDelta(), ReaderContent(), ReaderOptions(), formatPlaybackTime(), ReaderPlaybackBar() (+15 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 54 - "file"
Cohesion: 0.18
Nodes (4): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest, PlaybackTest

### Community 69 - "Tuturium"
Cohesion: 0.60
Nodes (3): ModernUpdateMenu(), PasteDialog(), Tuturium()

## Knowledge Gaps
- **58 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `OfflineAudio.kt`, `ListeningScreens.kt`, `Tuturium`, `OfflineAudio.kt`, `ReaderVoiceParityTest.kt`, `ReaderContent`?**
  _High betweenness centrality (0.115) - this node is a cross-community bridge._
- **Are the 2 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 2 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11051693404634581 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Tuturium` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.095) - this node is a cross-community bridge._
- **Are the 6 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImporter.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.07665505226480836 - nodes in this community are weakly interconnected._
- **Are the 12 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 12 INFERRED edges - model-reasoned connections that need verification._