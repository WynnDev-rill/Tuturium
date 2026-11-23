# Tuturium

Android native untuk mendengarkan dokumen offline. Package rilis `id.tuturium.app`.
Jaga antarmuka utama dalam bahasa Indonesia.

## Mengerjakan perubahan

1. Baca `docs/research.md` serta README. Untuk UI, baca `.agents/skills/ui-ux-pro-max/SKILL.md` dan `docs/ui-ux-pro-max.md`. Gunakan kembali komponen Compose Material 3, skema `Theme.kt`, ikon bawaan, dan font lokal.
2. Cari aset, library, atau API platform yang bisa digunakan sebelum menambah implementasi. Tinjau izin, kompatibilitas API 26+, lisensi, dan dampak offline.
3. Sebelum mengubah perilaku, tetapkan cara mengujinya. Jalankan `./gradlew testDebugUnitTest lintDebug assembleDebug`. Untuk alur Android, jalankan `./gradlew connectedDebugAndroidTest` pada emulator/perangkat dan cek README untuk pengujian perangkat.
4. Jangan unggah dokumen, kredensial, keystore, kata sandi, atau arsip cadangan. Publikasi hanya memuat APK yang ditandatangani dan checksum. Gunakan signing key asli untuk semua pembaruan.

## Alur kode

- `DocumentImporter` membaca URI Storage Access Framework dan mengekstrak isi. `Library` menyimpan metadata, markah, dan posisi di SQLite, serta menyimpan teks sebagai dokumen internal.
- `ReadingModel` menghasilkan rentang kalimat yang dipakai pemutar dan pembaca. Perubahan pemenggalan kalimat bisa mengubah arti indeks yang sudah disimpan. Pertahankan kompatibilitas posisi pengguna atau migrasikan indeks saat mengganti aturan.
- `ReadingService` adalah pemilik sesi TTS, audio focus, dan notifikasi media. Callback paket suara disaring dengan ID utterance. Pengendali layar membaca StateFlow dari layanan.
- `Updates` menerima rilis stabil dari `WynnDev-rill/Tuturium`, memeriksa ukuran, SHA-256, paket, nomor versi, dan sertifikat. Pertahankan pemeriksaan sebelum membuka pemasang sistem.
- `Tuturium.kt`, `Reader.kt`, `SettingsScreen.kt`, dan `Theme.kt` berisi layar dan tema Compose. Pertahankan semantik pembaca layar, target sentuh, tema gelap, dukungan ukuran font, dan navigasi tanpa bergantung pada gestur eksklusif.

## Rilis

Naikkan `versionCode` dan `versionName` bersama. Tag stabil menggunakan `vMAJOR.MINOR.PATCH`, nama aset `Tuturium.apk`. Workflow rilis membutuhkan secrets `TUTURIUM_KEYSTORE_BASE64` dan `TUTURIUM_STORE_PASSWORD`. Pinned fingerprint publik ada di `docs/signing-certificate.sha256`.
