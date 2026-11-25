# Graph Report - Tuturium  (2026-11-25)

## Corpus Check
- 118 files · ~85,734 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 6, .ttf 5, (none) 2)

## Summary
- 1557 nodes · 3778 edges · 116 communities (41 shown, 75 thin omitted)
- Extraction: 89% EXTRACTED · 11% INFERRED · 0% AMBIGUOUS · INFERRED: 428 edges (avg confidence: 0.85)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1e8314ee`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- .play
- Reader.kt
- PdfNarration.kt
- Tuturium
- Library
- Design System: Tuturium
- MainActivity
- steps
- Tuturium
- with
- PdfContentKind
- SupertonicBuffer
- ModernScreens.kt
- Riset Tuturium
- NarrationPipeline
- Tuturium.kt
- with
- benchmark-adaptive-supertonic.py
- PreparedSpeech
- Benchmark Supertonic 3 · Tuturium 1.3.12
- PdfWordSelection.kt
- CloudAccessException
- Pemrosesan teks untuk narasi Tuturium
- OriginalPdfPages.kt
- .markDirty
- OfflineVoices.kt
- Audit data, stabilitas, dan transisi Tuturium
- ReaderPlaybackBar
- ReadingDocument
- release-notes.md
- check-release.sh
- setup-signing-secrets.sh
- test-playback.sh
- supertonic-acceleration.md
- ListeningScreens.kt
- profile-android.sh
- update-graph.sh
- NarrationOpus.kt
- Pass
- ReadingService
- Supertonic adaptif · Tuturium 1.3.17
- IndonesianNumbers.kt
- README.md
- TuturiumAccountScreen.kt
- SettingsScreen.kt
- GoogleAccountAccess.kt
- ListeningOptions
- PlaybackTest.kt
- .write
- ReaderVoiceParityTest.kt
- .findsNearestWordOnSameLineButNotAcrossPage
- DocumentImportTest
- LongPdfImportTest.kt
- StorageScreenV2.kt
- DocumentImporter.kt
- Writer
- SynthesizedAudio
- Context
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
- `Alur kode` --references--> `Updates`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Updates.kt
- `Alur kode` --references--> `NarrationPipeline`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/NarrationPipeline.kt
- `Alur kode` --references--> `PdfNarrationPlan`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/PdfNarration.kt
- `Alur kode` --references--> `Library`  [INFERRED]
  AGENTS.md → app/src/main/java/id/tuturium/app/Library.kt

## Import Cycles
- None detected.

## Communities (116 total, 75 thin omitted)

### Community 0 - ".play"
Cohesion: 0.11
Nodes (5): OfflineAudio, Clip, Listener, OfflinePass, PlaybackMetrics

### Community 2 - "PdfNarration.kt"
Cohesion: 0.19
Nodes (5): cells(), PdfContentSpan, PdfNarrationPlan, PdfNarrationStore, Key

### Community 3 - "Tuturium"
Cohesion: 0.44
Nodes (6): BookRow(), LibraryScreen(), ModernUpdateMenu(), PasteDialog(), Tuturium(), TuturiumDialog()

### Community 4 - "Library"
Cohesion: 0.06
Nodes (19): RestoreBook, FolderStore, Book, Bookmark, Library, BookActions, countDocumentWords(), isInLibraryFolder() (+11 more)

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
Nodes (31): concurrency, cancel-in-progress, group, jobs, verify, name, on, pull_request (+23 more)

### Community 10 - "PdfContentKind"
Cohesion: 0.29
Nodes (7): PdfContentKind, DECORATION, FOOTER, FOOTNOTE, HEADER, MARGIN, TABLE

### Community 12 - "SupertonicBuffer"
Cohesion: 0.19
Nodes (5): SupertonicBuffer, synthesisThreadCount(), findReaderMatches(), ReaderMatch, ContinuousReadingTest

### Community 13 - "ModernScreens.kt"
Cohesion: 0.17
Nodes (18): DocumentArt(), durationLabel(), HomeGoogleAvatar(), MetricBars(), MetricPair(), ModernDetailHeader(), ModernHome(), ModernLibrary() (+10 more)

### Community 14 - "Riset Tuturium"
Cohesion: 0.25
Nodes (7): Aset dan komponen, PDF vertikal dan kecepatan otomatis 1.3.13, Pembaca dan koneksi Drive 1.3.11, Pembanding Android, Perbaikan 1.3.15, Riset Tuturium, Suara lokal 1.3.8

### Community 15 - "NarrationPipeline"
Cohesion: 0.21
Nodes (10): NarrationPipeline, buildPdfNarrationPlan(), layoutLines(), PdfLine, PdfMargins, PdfMarginSettings, alignPdfHits(), ocrPdfWordHits() (+2 more)

### Community 18 - "with"
Cohesion: 0.09
Nodes (21): jobs, package, name, on, push, workflow_dispatch, runs-on, steps (+13 more)

### Community 19 - "benchmark-adaptive-supertonic.py"
Cohesion: 0.12
Nodes (10): chunks(), engine(), replay(), run_synthesis(), generate(), score(), word_errors(), latency() (+2 more)

### Community 22 - "PreparedSpeech"
Cohesion: 0.15
Nodes (6): MediaReadingCaption, Cue, excluding(), forPass(), PreparedSpeech, MediaReadingCaptionTest

### Community 23 - "Benchmark Supertonic 3 · Tuturium 1.3.12"
Cohesion: 0.33
Nodes (6): Benchmark Supertonic 3 · Tuturium 1.3.12, Kandidat akselerasi, Pengujian lanjutan di POCO F6, Perbandingan perubahan aplikasi, Regresi dan pengukuran emulator Android 15, Reproduksi

### Community 25 - "PdfWordSelection.kt"
Cohesion: 0.19
Nodes (5): PDFTextStripper, pdfHighlightWords(), PdfWordStripper, readPdfWordHits(), readPdfWords()

### Community 26 - "CloudAccessException"
Cohesion: 0.20
Nodes (5): DriveAppDataClient, RemoteFile, nextDriveUploadOffset(), CloudAccessException, DriveHttpException

### Community 27 - "Pemrosesan teks untuk narasi Tuturium"
Cohesion: 0.25
Nodes (7): ICU, CLDR, dan Revo Norm, Implementasi 1.3.24, Pemrosesan teks untuk narasi Tuturium, Penerapan pada pembaca offline, Pengujian host, Rujukan pembaca, Verifikasi Android dan pembaruan

### Community 29 - ".markDirty"
Cohesion: 0.27
Nodes (3): TuturiumCloud, DriveConsentRequired, TuturiumCloudState

### Community 31 - "OfflineVoices.kt"
Cohesion: 0.30
Nodes (6): OfflineVoiceRuntimeTest, refreshOfflineModels(), ModelFile, OfflineCatalog, OfflineModel, OfflineModelStore

### Community 34 - "Audit data, stabilitas, dan transisi Tuturium"
Cohesion: 0.13
Nodes (12): Audit data, stabilitas, dan transisi Tuturium, Bukti pengujian, Jalur penyimpanan, Risiko tersisa dan alasan tidak mengganti arsitektur, Cara mengukur dan kriteria penerimaan, Pola dari produk dan panduan platform, Rujukan audit transisi dan kelancaran Tuturium, Titik audit di Tuturium (+4 more)

### Community 35 - "ReaderPlaybackBar"
Cohesion: 0.05
Nodes (23): SpeedControlSheet(), VerticalSpeedSlider(), newReaderPace(), readerCenterDelta(), formatPlaybackSpeed(), formatPlaybackTime(), playbackTimeCaption(), ReaderPlaybackBar() (+15 more)

### Community 36 - "ReadingDocument"
Cohesion: 0.06
Nodes (11): DriveArchiveTest, LibraryControlsTest, LibraryV2FlowTest, ReaderFlowTest, DriveArchive, ReadingDocument, Section, ReadingTimeline (+3 more)

### Community 40 - "release-notes.md"
Cohesion: 0.20
Nodes (9): Tuturium 1.3.17, Tuturium 1.3.18, Tuturium 1.3.19, Tuturium 1.3.20, Tuturium 1.3.21, Tuturium 1.3.22, Tuturium 1.3.23, Tuturium 1.3.24 (+1 more)

### Community 45 - "supertonic-acceleration.md"
Cohesion: 0.18
Nodes (11): Akselerasi Supertonic 3 di Tuturium, Benchmark host x86-64, Graf ONNX CPU, Implementasi, LiteRT dan XNNPACK, Paralelisme dan pengaturan thread, Pengukuran POCO F6, Pengukuran satu langkah estimator LiteRT/XNNPACK (+3 more)

### Community 46 - "ListeningScreens.kt"
Cohesion: 0.05
Nodes (50): IndonesianVoiceAvatar(), ListeningSettingsV2(), OfflineModelCard(), SkipOptionsSheet(), SwitchRow(), VoiceDetailPanel(), voiceLabel(), VoiceLibraryContent() (+42 more)

### Community 53 - "NarrationOpus.kt"
Cohesion: 0.16
Nodes (3): encodeNarrationOpus(), NarrationResampler, synthesisRanges()

### Community 55 - "Pass"
Cohesion: 0.09
Nodes (16): documentWordEnds(), OriginalPdfPages(), PdfPageImage(), PdfPageSession, pdfWordOutsideViewport(), matchPdfWords(), pdfKey(), buildReaderLayout() (+8 more)

### Community 57 - "ReadingService"
Cohesion: 0.06
Nodes (12): narrationBoostMb(), availableVoices(), choice(), indonesianCandidates(), offlineVoices(), Playback, ReadingService, OfflineSession (+4 more)

### Community 60 - "Supertonic adaptif · Tuturium 1.3.17"
Cohesion: 0.33
Nodes (6): Alternatif yang dipertimbangkan, Antrean dan cache, Metode, Reproduksi, Supertonic adaptif · Tuturium 1.3.17, Verifikasi Android 15

### Community 67 - "IndonesianNumbers.kt"
Cohesion: 0.11
Nodes (14): clockWords(), dateWords(), indonesianNumberReplacements(), romanWords(), spellDigits(), spellInteger(), spellNumber(), underThousand() (+6 more)

### Community 78 - "TuturiumAccountScreen.kt"
Cohesion: 0.16
Nodes (3): AccountDetailRow(), GoogleAvatar(), TuturiumAccountScreen()

### Community 79 - "SettingsScreen.kt"
Cohesion: 0.40
Nodes (6): SettingCard(), SettingsScreen(), UpdateScreen(), VersionBlock(), VoiceOption(), VoiceSlider()

### Community 80 - "GoogleAccountAccess.kt"
Cohesion: 0.22
Nodes (3): driveAuthorization(), googleAccessMessage(), isAccountReauthFailure()

### Community 82 - "ListeningOptions"
Cohesion: 0.22
Nodes (7): ListeningOptions, prepareSpeech(), rampedSpeed(), safePreparedSpeech(), seekBySeconds(), shouldRetrySpeechAtNormalSpeed(), SpeechControlTest

### Community 100 - "StorageScreenV2.kt"
Cohesion: 0.22
Nodes (4): humanSize(), LocalStorage, StorageInspector, StorageV2()

### Community 102 - "DocumentImporter.kt"
Cohesion: 0.05
Nodes (5): GoogleUpdateRegressionTest, DocumentImporter, Release, UpdateCheckException, Updates

### Community 105 - "SynthesizedAudio"
Cohesion: 0.17
Nodes (6): amplifyNarration(), NarrationAudioCache, narrationGain(), AudioCue, SynthesizedAudio, NarrationAudioTest

### Community 110 - "Context"
Cohesion: 0.21
Nodes (6): Alur kode, Graphify, Mengerjakan perubahan, Rilis, Tuturium, PdfMarginStore

## Knowledge Gaps
- **6 isolated node(s):** `DECORATION`, `FOOTER`, `FOOTNOTE`, `HEADER`, `MARGIN` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 415 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **75 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `Alur kode` connect `Context` to `PdfNarration.kt`, `Library`, `DocumentImporter.kt`, `NarrationPipeline`, `ReadingService`?**
  _High betweenness centrality (0.126) - this node is a cross-community bridge._
- **Are the 35 inferred relationships involving `ReadingDocument` (e.g. with `.exportsAndRestoresBooksAndBookmarks()` and `.invalidLaterBookDoesNotRestoreEarlierBookOrApplyDeletion()`) actually correct?**
  _`ReadingDocument` has 35 INFERRED edges - model-reasoned connections that need verification._
- **What connects `DECORATION`, `FOOTER`, `FOOTNOTE` to the rest of the system?**
  _6 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `.play` be split into smaller, more focused modules?**
  _Cohesion score 0.1051693404634581 - nodes in this community are weakly interconnected._
- **Why does `ReadingDocument` connect `ReadingDocument` to `PdfNarration.kt`, `ReaderPlaybackBar`, `Library`, `DocumentImporter.kt`, `SupertonicBuffer`, `ListeningScreens.kt`, `NarrationPipeline`, `Pass`, `.write`, `ReadingService`?**
  _High betweenness centrality (0.110) - this node is a cross-community bridge._
- **Are the 5 inferred relationships involving `ReadingService` (e.g. with `Alur kode` and `narrationBoostMb()`) actually correct?**
  _`ReadingService` has 5 INFERRED edges - model-reasoned connections that need verification._
- **Should `Reader.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.07017543859649122 - nodes in this community are weakly interconnected._