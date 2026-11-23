# Tuturium

Pembaca dokumen Android native dengan pustaka lokal dan text-to-speech offline. Tuturium gratis, tanpa akun atau kuota baca.

## Pasang dan gunakan

Unduh `Tuturium.apk` dari [GitHub Releases](https://github.com/WynnDev-rill/Tuturium/releases/latest). Android 8.0 (API 26) atau yang lebih baru diperlukan.

- Impor PDF, EPUB, DOCX atau TXT dari pemilih dokumen, atau tambahkan teks dari clipboard dan menu Berbagi.
- Buka dokumen di Pustaka untuk membaca, memutar, mencari teks, melompat ke bagian, atau menyimpan markah.
- Atur suara, kecepatan, nada, ukuran teks, dan tema dari Setelan. Mode otomatis memilih suara Indonesia offline dan mendahulukan `local(idd)` atau varian lokal `idd` jika tersedia. Unduh paket suara Indonesia di setelan TTS Android jika belum terpasang.
- Lanjutkan audio ketika layar terkunci. Gunakan putar/jeda dan tombol lompat dari notifikasi media atau dari pembaca. Pengatur waktu tidur tersedia melalui menu pembaca.
- Tuturium memeriksa rilis stabil di GitHub saat aplikasi dibuka dan menyediakan pemeriksaan manual di Setelan. Pengunduhan dan pemasangan dimulai dengan tindakan pengguna. Android meminta konfirmasi pemasangan.

Isi yang dibaca merupakan teks yang diekstrak. EPUB tanpa DRM mengikuti urutan spine, DOCX mengambil isi utama, dan PDF ditampilkan sebagai teks per halaman. Untuk halaman PDF tanpa teks, model OCR lokal mengenali tulisan Latin. Kuota perangkat dan keterbacaan gambar memengaruhi OCR. Jumlah maksimum dokumen impor adalah 100 MB, hingga 1.500 halaman PDF atau bagian EPUB, dan hingga dua juta karakter hasil ekstraksi. Mesin TTS yang menyediakan rentang ucapan memungkinkan sorotan kata. Mesin lain menggunakan sorotan kalimat.

## Build dan pengujian

Gunakan JDK 17, Android SDK Platform 35, dan Build Tools 35.0.0. Atur `ANDROID_HOME` atau `sdk.dir` di `local.properties`.

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
./gradlew connectedDebugAndroidTest
```

Suite JVM mencakup pemenggalan kalimat/Unicode, prioritas suara, dan versi pembaruan. Suite Android mengimpor empat format, memeriksa OCR dari PDF pindai, penyimpanan posisi dan markah, serta navigasi pembaca. Untuk uji perangkat, pasang suara Indonesia offline, nyalakan mode pesawat, putar dokumen, kunci layar, lalu gunakan jeda/lanjut dari kontrol media. Uji pembaruan dengan dua APK yang memakai sertifikat sama dan menaikkan `versionCode` serta `versionName`.

## Menandatangani dan menerbitkan

Simpan keystore PKCS#12 dan password di luar repositori. Alias produksi adalah `tuturium`, dengan password key sama seperti password keystore. Gunakan keystore yang sama untuk semua pembaruan aplikasi.

```bash
export TUTURIUM_KEYSTORE=/path/to/Tuturium.p12
read -rs TUTURIUM_STORE_PASSWORD
export TUTURIUM_STORE_PASSWORD
./gradlew testDebugUnitTest lintRelease assembleRelease
"$ANDROID_HOME/build-tools/35.0.0/apksigner" verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk
```

Di GitHub Actions, simpan isi keystore yang dikodekan Base64 sebagai `TUTURIUM_KEYSTORE_BASE64` dan password sebagai `TUTURIUM_STORE_PASSWORD`. Naikkan `versionCode` dan `versionName`, perbarui `docs/release-notes.md`, dan dorong tag `vMAJOR.MINOR.PATCH`. Workflow menyiapkan APK rilis, memverifikasi sidik jari publik, membuat rilis draft dengan APK serta checksum, lalu menerbitkannya. Nama aset APK untuk updater harus `Tuturium.apk`.

## Desain

Lihat [riset produk dan library](docs/research.md), [desain yang dihasilkan UI UX Pro Max](docs/ui-ux-pro-max.md), dan [instruksi agen](AGENTS.md). UI UX Pro Max dipasang permanen melalui `.agents/skills/ui-ux-pro-max` dan digunakan saat mengembangkan desain Compose.

```bash
python3 .agents/skills/ui-ux-pro-max/scripts/search.py \
  'mobile audiobook reading library editorial minimalist accessible offline text to speech' \
  --design-system --density 4 --motion 2 -p Tuturium -f markdown
```
