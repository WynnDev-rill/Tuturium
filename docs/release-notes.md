# Tuturium v1.3.4

- Perkuat pemutar TTS dokumen: sesi suara Google Indonesia yang sudah berhasil dipilih dipertahankan antar-kalimat, tanpa memilih ulang setiap kalimat.
- Penanganan audio focus, wake lock, dan notifikasi media yang bermasalah pada beberapa perangkat tidak lagi menghentikan TTS. Kegagalan utama kini menyebut tahap untuk membantu diagnosis.
- Jika pratinjau 1× bisa berjalan tetapi mesin gagal pada kecepatan pembaca, pemutar mencoba ulang **satu kali** pada kecepatan 1× tanpa mengganti bahasa atau suara; kesalahan jaringan tetap ditampilkan.
- Pencatatan statistik opsional yang gagal tidak memutus kelanjutan kalimat berikutnya.
- Hapus teks penjelasan dan tombol "Muat ulang suara" di bawah daftar Pustaka Suara. Tombol "Periksa suara lagi" tetap tersedia bila daftar kosong.
- Tambahkan pengujian kesetaraan pratinjau → pemutaran pembaca untuk perangkat Android yang memiliki suara Google Indonesia.

## Catatan
- Voice Indonesia online tetap memerlukan internet. Tuturium tidak menggunakan suara proprietari dari Speechify/NaturalReader.
- Unit test dan CI emulator tidak dapat membuktikan kompatibilitas semua suara jaringan Google pada model perangkat tertentu. Jika masih gagal di perangkat asli, tahap kesalahan sekarang lebih spesifik dan dapat diperiksa melalui log Android.
- Perbarui dengan keystore yang sama untuk mempertahankan data aplikasi.

