# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 89 files · ~57,359 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1173 nodes · 2671 edges · 94 communities (28 shown, 66 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 251 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `ee69ac2f`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ReadingService.kt
- Updates.kt
- Library
- ListeningScreens.kt
- TuturiumMenus.kt
- Tuturium.kt
- steps
- Tuturium
- with
- TuturiumCloud
- OfflineAudio.kt
- ModernScreens.kt
- LibraryControlsTest
- GoogleAccountAccess.kt
- Reader.kt
- with
- benchmark-supertonic.py
- TuturiumAccountScreen.kt
- ListeningOptions
- ReadingService
- PdfPageSession
- Test
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- .write
- PlaybackTest.kt
- LibraryV2FlowTest
- instrumentationregistry
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderContent
- ReadingDocument
- profile-android.sh
- update-graph.sh
- Theme.kt
- file
- isCompatibleCloudAccount
- ListeningStats.kt
- GoogleUpdateRegressionTest
- DocumentImporter
- SettingsScreen.kt
- GoogleAccessRegressionTest
- .voice
- .read
- Callback
- UtteranceProgressListener
- AutomaticSpeed

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 47 edges
2. `ReaderContent()` - 33 edges
3. `Library` - 31 edges
4. `LibraryV2()` - 30 edges
5. `ReadingDocument` - 27 edges
6. `TuturiumCloud` - 23 edges
7. `Tuturium()` - 22 edges
8. `Book` - 20 edges
9. `VoiceChoice` - 19 edges
10. `Section` - 19 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (94 total, 66 thin omitted)

### Community 0 - "ReadingService.kt"
Cohesion: 0.09
Nodes (4): availableVoices(), choice(), indonesianCandidates(), offlineVoices()

### Community 1 - "Updates.kt"
Cohesion: 0.16
Nodes (3): Release, UpdateCheckException, Updates

### Community 3 - "Library"
Cohesion: 0.06
Nodes (17): FolderStore, Book, Bookmark, Library, BookActions, countDocumentWords(), estimateRemainingMinutes(), matchesLibraryFilter() (+9 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.09
Nodes (31): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), VoiceDetailPanel() (+23 more)

### Community 5 - "TuturiumMenus.kt"
Cohesion: 0.17
Nodes (3): MenuFrameRate(), TuturiumDialog(), TuturiumMenu()

### Community 6 - "Tuturium.kt"
Cohesion: 0.14
Nodes (6): Playback, BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.04
Nodes (39): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung, Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android (+31 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.31
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.08
Nodes (8): OfflineAudio, Clip, Listener, OfflinePass, SupertonicBuffer, findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.19
Nodes (16): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+8 more)

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "Reader.kt"
Cohesion: 0.06
Nodes (3): toggleSelected(), rate(), readerTypeface()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.14
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.18
Nodes (8): forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), seekBySeconds(), SpeechControlTest

### Community 23 - "ReadingService"
Cohesion: 0.25
Nodes (3): ReadingService, OfflineSession, shouldRetrySpeechAtNormalSpeed()

### Community 25 - "PdfPageSession"
Cohesion: 0.32
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".write"
Cohesion: 0.08
Nodes (13): refreshOfflineModels(), amplifyNarration(), NarrationAudioCache, narrationGain(), AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio (+5 more)

### Community 45 - "ReaderContent"
Cohesion: 0.09
Nodes (25): Reader(), readerCenterDelta(), ReaderContent(), ReaderOptions(), ReaderRow, AppearanceChoice(), AppearanceSwitch(), ReaderAppearanceSheet() (+17 more)

### Community 46 - "ReadingDocument"
Cohesion: 0.27
Nodes (3): DriveArchive, ReadingDocument, Section

### Community 54 - "file"
Cohesion: 0.14
Nodes (4): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest, ReaderVoiceParityTest

### Community 67 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 78 - "DocumentImporter"
Cohesion: 0.17
Nodes (6): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, DocumentImporter

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

## Knowledge Gaps
- **66 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `ReadingService.kt`, `PlaybackTest.kt`, `ListeningScreens.kt`, `Tuturium.kt`, `OfflineAudio.kt`, `ReadingDocument`, `DocumentImporter`, `.voice`, `.read`, `.notification`, `ListeningOptions`, `file`, `AutomaticSpeed`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Should `ReadingService.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0873015873015873 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `DocumentImporter` to `Updates.kt`, `Library`, `ReadingService`?**
  _High betweenness centrality (0.091) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._
- **Should `DocumentImportTest.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.11764705882352941 - nodes in this community are weakly interconnected._
- **Why does `Tuturium` connect `DocumentImporter` to `Tuturium`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._