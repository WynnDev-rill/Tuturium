## Tuturium 1.3.20

- Progres di daftar, kotak, dan pembaca mengikuti jumlah kata sampai posisi baca yang tersimpan.
- Sisa durasi memperhitungkan teks yang dilewati, jeda tambahan, dan kecepatan. Tuturium menyesuaikan estimasi dari audio yang selesai diputar oleh Supertonic atau Google TTS.
- Pembaca dan panel kecepatan menampilkan sisa waktu dengan tanda ≈. Perpustakaan membulatkannya ke atas dalam menit.

## Tuturium 1.3.19

- Supertonic mengikuti klausa, merapikan singkatan, dan memangkas hening di tepi audio.
- Jeda narasi menyesuaikan tanda baca dan sambungan PDF. Pengaturan jeda tambahan tersedia di menu Audio.
- Pencarian kata menempel dekat keyboard. Menu folder mendukung pindah folder, dan layar Dihapus mendukung pilih, pulihkan, serta hapus permanen secara massal.
- [Riset, benchmark, dan pengujian](https://github.com/WynnDev-rill/Tuturium/blob/main/docs/narration-flow.md).

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
