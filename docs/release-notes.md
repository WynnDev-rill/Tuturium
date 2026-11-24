## Tuturium 1.3.17

- Supertonic menyesuaikan langkah inferensi dengan laju dengar, cadangan audio, dan suhu sambil memakai model Indonesia yang sama.
- Cadangan awal dan pemulihan antrean mengikuti throughput sintesis. Antrean memiliki batas waktu dan ruang agar disk tetap terkendali.
- Pada Android 10 ke atas, cache ulang dapat menyimpan Opus saat antrean aman. PCM tetap dipakai untuk sintesis langsung dan sebagai cadangan cache bila codec tidak tersedia.
- [Hasil benchmark dan metode pengujian](https://github.com/WynnDev-rill/Tuturium/blob/main/docs/supertonic-adaptive.md).
