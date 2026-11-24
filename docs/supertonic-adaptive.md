# Supertonic adaptif · Tuturium 1.3.17

## Metode

Paket INT8 Sherpa-ONNX 1.13.4 tetap sama dengan 1.3.16. Benchmark memakai AMD EPYC 9V74 x86_64 dengan tiga CPU terlihat dan kuota dua vCPU. Setiap baris mengukur enam frasa bahasa Indonesia dengan F1 dan M1, model sudah dimuat, dan seed sintesis tetap. WAV host berasal dari keluaran model sebelum penguatan audio Android. RTF adalah detik komputasi per detik audio dengan median antarfrasa. Throughput adalah total durasi audio dibagi waktu seluruh batch. Pada sintesis 1×, buffer 4× butuh throughput yang berkelanjutan di atas 4 detik audio per detik komputasi.

Whisper Tiny multibahasa dipakai hanya pada host untuk memeriksa transkripsi. Skor melaporkan edit kata mentah dari 88 kata, termasuk penulisan angka, ejaan nama, dan pemisahan kata. Skor yang tinggi pada baseline memperlihatkan keterbatasan recognizer dan sampel. Uji dengar manusia diperlukan untuk menilai pelafalan, naturalitas, dan kejernihan pada laju dengar 4×.

| Thread × instans | Langkah | RTF | Throughput | Edit kata / 88 |
|---|---:|---:|---:|---:|
| 3 × 1, baseline 1.3.16 | 8 | 0,465 | 2,30 | 38 |
| 2 × 1 | 8 | 0,319 | 3,49 | — |
| 2 × 1 | 6 | 0,232 | 4,75 | 26 |
| 2 × 1 | 5 | 0,204 | 5,44 | 37 |
| 2 × 1 | 4 | 0,157 | 6,33 | 37 |
| 2 × 1 | 3 | 0,122 | 7,93 | 66 |
| 2 × 1 | 2 | 0,092 | 10,70 | 85 |
| 1 × 1 | 4 | 0,228 | 4,27 | — |
| 3 × 1 | 4 | 0,258 | 4,14 | — |
| 4 × 1 | 4 | 0,376 | 2,65 | — |
| 1 × 2 | 4 | 0,235 | 8,21 | — |

Empat langkah melewati target RTF 0,20 pada host, tetapi skor agregat menyembunyikan kasus pengulangan suku kata dan akhir frasa yang hilang pada transkripsi. Kondisi memburuk pada dua dan tiga langkah. Lima langkah menjadi batas saat antrean tertekan, dengan enam atau delapan langkah saat cadangan cukup. Perubahan konfigurasi diproses pada batas potongan. Model, pembicara, volume narasi, urutan teks, dan sinkronisasi penanda tetap memakai jalur yang sudah ada.

## Antrean dan cache

Replay 120 potongan menggunakan durasi audio dan waktu sintesis yang diukur dari matriks di atas. Replay memodelkan waktu mulai, konsumsi, pengisian, dan pemulihan antrean. Media3 dan codec perangkat diuji terpisah di Android. Dampak muat dingin, pramuat sebelum Play, dan tekanan termal perlu diukur pada perangkat.

| Laju dengar | Mulai baseline → adaptif | Antrean kosong baseline → adaptif | Waktu menunggu baseline → adaptif |
|---|---:|---:|---:|
| 2× | 14,65 → 3,15 detik | 0 → 0 | 0 → 0 detik |
| 3× | 19,68 → 4,39 detik | 12 → 0 | 71,33 → 0 detik |
| 4× | 21,60 → 6,33 detik | 12 → 0 | 146,86 → 0 detik |

Estimasi throughput memakai EWMA dan melewati cache hit. Setelan awal mempertimbangkan laju dengar dan kebutuhan cadangan sebelum audio pertama. Target antrean berkurang ketika sintesis punya banyak ruang atau Android melaporkan panas. Batas keras adalah 750 detik PCM sumber atau 384 klip, sekitar 63 MiB PCM mono 44,1 kHz plus satu klip. Pemulihan menunggu cadangan sesuai laju sebelum melanjutkan potongan berikutnya.

Opus 32 kbps pada enam WAV lima-langkah menghasilkan median **24,0×** pengurangan ukuran melalui FFmpeg dan 31 edit kata setelah dekode ke 44,1 kHz, dibanding 37 untuk PCM. Pada 48, 64, dan 96 kbps, pengurangan median 15,6×, 11,0×, dan 7,4× dengan 32, 33, dan 33 edit kata. Angka ini berlaku untuk libopus host. Android memakai encoder platform pada API 29+, dengan PCM 44,1 kHz disampel ulang ke 48 kHz untuk kompatibilitas codec. Kompresi hanya berjalan saat suhu di bawah sedang, cadangan sedikitnya 18 detik, sintesis mempunyai ruang, dan klip berdurasi 2–30 detik. Audio pertama tetap PCM. Cache Opus dan PCM menyimpan penanda yang sama; saat encoder tidak tersedia atau melewati tenggat, cache kembali ke PCM. Cache tetap privat dengan kuota 96 MiB. [Format MediaMuxer Android](https://developer.android.com/reference/android/media/MediaMuxer)

## Verifikasi Android 15

61 tes JVM lulus. Empat tes Supertonic di emulator menguji seluruh 10 suara, urutan dan penanda pada 2× serta 4×, pengisian ulang setelah antrean sengaja dikosongkan, dan pemutaran Opus yang dipulihkan dari cache pada 4×. Pada satu klip 17,5 detik, PCM 1.543.640 byte menjadi Opus 92.277 byte, pengurangan **16,7×**. Pengambilan dari cache membutuhkan **3 ms**. Sintesis dan kompresi pertama bersama-sama membutuhkan **4.771 ms** pada emulator. [Log tes audio Android](https://github.com/WynnDev-rill/Tuturium/actions/runs/45388228391)

APK ARM64 bertanda tangan berhasil dipasang di atas 1.3.13 pada emulator dengan translasi ARM64. Waktu pemasangan awal dipertahankan dan dokumen lama dapat dibuka. [Log uji pemasangan](https://github.com/WynnDev-rill/Tuturium/actions/runs/45389032278)

## Alternatif yang dipertimbangkan

- Native 1,2×, 1,5×, dan 2× pada empat langkah menghasilkan 49, 60, dan 82 edit kata per 88 kata, dibanding 37 di 1×. Sintesis tetap 1× dengan perubahan tempo di Media3. Ini juga mempertahankan penggunaan cache saat kecepatan dipilih ulang. [Kontrol kecepatan model](https://github.com/supertone-oss-archive/supertonic/blob/main/py/README.md)
- Potongan 90 dan 120 karakter menghasilkan RTF 0,158 dan 0,172 dengan 42 edit kata masing-masing. Batas 180 karakter mencapai 0,157 dengan 37 edit pada konfigurasi yang sama. Pemenggalan yang ada dipertahankan agar batas kalimat dan penanda stabil.
- Pada pengukuran ulang lima langkah, serial dua thread menghasilkan RTF 0,212, throughput 4,51, dan puncak RSS 374 MiB. Dua pekerja satu thread dengan sesi yang sama menghasilkan RTF 0,298, throughput 6,72, dan RSS 437 MiB. Dua instans terpisah menghasilkan RTF 0,297, throughput 6,44, dan RSS 634 MiB. Paralelisme menaikkan throughput batch sekaligus menambah latensi per potongan dan kebutuhan memori. Aplikasi memakai satu instans dengan 1 thread bila Android melaporkan 1 CPU, 2 thread bila 2–3 CPU, dan 3 thread pada perangkat yang melaporkan lebih banyak. Mengganti jumlah thread saat berjalan mengharuskan pemuatan ulang sesi inferensi. Autotuning dan paralelisme pada perangkat fisik masih perlu diukur.
- QNN/HTP memerlukan runtime, graf, dan bentuk input yang sesuai, termasuk bentuk statis pada jalur HTP. XNNPACK mempercepat operator yang dapat ditempatkan pada provider tersebut. NNAPI telah dideprekasi pada Android 15. Build CPU ARM64 yang ada dipertahankan, dan data ini belum menunjukkan kernel NEON/MLAS yang aktif pada operator tertentu di POCO F6. [QNN](https://onnxruntime.ai/docs/execution-providers/QNN-ExecutionProvider.html) · [XNNPACK](https://onnxruntime.ai/docs/execution-providers/Xnnpack-ExecutionProvider.html) · [NNAPI](https://onnxruntime.ai/docs/execution-providers/NNAPI-ExecutionProvider.html) · [MLAS](https://onnxruntime.ai/docs/performance/kleidiai/)

## Reproduksi

Gunakan model INT8 yang dipatok pada [benchmark 1.3.12](supertonic-benchmark.md) dan Sherpa-ONNX 1.13.4. Untuk skor otomatis, sediakan hanya berkas encoder INT8, decoder INT8, dan token dari [Whisper Tiny multibahasa Sherpa](https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-whisper-tiny.tar.bz2).

```bash
OPENBLAS_NUM_THREADS=1 python scripts/benchmark-adaptive-supertonic.py /path/to/model --threads 2 --steps 5 --wav /tmp/s5
OPENBLAS_NUM_THREADS=1 python scripts/benchmark-adaptive-supertonic.py /path/to/model --asr /path/to/whisper-tiny --wav /tmp/s5
OPENBLAS_NUM_THREADS=1 python scripts/benchmark-adaptive-supertonic.py /path/to/model --trace /path/to/matrix.jsonl
```

Untuk replay, gabungkan keluaran JSON konfigurasi 3 thread/8 langkah dan 2 thread/5, 6, 8 langkah di satu JSONL. Uji POCO F6 fisik masih diperlukan untuk mengukur RTF ARM64, total rebuffer, baterai, suhu, dan sesi latar yang panjang.
