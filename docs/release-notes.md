# Tuturium v1.3.5 — Google Login dan Drive pribadi

- Login akun Google menggunakan Android Credential Manager dan OAuth Client ID Tuturium. Akses Google Drive memerlukan persetujuan terpisah hanya untuk scope `drive.appdata`.
- Menyimpan snapshot lengkap pustaka melalui Google Drive appDataFolder tersembunyi: teks buku, PDF asli jika tersedia, progres, markah, folder, antrian, statistik, serta pengaturan.
- Memulihkan data akun yang sama saat login pada perangkat baru, menghindari penggantian data lokal yang lebih baru, dan memverifikasi MD5 unggahan maupun unduhan arsip.
- Mengirim perubahan secara berkala dan saat aplikasi berpindah latar, menampilkan status sinkronisasi pada Profil/Penyimpanan.
- Mencegah berkas lokal dari satu akun terunggah ke akun Google lain tanpa migrasi yang disengaja.
- Memperbaiki kegagalan TTS pada tahap 'menyiapkan teks': filter teks opsional yang gagal kini kembali ke teks asli dengan offset asli, bukan menghentikan Play.
- Menambah tes migrasi arsip dan pengamanan akun serta tes regresi teks TTS.

## Batasan dan keselamatan data

- Rilis transisi ini **masih menyimpan database dan berkas buku lokal** untuk keamanan migrasi dan sebagai cache kerja. Jangan hapus data aplikasi sebelum pemulihan Google Drive diuji di perangkat lain. Penyimpanan lokal permanen belum dihapus.
- OAuth Google Personal hanya mengizinkan pemilik proyek Google Cloud. Akun lain memerlukan perubahan pengaturan proyek.
- Identitas dan persetujuan Drive harus diuji pada perangkat Android dengan Play Services. Emulator CI tanpa akun Google hanya menguji kode dan antarmuka; tes login langsung tidak dapat dilakukan di sana.
- Arsip aplikasi disimpan di appDataFolder Google Drive, bukan di folder Drive biasa yang dapat terlihat.
- Bila akses jaringan atau token kedaluwarsa, unggahan ditunda dan data kerja lokal tidak dihapus. Masuk ulang untuk memperbarui izin jika diperlukan.
- Gunakan sertifikat rilis Tuturium lama saat memasang pembaruan, agar buku dan pengaturan instalasi sebelumnya tetap tersedia.
