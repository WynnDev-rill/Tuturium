# Benchmark Supertonic 3 · Tuturium 1.3.12

Pengujian lokal menggunakan Sherpa-ONNX 1.13.4 dengan CPU execution provider pada AMD EPYC 9V74 x86_64. Kontainer mengekspos tiga CPU dengan kuota dua vCPU. Setiap konfigurasi memakai tiga frasa bahasa Indonesia, tiga ulangan setelah pemanasan, pembicara F1 dan M1, bahasa `id`, serta sintesis 1×. Angka median berasal dari sembilan keluaran per konfigurasi. RTF adalah detik komputasi per detik audio. Untuk mengimbangi pemutaran 4× tanpa cadangan, RTF berkelanjutan perlu di bawah 0,25.

| Model | Thread | Langkah | Muat model (ms) | Sintesis pertama (ms) | Median sintesis (ms) | Median RTF |
|---|---:|---:|---:|---:|---:|---:|
| INT8 rilis 1.3.11 | 3 | 8 | 775 | 2.166 | 3.226 | 0,404 |
| INT8 rilis | 1 | 8 | 528 | 1.650 | 3.589 | 0,450 |
| INT8 rilis | 2 | 8 | 472 | 1.395 | 2.032 | 0,266 |
| INT8 rilis | 4 | 8 | 543 | 4.033 | 4.872 | 0,732 |
| INT8 rilis | 3 | 5 | 503 | 1.308 | 2.215 | 0,277 |
| WilburDev | 3 | 8 | 570 | 742 | 1.217 | 0,165 |
| AlexeyNe | 3 | 8 | 745 | 919 | 1.440 | 0,180 |
| WilburDev | 3 | 5 | 480 | 506 | 898 | 0,114 |
| AlexeyNe | 3 | 5 | 1.095 | 579 | 1.023 | 0,128 |

Pemaksaan empat thread pada kuota dua vCPU memperburuk hasil. Angka dua thread juga bergantung pada kuota tersebut. Kedua graf alternatif menghasilkan PCM terbaca dan durasi mendekati baseline pada frasa uji. Durasi dan pemeriksaan PCM tidak mengukur pengucapan, intonasi, atau kualitas perseptual. Ukuran total graf ONNX masing-masing 144,5 MB untuk baseline, 132,1 MB untuk WilburDev, dan 153,4 MB untuk AlexeyNe, sebelum kompresi. WilburDev menjadi kandidat utama uji ARM64 karena RTF 59% lebih rendah pada delapan langkah di host dan ukuran graf paling kecil. Build produksi tetap menggunakan INT8 asli, tiga thread, dan delapan langkah untuk menjaga karakter suara yang sudah dipakai.

## Perbandingan perubahan aplikasi

| Ukuran | 1.3.11 | 1.3.12 |
|---|---|---|
| RTF sintesis PCM mentah dengan model rilis, host | 0,404 median | 0,404 sebagai acuan model dan konfigurasi yang sama. Penguatan PCM dan cache diukur terpisah. |
| Cadangan 4× sebelum batas antrean | 48 detik atau 56 klip | 180–300 detik dengan batas 384 klip. Saat suhu parah, target turun ke 60 detik. |
| Mengulang potongan yang sudah ada | Sintesis ulang | Cache PCM mengembalikan WAV dan penanda waktu yang sama. Cache dikeluarkan dari estimasi kecepatan inferensi. |
| Narasi sebelum skala volume pengguna | PCM mentah | Median RMS **+8,18 dB** pada sembilan sampel baseline. Puncak maksimum 0,878 skala penuh setelah pembatas. |
| Waktu muat model pada host | 775 ms untuk memuat model pada konfigurasi baseline, di luar waktu sintesis pertama | Pemuatan dapat dimulai saat pembaca dibuka. Penghematan waktu yang dirasakan bergantung pada jeda sebelum menekan Play. |

Cadangan menargetkan waktu dengar, bukan durasi file sebelum dipercepat. Target suhu normal adalah 3 menit dan menjadi 5 menit jika waktu sintesis mendekati laju konsumsi. Pada suhu sedang target kembali ke 3 menit. Setelah antrean habis, pemutar kembali dari potongan pertama yang belum diucapkan setelah ada cadangan atau input telah selesai. Media3 memakai satu daftar putar selama sesi. Cache lokal maksimal 96 MiB dan mengabaikan entri yang tidak dipakai selama tujuh hari. Perubahan suara, model, teks, atau algoritme penguatan menghasilkan kunci cache berbeda. Kecepatan dan nada diterapkan saat playback sehingga audio bisa dipakai ulang.

## Kandidat akselerasi

| Jalur | Bukti dan keputusan |
|---|---|
| INT8/ONNX dan Conv→MatMul | Ketiga varian berjalan pada CPU host. Port WilburDev dan AlexeyNe mengubah konvolusi pada vector estimator menjadi operasi matriks. Uji A/B ARM64 dengan teks Indonesia diperlukan sebelum mengganti graf rilis. |
| ARM64/MLAS/NEON | APK memuat pustaka Sherpa ARM64. Keberadaan biner ARM64 tidak menetapkan microkernel yang dipilih per operator. Perlu uji native pada Snapdragon untuk memastikan manfaat dan stabilitas termal. |
| XNNPACK | Dokumentasi ORT membatasi cakupan operator dan tipe data. Registrasi provider dan partisi graf perlu dibuktikan pada build Android, kemudian dibandingkan dengan CPU penuh. |
| Qualcomm QNN/HTP | Memerlukan build provider dan backend yang kompatibel dengan perangkat serta graf yang memenuhi bentuk dan tipe operator QNN. Tidak ada pengukuran NPU pada host ini. |
| LiteRT, GPU/Vulkan | Memerlukan konversi atau runtime alternatif, serta pengukuran bagian graf yang fallback ke CPU. Jalur ini belum memiliki bukti kesetaraan bahasa Indonesia atau percepatan pada POCO F6. |

Sumber teknis: [Model INT8 Sherpa](https://huggingface.co/csukuangfj2/sherpa-onnx-supertonic-3-tts-int8-2026-05-11/tree/cca5a0e6c96e1d2c720986bf7e75fcc81dee3ae4), [WilburDev](https://huggingface.co/WilburDev/supertonic-3-mobile/tree/2d53d752a6e9a1db1bd0f63fe36806dc6d1a3ee5), [AlexeyNe](https://huggingface.co/AlexeyNe/supertonic-3-android-onnx/tree/49100f077ce142d69cbf72c8800881dbbc106001), [ORT Mobile](https://onnxruntime.ai/docs/tutorials/mobile/), [MLAS/KleidiAI](https://onnxruntime.ai/docs/performance/kleidiai/), [XNNPACK](https://onnxruntime.ai/docs/execution-providers/Xnnpack-ExecutionProvider.html), [QNN](https://onnxruntime.ai/docs/execution-providers/QNN-ExecutionProvider.html), [LiteRT GPU](https://ai.google.dev/edge/litert/android/gpu).

## Reproduksi

Unduh ketujuh berkas model dari masing-masing revisi di atas. Gunakan `tts.json`, `unicode_indexer.bin`, dan `voice.bin` baseline jika port hanya menyediakan graf. Simpan baseline dan port di direktori berbeda.

```bash
uv venv /tmp/tuturium-bench
uv pip install --python /tmp/tuturium-bench/bin/python sherpa-onnx==1.13.4 numpy==2.4.4
OPENBLAS_NUM_THREADS=1 /tmp/tuturium-bench/bin/python scripts/benchmark-supertonic.py /path/to/baseline --threads 3 --steps 8
```

Ulangi secara serial dengan thread dan langkah pada tabel. Skrip menghitung waktu inferensi dan penguatan pada PCM nyata. Log regresi Android untuk cache bisa dibaca dengan `adb logcat -d -s TuturiumVoiceBenchmark:I`. Uji ini memeriksa bahwa cache mempertahankan byte WAV dan penanda waktu tepat. Uji pemutaran memeriksa urutan potongan dan jangkauan sorotan pada 2× dan 4×. Skenario pemulihan terpisah sengaja memperlambat produsen untuk mengosongkan antrean.

## Pengujian lanjutan di POCO F6

Gunakan APK 1.3.11 dan 1.3.12, suara, dokumen, skala volume Android, dan suhu awal yang sama. Jalankan sedikitnya tiga kali per laju 1×, 2×, dan 4×, dengan cache dingin lalu cache hangat. Rekam jeda Play ke audio, jumlah serta durasi rebuffer, dan urutan teks terdengar. Ulangi dengan layar dikunci minimal 20 menit dan dengan musik latar. Catat suhu melalui `dumpsys thermalservice`, konsumsi melalui `dumpsys batterystats`, dan frame UI melalui Perfetto FrameTimeline atau `scripts/profile-android.sh`. Bandingkan graf alternatif dengan fonem Indonesia, nama, angka, singkatan, dan kalimat panjang menggunakan uji dengar dengan level volume setara. Hasil host dan emulator tidak mengukur FPS, konsumsi daya, suhu, atau pengalaman audio pada POCO F6.
