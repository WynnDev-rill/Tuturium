## Tuturium 1.3.25

- Pulihkan hanya cadangan Drive yang lolos pemeriksaan isi. Pulihkan PDF asli yang hilang sambil mempertahankan progres dan markah lokal yang lebih baru.
- Tampilkan kembali buku di tingkat utama bila folder rujukannya tidak tersedia. Perbarui pustaka dan setelan sesudah menerima data Drive. Pembaca tetap bisa ditutup setelah dokumennya dihapus melalui sinkronisasi.
- Ikut bersihkan catatan lokal dan salinan berbagi sementara ketika buku dihapus permanen.
- Siapkan susunan teks dan indeks PDF di luar thread antarmuka. Pertahankan isi pembaca sampai transisi kembali selesai.
- Pemeriksaan pembaruan otomatis bisa dicoba lagi ketika aplikasi kembali ke depan.
## Tuturium 1.3.24

- Normalisasi angka Indonesia dan singkatan otomatis pada Google TTS dan Supertonic, dengan posisi sorotan mengikuti teks asli.
- Filter PDF membaca koordinat halaman untuk mengenali header/footer berulang, nomor halaman, catatan kaki, dan tabel yang memiliki pola tata letak yang jelas.
- Area bacaan PDF dapat diatur per halaman atau untuk seluruh buku, tanpa mengubah PDF asli, markah, atau posisi yang tersimpan.
- [Riset dan cakupan pemrosesan teks](https://github.com/WynnDev-rill/Tuturium/blob/main/docs/narration-text-processing.md).

## Tuturium 1.3.23

- Akselerasi Supertonic 3 tersedia di Pustaka Suara. Unduhan satu kali berukuran 66 MB, lalu pemrosesan berlangsung di perangkat.
- Graf INT8 mempercepat estimator. Aplikasi mempertahankan 6–8 langkah jika cadangan cukup dan menyesuaikannya saat perangkat panas atau produksi audio tertinggal.
- Sepuluh pilihan suara, posisi baca, dan pemutaran latar tetap memakai jalur Tuturium yang sama.
- [Perbandingan runtime dan benchmark](https://github.com/WynnDev-rill/Tuturium/blob/main/docs/supertonic-acceleration.md).

## Tuturium 1.3.22

- Estimasi waktu di pembaca dan panel kecepatan tetap stabil saat narasi berjalan dan mesin suara mengumpulkan pengukuran baru.
- Percobaan ulang Google TTS pada 1× tidak mengubah total durasi setiap kali mesin beralih kembali ke kecepatan pilihan.

## Tuturium 1.3.21

- Pemutar kembali menampilkan estimasi waktu berjalan / total (≈ 03:58 / 16:39:35), dengan kalibrasi durasi dan filter teks tetap aktif.
- Hapus bagian impor penyimpanan perangkat / cloud yang redundan; tombol File tetap membuka pemilih dokumen Android.

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
