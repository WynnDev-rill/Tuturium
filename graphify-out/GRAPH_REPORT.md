# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 109 files · ~75,663 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1425 nodes · 3365 edges · 117 communities (45 shown, 72 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 362 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `4f3888ab`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .play
- ReaderActions.kt
- DocumentImportTest.kt
- NarrationAudioCache.kt
- ListeningScreens.kt
- Design System: Tuturium
- ReaderContent
- steps
- Tuturium
- with
- TuturiumCloud
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- TuturiumCloud.kt
- GoogleAccountAccess.kt
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- Library
- ListeningOptions
- Benchmark Supertonic 3 · Tuturium 1.3.12
- Pass
- LibraryV2FlowTest
- StorageScreenV2.kt
- OriginalPdfPages.kt
- file
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- ReadingPace.kt
- Theme.kt
- MainActivity
- ReaderFlowTest.kt
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- Reader.kt
- profile-android.sh
- update-graph.sh
- NarrationOpus.kt
- ReaderVoiceParityTest.kt
- LibraryScreenV2.kt
- ReadingService
- Supertonic adaptif · Tuturium 1.3.17
- Reader.kt
- isCompatibleCloudAccount
- ListeningStats.kt
- README.md
- DriveArchive
- Updates.kt
- SettingsScreen.kt
- ReadingDocument
- ReadingService.kt
- isMatchingDriveAccount
- .write
- UtteranceProgressListener
- DocumentImporter
- ReaderPlaybackBar
- GoogleUpdateRegressionTest
- ReadingTimeline
- Composable
- MediaReadingCaption
- ReadingTimePresentation.kt
- NarrationFlow.kt
- SynthesizedAudio
- LongPdfImportTest.kt
- Test
- Alur kode
- .endPage
- Test
- narrationMillisPerWord
- AutomaticSpeed

## God Nodes (most connected - your core abstractions)
1. `ReadingService` - 55 edges
2. `ReadingDocument` - 39 edges
3. `ReaderContent()` - 33 edges
4. `Library` - 32 edges
5. `LibraryV2()` - 32 edges
6. `Book` - 28 edges
7. `Section` - 28 edges
8. `ListeningOptions` - 27 edges
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
- `TuturiumAccountScreen()` --calls--> `TuturiumCloudState`  [INFERRED]
  app/src/main/java/id/tuturium/app/TuturiumAccountScreen.kt → app/src/main/java/id/tuturium/app/TuturiumCloud.kt

## Import Cycles
- None detected.

## Communities (117 total, 72 thin omitted)

### Community 0 - ".play"
Cohesion: 0.14
Nodes (7): OfflineVoiceRuntimeTest, OfflineAudio, OfflinePass, PlaybackMetrics, synthesisRanges(), forPass(), PreparedSpeech

### Community 4 - "ListeningScreens.kt"
Cohesion: 0.11
Nodes (24): IndonesianVoiceAvatar(), VoiceDetailPanel(), voiceLabel(), VoiceLibraryContent(), VoiceLibraryRow(), voiceSubtitle(), chooseVoice(), firstPassByPdfPage() (+16 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.20
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 6 - "ReaderContent"
Cohesion: 0.08
Nodes (22): Reader(), ReaderContent(), ReaderOptions(), ReaderRow, AppearanceChoice(), AppearanceSwitch(), NarrationPauseControl(), ReaderAppearanceSheet() (+14 more)

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.14
Nodes (14): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+6 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 12 - "SupertonicBuffer"
Cohesion: 0.20
Nodes (5): SupertonicBuffer, synthesisThreadCount(), findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.18
Nodes (15): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernNavigation(), ModernProfile() (+7 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.25
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 16 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 17 - "Tuturium.kt"
Cohesion: 0.15
Nodes (6): Playback, BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium()

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 20 - "Library"
Cohesion: 0.06
Nodes (18): FolderStore, Book, Bookmark, Library, BookActions, countDocumentWords(), documentWordEnds(), isInLibraryFolder() (+10 more)

### Community 22 - "ListeningOptions"
Cohesion: 0.19
Nodes (7): ListeningOptions, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), seekBySeconds(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "Pass"
Cohesion: 0.06
Nodes (21): OriginalPdfPages(), PdfPageImage(), PdfPageSession, pdfWordOutsideViewport(), matchPdfWords(), nearestPdfWord(), ocrPdfWordHits(), pdfHighlightWords() (+13 more)

### Community 27 - "StorageScreenV2.kt"
Cohesion: 0.18
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 29 - "file"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveConsentRequired

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.26
Nodes (5): refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ReadingPace.kt"
Cohesion: 0.31
Nodes (3): PaceProfile, PaceSample, ReadingPace

### Community 40 - "release-notes.md"
Cohesion: 0.25
Nodes (7): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.13
Nodes (4): amplifyNarration(), encodeNarrationOpus(), NarrationResampler, NarrationAudioTest

### Community 55 - "LibraryScreenV2.kt"
Cohesion: 0.14
Nodes (5): bookRows(), FileArtwork(), PinnedBookTitle(), SheetAction(), ModernTrash()

### Community 57 - "ReadingService"
Cohesion: 0.15
Nodes (3): ReadingService, OfflineSession, Callback

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 62 - "Reader.kt"
Cohesion: 0.16
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 67 - "ListeningStats.kt"
Cohesion: 0.18
Nodes (6): ListeningDay, ListeningStats, StatsBucket, StatsSummary, MetricBars(), ListeningStatsTest

### Community 71 - "DriveArchive"
Cohesion: 0.16
Nodes (3): DriveArchiveTest, PlaybackNoCrashTest, DriveArchive

### Community 78 - "Updates.kt"
Cohesion: 0.19
Nodes (3): Release, UpdateCheckException, Updates

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.23
Nodes (7): rate(), SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "ReadingDocument"
Cohesion: 0.17
Nodes (4): LibraryControlsTest, ReadingDocument, Section, ReadingSettings

### Community 87 - ".write"
Cohesion: 0.26
Nodes (4): narrationGain(), spokenPcmRange(), OfflineSynth, OfflineVoice

### Community 94 - "ReaderPlaybackBar"
Cohesion: 0.26
Nodes (7): SpeedControlSheet(), VerticalSpeedSlider(), formatPlaybackSpeed(), formatPlaybackTime(), playbackTimeCaption(), ReaderPlaybackBar(), ReadingPosition

### Community 99 - "ReadingTimeline"
Cohesion: 0.29
Nodes (4): readingTimeline(), ReadingTimeline, timelineOptions(), ReadingTimelineTest

### Community 100 - "Composable"
Cohesion: 0.38
Nodes (7): ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SwitchRow(), VoiceLibraryV2(), libraryReadingLabel(), readingPosition()

### Community 101 - "MediaReadingCaption"
Cohesion: 0.36
Nodes (3): MediaReadingCaption, Cue, MediaReadingCaptionTest

### Community 102 - "ReadingTimePresentation.kt"
Cohesion: 0.24
Nodes (3): estimatedReadingSpeed(), hasPosition(), readingSpeed()

### Community 103 - "NarrationFlow.kt"
Cohesion: 0.39
Nodes (8): clausePause(), continuesAcrossPdfPage(), expandedAbbreviation(), NarrationPause, normalizedCharacter(), normalizedForNarration(), pauseAfter(), withoutClosingPunctuation()

### Community 104 - "SynthesizedAudio"
Cohesion: 0.61
Nodes (3): NarrationAudioCache, AudioCue, SynthesizedAudio

### Community 108 - "Alur kode"
Cohesion: 0.33
Nodes (5): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium

### Community 111 - "narrationMillisPerWord"
Cohesion: 0.60
Nodes (3): newReaderPace(), NarrationPaceSnapshot, narrationMillisPerWord()

## Knowledge Gaps
- **72 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `ReadingService` connect `ReadingService` to `.play`, `NarrationAudioCache.kt`, `ListeningScreens.kt`, `MediaReadingCaption`, `ReadingPace.kt`, `Alur kode`, `ReadingDocument`, `Tuturium.kt`, `AutomaticSpeed`, `ReadingService.kt`, `ListeningOptions`, `ReaderVoiceParityTest.kt`, `Pass`?**
  _High betweenness centrality (0.146) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.13793103448275862 - nodes in this community are weakly interconnected._
- **Why does `Alur kode` connect `Alur kode` to `ReadingService`, `DocumentImporter`, `Library`, `Updates.kt`?**
  _High betweenness centrality (0.142) - this node is a cross-community bridge._
- **Are the 24 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.unchangedArchiveIsStableAndHardDeleteWinsOverStaleBackup()`) actually correct?**
  _`ReadingDocument` has 24 INFERRED edges - model-reasoned connections that need verification._
- **Should `NarrationAudioCache.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._
- **Are the 20 inferred relationships involving `ReaderContent()` (e.g. with `SkipOptionsSheet()` and `SpeedControlSheet()`) actually correct?**
  _`ReaderContent()` has 20 INFERRED edges - model-reasoned connections that need verification._