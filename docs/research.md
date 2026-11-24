# Riset Tuturium

## Pembanding Android

| Produk | Keunggulan yang diuji terhadap kebutuhan Tuturium | Batasan relevan | Keputusan Tuturium |
|---|---|---|---|
| NaturalReader Personal | Impor berkas, suara pilihan, tampilan baca, dan pemrosesan PDF. Panduan mobile menyatakan suara Free yang tersedia bisa dipakai tanpa batas dan bergantung pada perangkat atau OS. | Kuota suara AI berbeda mengikuti kelas suara. Panduan paket menempatkan OCR dan ekspor MP3 di luar paket gratis. | Gunakan suara TTS yang tersedia offline di Android, tanpa batas pemakaian dari Tuturium. Sertakan OCR Latin lokal. [Suara mobile](https://help.naturalreaders.com/en/articles/7198951-mobile-app-voices-languages) · [Paket](https://help.naturalreaders.com/en/articles/8854700-plans-pricing-personal-version) |
| @Voice Aloud Reader | Alur dokumen Android matang, berbagi teks, EPUB dengan navigasi bab, markah, progres, dan penyelarasan kata pada mesin yang mendukungnya. | PDF dengan kolom atau urutan bacaan rumit dapat bergantung pada hasil ekstraksi. Penyorotan kata bergantung pada dukungan mesin. | Sorot kalimat sebagai dasar, naikkan ke sorotan kata ketika Android memberikan rentang. Simpan posisi dan markah, tampilkan judul bagian. [Panduan](https://hyperionics.com/atVoice/AppFeatures.html) · [PDF](https://hyperionics.com/atVoice/features/pdf-reader-aloud.asp) |
| Librera Reader | Pustaka lokal, banyak format, konfigurasi pembaca, dan integrasi TTS sistem. Kode sumber dapat dikaji. | Perlu suara pada mesin yang terpasang. Cakupan format dan pilihan pengaturan menambah kepadatan UI. | Prioritaskan Indonesia, sandingkan impor dan perpustakaan dalam satu tab, dan sediakan setelan mesin Android. [Fitur](https://librera.app/librera-reader) · [Konfigurasi TTS](https://librera.app/librera-reader/faq/installation-and-configuration-of-tts) |
| Speechify | Alur mendengarkan, PDF, pengaturan kecepatan, dan pilihan suara neural. | Situsnya menempatkan unduhan audio offline pada Premium. | Pertahankan pengalaman mendengar sebagai fungsi utama, menggunakan mesin offline tanpa akun. [Produk](https://speechify.com/) · [Android](https://play.google.com/store/apps/details?id=com.cliffweitzman.speechify2) |

Perbandingan didasarkan pada dokumentasi publik. Target kualitas yang diterapkan meliputi impor dari pemilih berkas Android, pemutaran yang tetap berjalan saat layar terkunci, penyorotan sesuai posisi baca, serta cara kembali ke bagian terakhir atau markah tanpa mengimpor ulang.

## Aset dan komponen

- [UI UX Pro Max](https://github.com/nextlevelbuilder/ui-ux-pro-max-skill) dipasang di `.agents/skills/ui-ux-pro-max` dari commit yang tercatat di `UPSTREAM`. Hasil generator di `docs/ui-ux-pro-max.md` memberi hierarki editorial, Cormorant Garamond, Crimson Pro, dan motion rendah. `Theme.kt` memakai font tersebut dan warna biru-ungu berdasarkan identitas Tuturium. Komponen web yang disarankan generator disesuaikan ke Compose Material 3.
- [PDFBox Android](https://github.com/TomRoush/PdfBox-Android) menyediakan ekstraksi teks PDF berbasis Apache PDFBox. [PdfRenderer](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer) dan [ML Kit Text Recognition Latin](https://developers.google.com/ml-kit/vision/text-recognition/v2/android) menangani halaman pindai dengan model yang dibundel.
- [Readium Kotlin](https://github.com/readium/kotlin-toolkit) menyediakan navigator, DRM, dan kemampuan publikasi EPUB yang luas. Tuturium memerlukan teks untuk TTS dan posisi yang konsisten di empat format. Basis EPUB yang dipilih adalah ZIP/spine standar dengan [jsoup](https://jsoup.org/) untuk parsing HTML. DOCX dibaca dengan parser XML streaming Android.
- [Android TextToSpeech](https://developer.android.com/reference/android/speech/tts/TextToSpeech), audio focus, dan MediaSession menyediakan integrasi mesin lokal, headset, dan notifikasi. Pemutar audio berbasis file akan membutuhkan sintesis awal dan cache per berkas. Penggunaan TTS langsung mempertahankan umpan balik rentang tanpa menunggu konversi.
- [Levyra](https://github.com/LUC4N3X/Levyra-deepsound) menjadi referensi pemeriksaan `/releases/latest`, tampilan versi terpasang/tersedia, dan verifikasi tanda tangan APK. Implementasi Tuturium ditulis sendiri. Pemeriksaan SHA-256 memakai metadata aset [GitHub Releases](https://docs.github.com/en/rest/releases/assets).


## Suara lokal 1.3.8
- Supertonic 3: https://k2-fsa.github.io/sherpa/onnx/tts/supertonic.html — 10 speaker, Indonesian lang=id, INT8. Sherpa AAR pinned v1.13.4.
- Piper News TTS Indonesia: https://k2-fsa.github.io/sherpa/onnx/tts/all/Indonesian/vits-piper-id_ID-news_tts-medium.html — one speaker, original medium model.
- ArboTTS: https://github.com/ARBO-TEAM/arboTTS — official release assets Nano Ardi, Small Arbo/Arba, Standard Arbo; Kokoro packs id_arbo/id_arba. G2P adapted from MIT upstream. Studio voice cloning, derived personas using other engines, and MCU binaries are not represented as separate ready-to-use voices. Runtime model smoke tests still necessary on Android.

## Pembaca dan koneksi Drive 1.3.11

- Supertonic memakai sintesis 1× dan delapan langkah seperti contoh resmi. Cadangan mengikuti kecepatan pemutaran Media3, sehingga perubahan kecepatan tidak memerlukan sintesis ulang. Media3 menjaga CPU aktif selama pemutaran latar. [Supertonic](https://github.com/supertone-inc/supertonic/blob/main/py/example_onnx.py) · [Media3](https://developer.android.com/media/media3/exoplayer)
- Mode PDF mengikuti pola halaman asli dan mode teks dari referensi NaturalReader. Tuturium merender halaman dengan Android PdfRenderer dan memakai teks per halaman untuk narasi serta tampilan Teks. [Tampilan NaturalReader](https://help.naturalreaders.com/en/articles/11585617-display-and-reading-appearance-personal-version) · [PdfRenderer](https://developer.android.com/reference/android/graphics/pdf/PdfRenderer)
- Google Identity Services meminta persetujuan saat izin belum tersedia. Sesudah diberikan, aplikasi memperoleh token secara diam-diam dan dapat menghapus token yang kedaluwarsa sebelum mencoba lagi. [Otorisasi Android](https://developer.android.com/identity/authorization)

## PDF vertikal dan kecepatan otomatis 1.3.13

NaturalReader mendokumentasikan tampilan teks dan perilaku autoscroll yang mengikuti narasi, dengan tindakan kembali ke posisi baca setelah pengguna menggulir. Tuturium menerapkannya pada daftar halaman PDF asli yang vertikal, sesuai referensi Android, serta memuat bitmap halaman secara bertahap. [Tampilan](https://help.naturalreaders.com/en/articles/11585617-display-and-reading-appearance-personal-version) · [Navigasi](https://help.naturalreaders.com/en/articles/11519494-reading-flow-filters-and-navigation-personal-version)

Speechify menyediakan penggeser vertikal di Android dan menjelaskan bahwa kenaikan otomatis mengikuti aktivitas mendengarkan. Catatan rilis web mencatat 10 kata/menit per 1.050 kata. Referensi Android yang diberikan menampilkan interval 600 kata. Tuturium mengikuti interval Android dengan langkah Tuturium sebesar 0,05×, mulai dari pengaturan pengguna, hingga 4×. Hitungan menggunakan kata dari potongan yang selesai diucapkan. Jeda dan lompat dalam dokumen yang sama mempertahankan hitungan. Mengubah laju awal, mengubah sakelar, atau membuka dokumen berbeda memulai hitungan baru. [Penggeser Android Speechify](https://speechify.com/blog/change-listening-speed-on-android/) · [Catatan rilis web Speechify](https://speechify.com/news/speechify-web-app-and-chrome-extension-fall/)
