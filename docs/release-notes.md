# Tuturium v1.3.3

- Pustaka Suara kini menerima suara Google TTS berbahasa Indonesia baik yang membutuhkan internet maupun yang tersedia offline. Pemilihan suara tetap menyaring bahasa Indonesia saja, tanpa fallback ke bahasa lain.
- Bila Google TTS melaporkan dukungan bahasa Indonesia namun tidak memiliki nama suara khusus, tersedia pilihan "Google Indonesia otomatis" yang memverifikasi bahasa aktif sebelum berbicara.
- Setiap suara dapat dibuka ke halaman detail bergaya Speechify dengan avatar, status online/offline, tombol favorit, pratinjau suara langsung, dan tombol Pilih Suara.
- Tombol pratinjau menggunakan Google Speech Services sungguhan; menampilkan status saat menyiapkan suara dan alasan jika pemutaran gagal. Suara online membutuhkan internet.
- Pustaka Suara menampilkan semua suara Indonesia yang dilaporkan mesin, tidak lagi menganggap suara online sebagai tidak terpasang.
- Tampilan pembaca gelap dan interaksi per kata dari v1.3.2 dipertahankan.

## Catatan
- Suara Speechify dan NaturalReader bukan bagian dari Tuturium. Jumlah suara bergantung pada Google TTS dan perangkat.
- Pratinjau hanya membuktikan pemutaran pada perangkat saat tombol ditekan; build dan emulator CI tidak dapat menjamin ketersediaan suara Google Indonesia pada POCO F6.
- Pembaruan instalasi wajib memakai keystore Tuturium yang sama seperti versi sebelumnya.
