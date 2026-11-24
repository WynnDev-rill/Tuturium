# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 89 files · ~57,159 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1169 nodes · 2665 edges · 93 communities (33 shown, 60 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 251 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5d4cd5df`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ReadingService.kt
- Updates.kt
- DocumentImporter.kt
- Library
- ListeningScreens.kt
- TuturiumMenus.kt
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
- ListeningScreens.kt
- ReaderContent
- Test
- StorageScreenV2.kt
- Reader.kt
- file
- .write
- .play
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
- SupertonicBuffer
- isCompatibleCloudAccount
- Tuturium
- Design System: Tuturium
- AndroidJUnit4
- Alur kode
- SettingsScreen.kt
- GoogleAccessRegressionTest
- Riset Tuturium
- Benchmark Supertonic 3 · Tuturium 1.3.12
- Audit Google dan pembaruan Tuturium 1.3.6
- findReaderMatches
- UtteranceProgressListener

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
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `SettingsScreen()` --calls--> `rate()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SettingsScreen.kt → app/src/main/java/id/tuturium/app/Reader.kt

## Import Cycles
- None detected.

## Communities (93 total, 60 thin omitted)

### Community 0 - "ReadingService.kt"
Cohesion: 0.06
Nodes (11): OfflineVoice, availableVoices(), choice(), indonesianCandidates(), offlineVoices(), ReadingService, OfflineSession, Callback (+3 more)

### Community 1 - "Updates.kt"
Cohesion: 0.11
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 2 - "DocumentImporter.kt"
Cohesion: 0.06
Nodes (5): DocumentImporter, OriginalPdfPages(), NestedScrollConnection, PdfPageImage(), PdfPageSession

### Community 3 - "Library"
Cohesion: 0.06
Nodes (17): Book, Bookmark, Library, BookActions, countDocumentWords(), matchesLibraryFilter(), sortBooks(), readerCenterDelta() (+9 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.12
Nodes (19): chooseVoice(), firstPassByPdfPage(), googleIndonesianVoices(), indonesianVoiceLabel(), isIndonesian(), isNewer(), Pass, pdfPageForPass() (+11 more)

### Community 5 - "TuturiumMenus.kt"
Cohesion: 0.15
Nodes (4): MenuFrameRate(), TuturiumDialog(), TuturiumMenu(), TuturiumSheet()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.15
Nodes (13): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+5 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.28
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 13 - "ModernScreens.kt"
Cohesion: 0.20
Nodes (15): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+7 more)

### Community 14 - "LibraryV2"
Cohesion: 0.14
Nodes (10): FolderStore, estimateRemainingMinutes(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2(), SheetAction(), SheetHeadline() (+2 more)

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.11
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.17
Nodes (8): forPass(), ListeningOptions, PreparedSpeech, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 23 - "ListeningScreens.kt"
Cohesion: 0.28
Nodes (11): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel(), VoiceLibraryContent() (+3 more)

### Community 25 - "ReaderContent"
Cohesion: 0.43
Nodes (4): Reader(), ReaderContent(), ReaderOptions(), ReaderRow

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.27
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".write"
Cohesion: 0.08
Nodes (13): refreshOfflineModels(), amplifyNarration(), NarrationAudioCache, narrationGain(), AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio (+5 more)

### Community 34 - ".play"
Cohesion: 0.25
Nodes (3): OfflineAudio, Clip, OfflinePass

### Community 45 - "ReaderActions.kt"
Cohesion: 0.15
Nodes (8): AppearanceChoice(), AppearanceSwitch(), ReaderAppearanceSheet(), ReaderAudioSheet(), readerCursorColor(), ReaderSearchBar(), readerTypeface(), RenameBookSheet()

### Community 46 - "ReadingDocument"
Cohesion: 0.21
Nodes (4): LibraryControlsTest, DriveArchive, ReadingDocument, Section

### Community 67 - "Tuturium"
Cohesion: 0.18
Nodes (11): SpeedControlSheet(), formatPlaybackSpeed(), formatPlaybackTime(), ReaderPlaybackBar(), Playback, seekBySeconds(), BookRow(), LibraryScreen() (+3 more)

### Community 71 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 77 - "AndroidJUnit4"
Cohesion: 0.43
Nodes (3): DriveArchiveTest, OfflineVoiceRuntimeTest, PlaybackNoCrashTest

### Community 78 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 82 - "Riset Tuturium"
Cohesion: 0.33
Nodes (6): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Riset Tuturium, Suara lokal 1.3.8

### Community 83 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 84 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

## Knowledge Gaps
- **60 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService.kt` to `.play`, `Tuturium`, `ListeningScreens.kt`, `ReadingDocument`, `Alur kode`, `ListeningOptions`, `file`?**
  _High betweenness centrality (0.111) - this node is a cross-community bridge._
- **Are the 3 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `OfflineAudio`) actually correct?**
  _`ReadingService` has 3 INFERRED edges - model-reasoned connections that need verification._
- **Should `ReadingService.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.05671466353217749 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `ReadingService.kt`, `Updates.kt`, `DocumentImporter.kt`, `Library`?**
  _High betweenness centrality (0.105) - this node is a cross-community bridge._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.10588235294117647 - nodes in this community are weakly interconnected._
- **Are the 7 inferred relationships involving `LibraryV2()` (e.g. with `matchesLibraryFilter()` and `sortBooks()`) actually correct?**
  _`LibraryV2()` has 7 INFERRED edges - model-reasoned connections that need verification._