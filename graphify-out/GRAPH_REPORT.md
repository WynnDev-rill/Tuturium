# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 117 files · ~84,332 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1549 nodes · 3765 edges · 132 communities (49 shown, 83 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 426 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `5e3a2c03`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .play
- Reader.kt
- PdfNarration.kt
- ListeningScreens.kt
- LibraryV2
- Design System: Tuturium
- MainActivity
- steps
- Tuturium
- with
- LibraryV2FlowTest
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- NarrationPipeline
- ReaderLayout
- with
- benchmark-adaptive-supertonic.py
- Library
- PreparedSpeech
- Benchmark Supertonic 3 · Tuturium 1.3.12
- PdfWordSelection.kt
- CloudAccessException
- Pemrosesan teks untuk narasi Tuturium
- OriginalPdfPages.kt
- .markDirty
- OfflineVoices.kt
- Audit Google dan pembaruan Tuturium 1.3.6
- ReaderPlaybackBar
- ReadingDocument
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- ReaderSheetsV2.kt
- profile-android.sh
- update-graph.sh
- DocumentImporter
- NarrationOpus.kt
- GoogleUpdateRegressionTest
- PdfPageImage
- ReadingService
- Supertonic adaptif · Tuturium 1.3.17
- IndonesianNumbers.kt
- README.md
- Test
- .highRatesPlayEveryQueuedPassThroughToItsLastCue
- TuturiumAccountScreen.kt
- SettingsScreen.kt
- GoogleAccountAccess.kt
- ListeningOptions
- PlaybackTest.kt
- Pass
- .write
- ReaderVoiceParityTest.kt
- .findsNearestWordOnSameLineButNotAcrossPage
- ReadingModel.kt
- DocumentImportTest
- LongPdfImportTest.kt
- LibraryControlsTest
- Color
- StorageScreenV2.kt
- matchPdfWords
- Updates.kt
- Writer
- Tuturium
- Rujukan audit transisi dan kelancaran Tuturium
- Test
- .highlightUsesOnlyCurrentPassAndIntersectingWords
- isCompatibleCloudAccount
- UtteranceProgressListener
- PdfSourceStorageTest.kt
- GoogleAccessRegressionTest

## God Nodes (most connected - your core abstractions)
1. `ReadingDocument` - 58 edges
2. `ReadingService` - 57 edges
3. `Section` - 38 edges
4. `Library` - 37 edges
5. `ListeningOptions` - 35 edges
6. `ReaderContent()` - 33 edges
7. `LibraryV2()` - 32 edges
8. `Book` - 28 edges
9. `PreparedSpeech` - 25 edges
10. `Tuturium()` - 24 edges

## Surprising Connections (you probably didn't know these)
- `Implementasi 1.3.24` --references--> `NarrationPipeline`  [INFERRED]
  docs/narration-text-processing.md → app/src/main/java/id/tuturium/app/NarrationPipeline.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `PdfMarginStore`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/PdfNarration.kt
- `Alur kode` --references--> `PdfNarrationPlan`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/PdfNarration.kt

## Import Cycles
- None detected.

## Communities (132 total, 83 thin omitted)

### Community 0 - ".play"
Cohesion: 0.11
Nodes (5): OfflineAudio, Clip, Listener, OfflinePass, PlaybackMetrics

### Community 1 - "Reader.kt"
Cohesion: 0.07
Nodes (7): toggleSelected(), rate(), AppearanceChoice(), AppearanceSwitch(), ReaderAppearanceSheet(), readerCursorColor(), readerTypeface()

### Community 2 - "PdfNarration.kt"
Cohesion: 0.11
Nodes (15): cells(), layoutLines(), PdfContentKind, DECORATION, FOOTER, FOOTNOTE, HEADER, MARGIN (+7 more)

### Community 3 - "ListeningScreens.kt"
Cohesion: 0.31
Nodes (8): IndonesianVoiceAvatar(), OfflineModelCard(), VoiceDetailPanel(), voiceLabel(), VoiceLibraryContent(), VoiceLibraryRow(), VoiceLibraryV2(), voiceSubtitle()

### Community 4 - "LibraryV2"
Cohesion: 0.08
Nodes (18): FolderStore, BookActions, isInLibraryFolder(), bookRows(), FileArtwork(), FolderEditorV2(), LibraryV2(), PinnedBookTitle() (+10 more)

### Community 5 - "Design System: Tuturium"
Cohesion: 0.18
Nodes (10): Avoid (Anti-patterns), Colors, Design Dials, Design System: Tuturium, Key Effects, Motion, Pattern, Pre-Delivery Checklist (+2 more)

### Community 7 - "steps"
Cohesion: 0.06
Nodes (35): concurrency, cancel-in-progress, group, GH_TOKEN, KEYSTORE_BASE64, TAG, TUTURIUM_STORE_PASSWORD, tag (+27 more)

### Community 8 - "Tuturium"
Cohesion: 0.13
Nodes (15): Aktivasi rilis otomatis (sekali saja), Akun Google dan pembaruan 1.3.6, Build dan pengujian, Desain, Menandatangani dan menerbitkan, Pasang dan gunakan, Pembaruan 1.1.1, Pembaruan 1.3.2 (+7 more)

### Community 9 - "with"
Cohesion: 0.06
Nodes (30): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+22 more)

### Community 12 - "SupertonicBuffer"
Cohesion: 0.19
Nodes (5): SupertonicBuffer, synthesisThreadCount(), findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.08
Nodes (25): ListeningDay, ListeningStats, StatsBucket, StatsSummary, DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars() (+17 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.25
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 15 - "NarrationPipeline"
Cohesion: 0.18
Nodes (10): Alur kode, excluding(), NarrationPipeline, buildPdfNarrationPlan(), PdfMargins, PdfMarginSettings, alignPdfHits(), ocrPdfWordHits() (+2 more)

### Community 16 - "ReaderLayout"
Cohesion: 0.47
Nodes (4): documentWordEnds(), buildReaderLayout(), ReaderLayout, ReaderRow

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 20 - "Library"
Cohesion: 0.11
Nodes (3): Bookmark, Library, countDocumentWords()

### Community 22 - "PreparedSpeech"
Cohesion: 0.20
Nodes (4): MediaReadingCaption, Cue, PreparedSpeech, MediaReadingCaptionTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "PdfWordSelection.kt"
Cohesion: 0.23
Nodes (4): PDFTextStripper, PdfWordStripper, readPdfWordHits(), readPdfWords()

### Community 26 - "CloudAccessException"
Cohesion: 0.34
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 27 - "Pemrosesan teks untuk narasi Tuturium"
Cohesion: 0.25
Nodes (7): ICU, CLDR, dan Revo Norm, Implementasi 1.3.24, Pemrosesan teks untuk narasi Tuturium, Penerapan pada pembaca offline, Pengujian host, Rujukan pembaca, Verifikasi Android dan pembaruan

### Community 28 - "OriginalPdfPages.kt"
Cohesion: 0.08
Nodes (3): NestedScrollConnection, MarginOverlay(), PdfMarginSheet()

### Community 29 - ".markDirty"
Cohesion: 0.26
Nodes (4): isMatchingDriveAccount(), TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.30
Nodes (6): OfflineVoiceRuntimeTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit Google dan pembaruan Tuturium 1.3.6"
Cohesion: 0.40
Nodes (4): Audit Google dan pembaruan Tuturium 1.3.6, Identitas dan Google Cloud, Temuan, Uji langsung

### Community 35 - "ReaderPlaybackBar"
Cohesion: 0.07
Nodes (19): Book, matchesLibraryFilter(), sortBooks(), newReaderPace(), readerCenterDelta(), playbackTimeCaption(), NarrationPaceSnapshot, PaceProfile (+11 more)

### Community 36 - "ReadingDocument"
Cohesion: 0.15
Nodes (6): DriveArchiveTest, ReaderFlowTest, DriveArchive, RestoreBook, ReadingDocument, Section

### Community 40 - "release-notes.md"
Cohesion: 0.20
Nodes (9): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23, Tuturium 1.3.24 (+1 more)

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 46 - "ReaderSheetsV2.kt"
Cohesion: 0.14
Nodes (22): ListeningSettingsV2(), SkipOptionsSheet(), SpeedControlSheet(), SwitchRow(), VerticalSpeedSlider(), Reader(), ReaderContent(), ReaderOptions() (+14 more)

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.13
Nodes (5): amplifyNarration(), narrationGain(), encodeNarrationOpus(), NarrationResampler, NarrationAudioTest

### Community 55 - "PdfPageImage"
Cohesion: 0.28
Nodes (3): OriginalPdfPages(), PdfPageImage(), PdfPageSession

### Community 57 - "ReadingService"
Cohesion: 0.05
Nodes (12): narrationBoostMb(), UtteranceAudioLength, availableVoices(), choice(), indonesianCandidates(), offlineVoices(), ReadingService, OfflineSession (+4 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 67 - "IndonesianNumbers.kt"
Cohesion: 0.27
Nodes (9): clockWords(), dateWords(), indonesianNumberReplacements(), romanWords(), spellDigits(), spellInteger(), spellNumber(), underThousand() (+1 more)

### Community 71 - "Test"
Cohesion: 0.17
Nodes (6): readingTimeline(), ReadingTimeline, timelineOptions(), ListeningOptions, IndonesianNormalizationTest, ReadingTimelineTest

### Community 78 - "TuturiumAccountScreen.kt"
Cohesion: 0.16
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "GoogleAccountAccess.kt"
Cohesion: 0.20
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 82 - "ListeningOptions"
Cohesion: 0.15
Nodes (8): ReaderPlaybackBar(), forPass(), prepareSpeech(), rampedSpeed(), safePreparedSpeech(), seekBySeconds(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 86 - "Pass"
Cohesion: 0.44
Nodes (4): Pass, selectSpokenWord(), WordSelection, ReaderWordSelectionTest

### Community 87 - ".write"
Cohesion: 0.21
Nodes (6): NarrationAudioCache, AudioCue, OfflineSynth, synthesisRanges(), SynthesizedAudio, OfflineVoice

### Community 94 - "ReadingModel.kt"
Cohesion: 0.22
Nodes (13): chooseVoice(), firstPassByPdfPage(), googleIndonesianVoices(), indonesianVoiceLabel(), isIndonesian(), isNewer(), pdfPageForPass(), pdfPageForSection() (+5 more)

### Community 100 - "StorageScreenV2.kt"
Cohesion: 0.24
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 101 - "matchPdfWords"
Cohesion: 0.39
Nodes (3): matchPdfWords(), pdfKey(), ReaderUpdateTest

### Community 102 - "Updates.kt"
Cohesion: 0.20
Nodes (3): Release, UpdateCheckException, Updates

### Community 110 - "Tuturium"
Cohesion: 0.40
Nodes (4): Graphify, Mengerjakan perubahan, Rilis, Tuturium

### Community 111 - "Rujukan audit transisi dan kelancaran Tuturium"
Cohesion: 0.40
Nodes (4): Cara mengukur dan kriteria penerimaan, Pola dari produk dan panduan platform, Rujukan audit transisi dan kelancaran Tuturium, Titik audit di Tuturium

## Knowledge Gaps
- **6 isolated node(s):** `DECORATION`, `FOOTER`, `FOOTNOTE`, `HEADER`, `MARGIN` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 411 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **83 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Alur kode` connect `NarrationPipeline` to `PdfNarration.kt`, `Updates.kt`, `Tuturium`, `DocumentImporter`, `Library`, `ReadingService`?**
  _High betweenness centrality (0.112) - this node is a cross-community bridge._
- **Are the 35 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.invalidLaterBookDoesNotRestoreEarlierBookOrApplyDeletion()`) actually correct?**
  _`ReadingDocument` has 35 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DECORATION`, `FOOTER`, `FOOTNOTE` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.1051693404634581 - nodes in this community are weakly interconnected._
- **Why does `ReadingService` connect `ReadingService` to `.play`, `ReaderPlaybackBar`, `ReadingDocument`, `LibraryV2`, `Test`, `NarrationPipeline`, `PlaybackTest.kt`, `PreparedSpeech`, `ReadingModel.kt`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `Reader.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.0673758865248227 - nodes in this community are weakly interconnected._