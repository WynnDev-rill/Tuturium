# Akselerasi Supertonic 3 di Tuturium

## Referensi dan kompatibilitas

Rilis 1.3.22 memakai paket Supertonic 3 INT8 dari Sherpa-ONNX dan runtime Android Sherpa 1.13.4. Paket menyimpan empat graf ONNX, indeks Unicode, konfigurasi, dan gaya 10 suara. Jalur produksi memakai CPU dan menyintesis audio pada 1×. Media3 mengubah tempo putar, sehingga klip bisa dipakai ulang saat pengguna mengganti kecepatan. [Paket dasar Sherpa](https://huggingface.co/csukuangfj2/sherpa-onnx-supertonic-3-tts-int8-2026-05-11) · [Runtime Sherpa 1.13.4](https://github.com/k2-fsa/sherpa-onnx/releases/tag/v1.13.4)

### Graf ONNX CPU

Kedua kandidat mengubah konvolusi satu dimensi dengan kernel 1, satu grup, tanpa padding, dan stride 1 menjadi transpose, perkalian matriks, penambahan bias bila ada, dan transpose kembali. Estimator dijalankan pada setiap langkah inferensi. Mengurangi biaya estimator dapat membantu mempertahankan langkah 6–8 tanpa mengubah alur Media3 atau pemetaan penanda pembacaan. Hasil numerik dan kejernihan bahasa Indonesia perlu diukur sesudah kuantisasi. [Skrip Wilbur](https://huggingface.co/WilburDev/supertonic-3-mobile/blob/2d53d752a6e9a1db1bd0f63fe36806dc6d1a3ee5/conv2mm.py) · [Skrip Alexey](https://huggingface.co/AlexeyNe/supertonic-3-android-onnx/blob/49100f077ce142d69cbf72c8800881dbbc106001/build-supertonic-pack.py)

| Kandidat | Revisi | Perubahan yang bisa diuji pada runtime yang sama |
|---|---|---|
| WilburDev | `2d53d752a6e9a1db1bd0f63fe36806dc6d1a3ee5`, 28 Sep 2026 | Hanya estimator berbeda dari paket Tuturium. Skrip kuantisasi dinamis INT8 per kanal menerima MatMul dengan bobot konstan berdimensi dua. Estimator 65.967.881 byte dan vokoder dasar dipertahankan. |
| AlexeyNe | `49100f077ce142d69cbf72c8800881dbbc106001`, 9 Okt 2026 | Kuantisasi dinamis INT8 hanya diterapkan pada MatMul hasil konversi estimator. Vokoder juga memakai Conv-ke-MatMul. Vokoder dan enkoder menyimpan bobot FP16 yang dikonversi ke FP32 untuk eksekusi. Perubahan ini perlu diuji terpisah dari efek kuantisasi estimator. |

Prediktor durasi, konfigurasi, indeks Unicode, dan gaya suara punya hash yang sama pada ketiga paket. Enkoder dan vokoder Wilbur juga sama dengan baseline. Kedua paket kandidat menyertakan lisensi BigScience OpenRAIL-M. Pertahankan pemberitahuan lisensi dan syarat penggunaan dari model dasar jika mendistribusikan varian. [Manifest Wilbur](https://huggingface.co/api/models/WilburDev/supertonic-3-mobile?blobs=true) · [Manifest Alexey](https://huggingface.co/api/models/AlexeyNe/supertonic-3-android-onnx?blobs=true) · [Lisensi model dasar](https://huggingface.co/Supertone/supertonic-3/blob/main/LICENSE)

| Berkas kandidat | Byte | SHA-256 |
|---|---:|---|
| Wilbur `vector_estimator.int8.onnx` | 65.967.881 | `6fd0f1637a0192ec2ec160a899045d8138395100a3c25414134adf76abd3f57b` |
| Alexey `text_encoder.onnx` | 18.474.644 | `16ae0c53f37330120980fd8103e78f0a52cdc38909d75e15148f97093de462d0` |
| Alexey `vector_estimator.onnx` | 80.449.124 | `cf3d5993e3d736a6c53103a3c0b5112581c4a6f370696188854f26080232cca0` |
| Alexey `vocoder.onnx` | 50.819.161 | `1f42e348929559624fd8c65a7553cc28d87525f79b935d78c86183c701aa293c` |

### LiteRT dan XNNPACK

Proyek Supertonic-LiteRT-TTS menggabungkan runtime LiteRT 2.2 kustom, XNNPACK, dan port Soniqo speech-core. Varian Multi-P memakai 49 bentuk statis dari kombinasi panjang teks dan laten. Runtime memilih bentuk yang sesuai, menyiapkan subgraf, dan menyimpan bobot yang sudah dikemas. Waktu menyiapkan bentuk baru perlu diukur terpisah dari sintesis setelah cache siap. Empat graf W8-AFP32 tetap berjumlah 138,8 MB, sedangkan empat graf W8-AFP32 Multi-P berjumlah 243,8 MB sebelum data suara, manifest, dan cache runtime. Proyek menetapkan minimum API 26 dan menyertakan target ARM64 serta x86-64. [Kode sumber tertambat](https://github.com/marv435624q/Supertonic-LiteRT-TTS/tree/b02f553bdc9e2c74f0d44d7b6d4fc9ff394919cb) · [Manifest model tetap](https://huggingface.co/api/models/q3146dq4/Supertonic-3-LiteRT-WI8-AFP32?blobs=true) · [Manifest model Multi-P](https://huggingface.co/api/models/q3146dq4/Supertonic-3-LiteRT-Static-MultiPreset-GELU-WI8-AFP32?blobs=true)

Provider XNNPACK di ONNX Runtime merupakan jalur lain. Dokumentasi operator ONNX XNNPACK membatasi `Conv` dan `ConvTranspose` ke 2D serta `MatMul` ke operasi 2D. Cakupan graf model harus diprofilkan sebelum mengandalkan provider tersebut untuk konvolusi temporal atau perkalian matriks dengan dimensi batch. Pool thread XNNPACK terpisah dari pool ONNX Runtime, sehingga jumlah thread dan spinning perlu diukur bersama. [ONNX Runtime XNNPACK](https://onnxruntime.ai/docs/execution-providers/Xnnpack-ExecutionProvider.html)

Subpohon speech-core memuat Apache 2.0. Revisi aplikasi yang diperiksa tidak memuat lisensi di akar repositori. Hak untuk memakai ulang bagian aplikasi lain dan lisensi bobot tiap varian perlu diperiksa sebelum menyalin kode atau membundel model. Jalur CPU LiteRT pada proyek tersebut memakai kode JNI dan konfigurasi model tersendiri. [Lisensi subpohon speech-core](https://github.com/marv435624q/Supertonic-LiteRT-TTS/blob/b02f553bdc9e2c74f0d44d7b6d4fc9ff394919cb/speech-core/LICENSE) · [ModelManager](https://github.com/marv435624q/Supertonic-LiteRT-TTS/blob/b02f553bdc9e2c74f0d44d7b6d4fc9ff394919cb/sdk/src/main/kotlin/audio/soniqo/speech/ModelManager.kt)

### Qualcomm QNN dan HTP

QNN HTP memerlukan dukungan runtime serta firmware perangkat, versi pustaka Qualcomm yang cocok, bentuk input statis, dan cakupan operator untuk tipe setiap tensor. ONNX Runtime menyebut dukungan khusus untuk kombinasi tipe kuantisasi pada `MatMul` HTP. Contoh resmi LiteRT mencantumkan Snapdragon 8 Gen 3 SM8650. POCO F6 memakai Snapdragon 8s Gen 3, sehingga kompatibilitas delegate dan throughput berkelanjutan memerlukan pengujian pada unit tersebut. [Syarat QNN HTP](https://onnxruntime.ai/docs/execution-providers/QNN-ExecutionProvider.html#model-requirements) · [Delegate Qualcomm untuk LiteRT](https://developers.google.com/edge/litert/android/npu/qualcomm) · [Spesifikasi POCO F6](https://www.po.co/global/product/poco-f6/specs/)

Varian eksternal W8A16 memakai graf ONNX QDQ khusus dan cache konteks untuk bentuk input yang dipilih. Empat graf mentahnya berjumlah 106,8 MB di luar gaya suara dan cache. Jalur LiteRT NPU proyek yang sama memakai delegate pada subgraf estimator bentuk T64/L64 di jalur pratinjau, dengan pemeriksaan numerik terhadap CPU dan fallback CPU bila perlu. Pengukuran penerbit pada Snapdragon 8 Elite Gen 5 berlaku setelah cache dan bucket siap. Pengujian terpisah diperlukan untuk bahasa Indonesia, batas panjang teks, konsumsi baterai, suhu, dan fallback pada POCO F6. [Manifest W8A16](https://huggingface.co/api/models/q3146dq4/supertonic-3-w8a16-qnn-qdq?blobs=true) · [Dokumentasi jalur QNN dan LiteRT](https://github.com/marv435624q/Supertonic-LiteRT-TTS/tree/b02f553bdc9e2c74f0d44d7b6d4fc9ff394919cb)

### Paralelisme dan pengaturan thread

Thread per instans dan jumlah instans sama-sama memakai anggaran CPU. Pengukuran perlu membedakan RTF per potongan, throughput seluruh antrean, latensi mulai, penggunaan RAM puncak, dan pengisian ulang saat layar terkunci. Referensi Tuturium 1.3.17 mengukur kenaikan throughput dan latensi potongan saat dua pekerja dipakai pada host dua vCPU, serta tambahan RAM ketika dua instans memakai sesi berbeda. Hasil itu berguna untuk memilih kandidat uji. Kebijakan Android perlu mempertimbangkan suhu dan menghindari autotuning yang memuat ulang sesi sambil audio aktif. [Riset Tuturium 1.3.17](supertonic-adaptive.md)
