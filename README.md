# Tuturium

Pembaca dokumen Android native dengan pustaka lokal dan text-to-speech bahasa Indonesia melalui Google TTS (online maupun offline). Tuturium gratis, tanpa akun atau kuota baca.

## Pasang dan gunakan

Unduh `Tuturium.apk` dari [GitHub Releases](https://github.com/WynnDev-rill/Tuturium/releases/latest). Android 8.0 (API 26) atau yang lebih baru diperlukan.

- Impor PDF, EPUB, DOCX atau TXT dari pemilih dokumen, atau tambahkan teks dari clipboard dan menu Berbagi.
- Buka dokumen di Pustaka untuk membaca, memutar, mencari teks, melompat ke bagian, atau menyimpan markah.
- Atur suara, kecepatan, nada, ukuran teks, dan tema dari Setelan. Pilih Google TTS atau unduh Supertonic 3 untuk menggunakan sepuluh suara bahasa Indonesia secara luring. Setiap suara bisa dibuka untuk pratinjau, favorit, dan pemilihan. Mode otomatis memprioritaskan suara Indonesia lokal jika tersedia. Pilihan online memerlukan internet, dan pilihan suara bergantung pada paket mesin Google TTS pada perangkat.
- Lanjutkan audio ketika layar terkunci. Gunakan putar/jeda dan tombol lompat dari notifikasi media atau dari pembaca. Pengatur waktu tidur tersedia melalui menu pembaca.
- Tuturium memeriksa rilis stabil di GitHub saat aplikasi dibuka dan menyediakan pemeriksaan manual di Setelan. Pengunduhan dan pemasangan dimulai dengan tindakan pengguna. Android meminta konfirmasi pemasangan.

Tuturium membacakan teks hasil ekstraksi. EPUB tanpa DRM mengikuti urutan spine, DOCX mengambil isi utama, dan PDF menyediakan pilihan **PDF** (halaman asli jika berkasnya tersimpan) dengan gulir vertikal atau **Teks** (paragraf rapi dengan sorotan dan pemilihan kata yang langsung dibacakan). Tampilan Teks mengikuti tema gelap/terang aplikasi, dan tombol kata aktif memudahkan kembali ke posisi narasi setelah menggulir jauh. Untuk halaman PDF tanpa teks, model OCR lokal mengenali tulisan Latin. Kuota perangkat dan keterbacaan gambar memengaruhi OCR. Ukuran maksimum dokumen impor 100 MB. PDF tidak memiliki batas jumlah halaman atau karakter tetap; kemampuan memuat tetap bergantung pada RAM dan ruang penyimpanan. EPUB dan format teks lain mempertahankan pembatas keamanan. Mesin TTS yang menyediakan rentang ucapan memungkinkan sorotan kata. Mesin lain menggunakan sorotan kalimat.

## Supertonic 3

Tuturium menyintesis teks pada 1×, lalu Media3 mengubah kecepatan pemutaran. Cadangan audio mengikuti laju baca, kecepatan sintesis, dan suhu perangkat. Audio yang sudah dibaca dapat dipakai ulang dari cache lokal, yang bisa dibersihkan dari menu Penyimpanan. Audio baru memakai penguatan otomatis dengan pembatas puncak. Lihat [hasil benchmark dan prosedur pengujian POCO F6](docs/supertonic-benchmark.md).

Rilis 1.3.17 memakai 5–8 langkah adaptif dan cache Opus bersyarat. Perbandingan RTF, kejernihan, dan simulasi antrean tersedia di [benchmark Supertonic adaptif](docs/supertonic-adaptive.md).

Di pembaca, buka **Opsi pembaca → Tampilan** untuk mengatur tema, warna sorotan, font, dan mode Standar, Sepia, atau Fokus. Untuk mengatur volume narasi saat memutar musik dari aplikasi lain, buka **Opsi pembaca → Audio**.

## Aktivasi rilis otomatis (sekali saja)

GitHub Actions memerlukan dua secret privat `TUTURIUM_KEYSTORE_BASE64` dan `TUTURIUM_STORE_PASSWORD`. Berkas `Tuturium-signing-key.zip` yang asli berisi kedua bahan tersebut; jangan unggah ke repository publik atau kirim nilainya melalui issue.

Cara cepat melalui Termux atau terminal yang telah login dengan `gh auth login`:

```bash
bash scripts/setup-signing-secrets.sh /lokasi/Tuturium-signing-key.zip
```

Skrip mengunggah nilai sebagai GitHub Actions secrets secara privat, tidak mencetak atau menyimpan nilai dalam repo, kemudian memicu build bertanda tangan. Prasyarat: `gh`, `unzip`, dan `base64`. Alternatif: atur dua nilai tersebut melalui [Repository Actions secrets](https://github.com/WynnDev-rill/Tuturium/settings/secrets/actions), lalu pilih **Actions → Signed Android release → Run workflow** pada `main` tanpa mengisi tag.

Setelah berhasil, rilis terbaru ada di [GitHub Releases](https://github.com/WynnDev-rill/Tuturium/releases/latest) dan dapat diperiksa dari menu **Profil → Pembaruan aplikasi**. Versi baru harus memakai sertifikat yang sama agar bisa memperbarui instalasi yang sudah ada tanpa menghapus data.

## Akun Google dan pembaruan 1.3.6

Di **Profil → Akun**, pilih **Masuk dengan Google**. Tuturium meminta izin Google Drive saat diperlukan dan memeriksa koneksi secara otomatis. Ketuk **Penyimpanan Google Drive** untuk melihat status dan waktu sinkronisasi terakhir. Tuturium tetap dapat membaca dan menyimpan buku sebagai tamu. Gunakan akun yang membuat proyek jika audience OAuth masih Personal. Lihat [audit OAuth dan pengujian perangkat](docs/google-oauth-audit.md).

## Pembaruan 1.3.5 — Cloud-first Google Drive

- Default tetap **akun tamu dengan penyimpanan lokal**; login Google opsional dari Profil. Setelah login dan persetujuan izin `drive.appdata`, pustaka tersinkronkan ke Drive pribadi tanpa Supabase.
- Arsip Google Drive pribadi mencakup buku, PDF asli, progres, markah, folder, statistik, dan preferensi. Sinkronisasi otomatis tertunda dan bisa dipicu dari Profil.
- Saat migrasi awal, data lokal lama **tidak dihapus** sampai sinkronisasi, checksum, dan pemulihan Google Drive telah diuji. Penyimpanan lokal masih dipakai sebagai cache kerja untuk memproses dokumen, sehingga rilis ini belum sepenuhnya bebas penyimpanan lokal permanen.
- Pemutar TTS dapat melanjutkan dari teks asli jika filter persiapan teks melempar exception.
- OAuth Google Personal/akun pemilik project diperlukan; login dan Drive API perlu diuji pada perangkat asli dengan Google Play Services.

## Pembaruan 1.3.4

- Perbaikan reliabilitas tombol Play pembaca dan transisi antar-kalimat dengan suara Indonesia online.
- Menangani audio focus, wakelock, dan notifikasi sebagai komponen terpisah dari TTS.
- Jika Google TTS gagal pada kecepatan bacaan pilihan pengguna, coba ulang sekali pada 1× seperti pratinjau, tanpa mengubah suara.
- Pustaka Suara lebih ringkas: penjelasan panjang serta tombol muat ulang di bawah daftar dihapus.
- Tambahan pengujian Android yang membandingkan pratinjau dan layanan pembaca pada mesin Google Indonesia yang tersedia.

## Pembaruan 1.3.3

- Menghilangkan pembatasan suara Google Indonesia hanya offline; seluruh suara Indonesia Google TTS yang tersedia, termasuk melalui jaringan, dapat dipilih.
- Jalur Google Indonesia otomatis tersedia bila mesin melaporkan dukungan bahasa Indonesia; tidak pernah diam-diam beralih ke bahasa lain.
- Pustaka Suara memiliki detail suara bergaya Speechify dengan tombol pratinjau, status online/offline, favorit, pemilihan suara, serta pesan kesalahan TTS.
- Pratinjau memanfaatkan Google TTS yang benar-benar terpasang, bukan demo atau audio palsu. Ketersediaan dan keberhasilan pemutaran jaringan tetap bergantung pada mesin serta koneksi perangkat.

## Pembaruan 1.3.2

- Menyederhanakan sakelar **PDF / Teks** dan menghilangkan latar putih paksa pada tampilan teks PDF.
- Mengatur ulang tipografi, jarak paragraf, dan penanda halaman tanpa menghilangkan teks sumber.
- Sorotan warna pada kalimat dan kata sedang dibacakan; ketuk kata mana saja untuk memulai Google TTS dari kata itu.
- Saat menggulir jauh, ketuk chip kata aktif di bagian atas untuk kembali; pembacaan tidak memaksa scroll ketika menjelajah manual.
- Sejak 1.3.14 halaman PDF menggunakan filter gelap ketika tema pembaca gelap.

## Pembaruan 1.1.1

- Menggunakan **Comic Neue** (SIL OFL 1.1) untuk tampilan yang mendekati tulisan tangan pada referensi tanpa huruf sambung. Berkas font dan lisensinya disertakan secara lokal di `res/font` dan `docs/licenses/ComicNeue-OFL.txt`.
- Navigasi bawah sekarang menghormati area aman tombol navigasi sistem Android.
- APK **release** dioptimalkan untuk perangkat **ARM64** saja dan memakai R8/resource shrinking. Berbeda dari APK debug universal untuk pengujian emulator. Tidak ada perubahan pada kualitas model OCR atau mesin TTS; perangkat dengan arsitektur x86 atau 32-bit tidak didukung oleh APK release personal ini.

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
