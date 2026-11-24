## Tuturium 1.3.18

- Gunakan audio focus MAY_DUCK untuk opsi musik latar; pemulihan volume ditangani Android saat focus dilepas.
- Sorotan PDF lebih kontras tanpa menutup tinta, tombol kembali ke bacaan mengambang pada PDF dan teks.
- Perpustakaan utama hanya menampilkan dokumen tanpa folder; pemindahan mendukung tujuan Perpustakaan, pencarian dan pembuatan folder, serta konfirmasi.
- Unduh dokumen asli PDF atau salinan teks melalui pemilih penyimpanan Android.
- Laman Tentang Tuturium terpisah.

## Tuturium 1.3.17

- Supertonic menyesuaikan langkah inferensi dengan laju dengar, cadangan audio, dan suhu sambil memakai model Indonesia yang sama.
- Cadangan awal dan pemulihan antrean mengikuti throughput sintesis. Antrean memiliki batas waktu dan ruang agar disk tetap terkendali.
- Pada Android 10 ke atas, cache ulang dapat menyimpan Opus saat antrean aman. PCM tetap dipakai untuk sintesis langsung dan sebagai cadangan cache bila codec tidak tersedia.
- [Hasil benchmark dan metode pengujian](https://github.com/WynnDev-rill/Tuturium/blob/main/docs/supertonic-adaptive.md).
