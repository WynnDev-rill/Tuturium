# Tuturium v1.3.0

- Desain pembaca dan pemutar mengikuti referensi Speechify: tombol navigasi turun, bookmark, menu titik tiga, teks tersorot, posisi progress, estimasi waktu, ikon suara Indonesia, kontrol mundur/maju sekitar 10 detik, tombol putar biru, dan kecepatan.
- Pengaturan kecepatan 0,5× hingga 4,0× tanpa fitur terkunci. Auto speed ramp opsional menaikkan kecepatan +0,1× setiap 500 kata yang selesai dibacakan, maksimal 4×.
- Pustaka suara offline baru menampilkan suara bahasa Indonesia yang benar-benar tersedia di perangkat. Empat profil (Standar, Hangat, Dalam, Ceria) adalah variasi nada dari suara dasar Android, bukan empat model neural berbeda.
- Panel menu pembaca: pencarian dokumen, ubah nama, tampilan, bookmark, catatan pribadi, antrean mendengarkan, filter konten, pemutaran otomatis, auto-scroll, timer tidur 5/10/15/30/45/60 menit, opsi memadukan audio dengan musik, dan hapus file.
- Pengaturan mendengarkan baru mendukung pemutaran otomatis dan kontrol jenis teks yang ingin dilewati dalam audio. Filter berbasis pola teks, tidak memodifikasi dokumen dan menggunakan pemetaan offset agar sorotan tetap sinkron.
- Android menangani refresh rate sesuai perangkat dan sistem (tidak ada batas FPS manual pada aplikasi); hanya UI animasi yang memerlukan frame, tidak ada render loop konstan.
- Tidak menggunakan API suara Speechify yang bersifat proprietari atau tidak didokumentasikan. Tidak ada klaim akses gratis seumur hidup ke suara neural online pihak ketiga.
- Tetap kompatibel dengan penyimpanan, antrean, dokumen, dan sertifikat penandatanganan lama. Android release ARM64 dengan R8 tetap digunakan.

## Catatan penting
Pemilihan suara lokal bergantung pada suara yang tersedia di mesin TTS Android. Audio “±10 detik” berbasis perkiraan 150 kata/menit yang disesuaikan kecepatan, bukan seeking PCM sample-akurat. Waktu pemutaran pada bar merupakan perkiraan berbasis jumlah kata. Fitur filter konten sengaja tidak aktif secara bawaan agar tidak menghilangkan teks audiobook tanpa persetujuan pengguna.
