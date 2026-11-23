# Tuturium v1.3.1

- Perbaikan penting pada layanan pemutar: pengecualian Google TTS dan audio focus tidak lagi menutup aplikasi; pesan error lebih jelas.
- Google Speech Services (com.google.android.tts) menjadi sumber suara; hanya suara bahasa Indonesia **offline** yang benar-benar terpasang dan terdeteksi yang ditampilkan, tanpa contoh palsu atau model suara bahasa lain. Jika belum tersedia, Pustaka Suara menyediakan akses langsung ke pemasangan data suara Google.
- Empat suara lokal dikenal dengan kode id-id-x-dfz-local, id-id-x-idc-local, id-id-x-idd-local, id-id-x-ide-local, namun hanya ditampilkan jika tersedia pada perangkat.
- PDF yang diimpor mulai v1.3.1 menyimpan file sumber: mode PDF asli menjaga layout halaman, dan mode Teks & sorotan menjaga fitur pembacaan kata demi kata.
- PDF yang diimpor pada versi sebelumnya tetap tersedia dalam mode teks; impor ulang diperlukan untuk menampilkan halaman aslinya.
- Mode teks PDF memakai tipografi serif dengan pemisah halaman yang lebih halus; file PDF rusak menampilkan pesan error alih-alih loading tanpa akhir.
- Perbaikan crash penting pada service audio, error suara Google, error layanan foreground, dan pengaturan fallback suara; regresi diuji dengan unit test dan emulator.
- Tetap ARM64, compatible signing, dan menyimpan progres serta seluruh koleksi lama tanpa reset.

## Catatan
Tuturium tidak menyertakan suara proprietari Speechify/NaturalReader. Google TTS offline tidak menggunakan API berbayar, tetapi ketersediaan paket suaranya mengikuti Android dan versi Google Speech Services. Tidak ada jaminan bahwa semua perangkat menyediakan jumlah suara yang sama.
