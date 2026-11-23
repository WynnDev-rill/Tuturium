# Tuturium v1.3.2

- Mode pembaca PDF/Teks kini menggunakan sakelar ringkas yang mengikuti UI Tuturium. Mode Teks adalah tampilan awal yang nyaman untuk pembacaan dengan suara, sementara mode PDF menampilkan halaman asli dokumen jika tersedia.
- Tampilan teks PDF tidak lagi memaksakan latar putih atau tulisan hitam pada tema gelap. Ukuran huruf lebih proporsional, jarak antarparagraf diperbaiki, label halaman lebih tenang, dan warna mengikuti tema aplikasi.
- Saat pengguna menggulir menjauh dari bagian yang sedang dibaca, tombol kecil berisi kata suara aktif muncul untuk kembali ke posisi pembacaan. Auto-scroll berhenti sementara saat dokumen digulir manual.
- Sorotan kalimat dan kata aktif mengikuti warna utama Tuturium. Ketukan pada kata kini mulai membaca tepat dari awal kata tersebut, bukan awal paragraf atau kalimat.
- Perbaikan offset pembacaan Google TTS: memilih kata pada kalimat yang sama, memilih kata setelah tanda baca, dan berpindah dokumen menggunakan posisi yang benar.
- Menambah lima pengujian unit penentuan kata; pengujian Android lainnya tetap dijalankan melalui GitHub Actions.

## Catatan
- TTS tetap Google Speech Services dengan suara Indonesia lokal; tidak menggunakan suara proprietari Speechify atau NaturalReader.
- Mode PDF asli mempertahankan halaman dokumen sebagai gambar, sedangkan sorotan dan pemilihan kata berlaku di mode Teks. Sorotan per kata memerlukan callback rentang ucapan dari Google TTS; jika tidak didukung, sorotan kalimat tetap tersedia.
- Dokumen yang diimpor sebelum penyimpanan PDF asli diperkenalkan mungkin memerlukan impor ulang untuk membuka mode PDF. Progres dan markah lama tidak dihapus.
