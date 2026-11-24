# Graph Report - Tuturium  (2026-11-24)

## Corpus Check
- 105 files · ~68,984 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1367 nodes · 3200 edges · 102 communities (39 shown, 63 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 337 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `65c5eb4d`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- ReadingService
- Updates.kt
- LibraryV2
- ListeningScreens.kt
- Design System: Tuturium
- Tuturium.kt
- steps
- Tuturium
- with
- TuturiumCloud
- OfflineAudio.kt
- ModernScreens.kt
- Riset Tuturium
- GoogleAccountAccess.kt
- ListeningScreens.kt
- with
- benchmark-adaptive-supertonic.py
- TuturiumAccountScreen.kt
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- Pass
- Test
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- .install
- Audit Google dan pembaruan Tuturium 1.3.6
- LibraryV2FlowTest
- instrumentationregistry
- MainActivity
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- ReaderContent
- AndroidJUnit4
- profile-android.sh
- update-graph.sh
- ReaderPlaybackBar
- file
- Library
- ReadingService.kt
- README.md
- ReaderActions.kt
- ReadingTimePresentation.kt
- isCompatibleCloudAccount
- ListeningStats.kt
- PlaybackTest.kt
- narration-flow.md
- ReadingPace.kt
- .write
- file
- LongPdfImportTest.kt
- NarrationOpus.kt
- narrationGain
- DocumentImporter.kt
- SettingsScreen.kt
- .read
- .onCreate
- OfflineSynth
- ReadingDocument
- LibraryControlsTest
- isMatchingDriveAccount
- UtteranceAudioLength
- UtteranceProgressListener
- Test
- AutomaticSpeed
- ReaderFlowTest
- NarrationFlow.kt
- Test

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 50 edges
2. `ReadingDocument` - 38 edges
3. `ReaderContent()` - 33 edges
4. `Library` - 32 edges
5. `LibraryV2()` - 32 edges
6. `Section` - 27 edges
7. `Book` - 26 edges
8. `ListeningOptions` - 26 edges
9. `TuturiumCloud` - 23 edges
10. `Tuturium()` - 23 edges

## Surprising Connections (you probably didn't know these)
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `ReadingService`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/ReadingService.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `DocumentImporter`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/DocumentImporter.kt
- `seekBySeconds()` --calls--> `countDocumentWords()`  [INFERRED]
  app/src/main/java/id/tuturium/app/SpeechControls.kt → app/src/main/java/id/tuturium/app/LibraryPresentation.kt

## Import Cycles
- None detected.

## Communities (102 total, 63 thin omitted)

### Community 0 - "ReadingService"
Cohesion: 0.23
Nodes (4): narrationBoostMb(), OfflineVoice, ReadingService, OfflineSession

### Community 1 - "Updates.kt"
Cohesion: 0.12
Nodes (4): GoogleUpdateRegressionTest, Release, UpdateCheckException, Updates

### Community 3 - "LibraryV2"
Cohesion: 0.15
Nodes (12): FolderStore, isInLibraryFolder(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2(), PinnedBookTitle(), SheetAction() (+4 more)

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.11
Nodes (26): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel(), VoiceLibraryContent() (+18 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "Tuturium.kt"
Cohesion: 0.16
Nodes (7): Playback, BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), TuturiumDialog()

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.14
Nodes (14): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+6 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 10 - "TuturiumCloud"
Cohesion: 0.28
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 12 - "OfflineAudio.kt"
Cohesion: 0.07
Nodes (8): OfflineAudio, Listener, OfflinePass, SupertonicBuffer, findReaderMatches(), ReaderMatch, PreparedSpeech, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.17
Nodes (17): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation() (+9 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.29
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.13
Nodes (8): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), main()

### Community 20 - "TuturiumAccountScreen.kt"
Cohesion: 0.16
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 22 - "ListeningOptions"
Cohesion: 0.18
Nodes (7): forPass(), ListeningOptions, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "Pass"
Cohesion: 0.07
Nodes (18): OriginalPdfPages(), PdfPageImage(), PdfPageSession, matchPdfWords(), nearestPdfWord(), ocrPdfWordHits(), pdfHighlightWords(), pdfKey() (+10 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.18
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 31 - ".install"
Cohesion: 0.21
Nodes (5): refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 40 - "release-notes.md"
Cohesion: 0.50
Nodes (3): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19

### Community 45 - "ReaderContent"
Cohesion: 0.07
Nodes (23): BookActions, Reader(), ReaderContent(), ReaderOptions(), ReaderRow, AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl() (+15 more)

### Community 53 - "ReaderPlaybackBar"
Cohesion: 0.19
Nodes (8): SpeedControlSheet(), VerticalSpeedSlider(), formatPlaybackSpeed(), formatPlaybackTime(), ReaderPlaybackBar(), ReadingPosition, timelineOptions(), seekBySeconds()

### Community 55 - "Library"
Cohesion: 0.07
Nodes (10): Book, Bookmark, Library, countDocumentWords(), documentWordEnds(), matchesLibraryFilter(), sortBooks(), readerCenterDelta() (+2 more)

### Community 57 - "ReadingService.kt"
Cohesion: 0.09
Nodes (4): availableVoices(), choice(), indonesianCandidates(), offlineVoices()

### Community 60 - "README.md"
Cohesion: 0.18
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 63 - "ReadingTimePresentation.kt"
Cohesion: 0.23
Nodes (5): hasPosition(), libraryReadingLabel(), narrationMillisPerWord(), readingPosition(), readingSpeed()

### Community 67 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (5): ListeningDay, ListeningStats, StatsBucket, StatsSummary, ListeningStatsTest

### Community 70 - "ReadingPace.kt"
Cohesion: 0.44
Nodes (3): PaceProfile, PaceSample, ReadingPace

### Community 71 - ".write"
Cohesion: 0.61
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 77 - "narrationGain"
Cohesion: 0.28
Nodes (3): amplifyNarration(), narrationGain(), NarrationAudioTest

### Community 78 - "DocumentImporter.kt"
Cohesion: 0.07
Nodes (7): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, DocumentImporter, PDFTextStripper

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.25
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 84 - "ReadingDocument"
Cohesion: 0.24
Nodes (5): readingTimeline(), ReadingDocument, Section, ReadingTimeline, ReadingTimelineTest

### Community 100 - "NarrationFlow.kt"
Cohesion: 0.39
Nodes (8): clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter(), normalizedForNarration(), pauseAfter(), withoutClosingPunctuation()

## Knowledge Gaps
- **63 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `ListeningScreens.kt`, `PlaybackTest.kt`, `ReadingPace.kt`, `Tuturium.kt`, `OfflineAudio.kt`, `DocumentImporter.kt`, `.read`, `.onCreate`, `ReadingDocument`, `ListeningOptions`, `UtteranceAudioLength`, `file`, `ReadingService.kt`, `AutomaticSpeed`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `Updates.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.12096774193548387 - nodes in this community are weakly interconnected._
- **Why does `ReadingDocument` connect `ReadingDocument` to `ReadingService`, `LibraryV2FlowTest`, `LibraryV2`, `ListeningScreens.kt`, `Test`, `OfflineAudio.kt`, `ReaderContent`, `AndroidJUnit4`, `DocumentImporter.kt`, `.read`, `ReaderPlaybackBar`, `ListeningOptions`, `Library`, `Pass`, `ReaderFlowTest`, `ReadingTimePresentation.kt`?**
  _High betweenness centrality (0.076) - this node is a cross-community bridge._
- **Are the 23 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 23 INFERRED edges - model-reasoned connections that need verification._
- **Should `ListeningScreens.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.1111111111111111 - nodes in this community are weakly interconnected._
- **Why does `ReaderContent()` connect `ReaderContent` to `ReadingService`, `ListeningScreens.kt`, `Tuturium.kt`, `OfflineAudio.kt`, `AndroidJUnit4`, `ReadingDocument`, `ReaderPlaybackBar`, `ListeningOptions`, `Library`, `Pass`, `OriginalPdfPages.kt`, `ReaderActions.kt`, `ReadingTimePresentation.kt`?**
  _High betweenness centrality (0.071) - this node is a cross-community bridge._