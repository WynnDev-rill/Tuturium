#!/usr/bin/env python3
"""Benchmark the same Sherpa inference API used by Tuturium, using public Indonesian fixtures."""
import argparse
import json
import math
from pathlib import Path
from statistics import median
from time import perf_counter

import numpy as np
import sherpa_onnx as sherpa

FIXTURES = [
    "Hari ini saya akan membaca satu halaman buku.",
    "Ketika hujan mulai turun, mereka menutup jendela dan kembali membaca cerita tentang perjalanan ke pulau kecil.",
    "Pada suatu sore, Rani membuka buku bersampul biru, lalu membacakan cerita lama tentang seseorang yang pergi ke perpustakaan bersama teman-temannya.",
]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("model", type=Path)
    parser.add_argument("--threads", type=int, default=3)
    parser.add_argument("--steps", type=int, default=8)
    parser.add_argument("--repeats", type=int, default=3)
    args = parser.parse_args()

    def graph(name):
        quantized = args.model / f"{name}.int8.onnx"
        return str(quantized if quantized.is_file() else args.model / f"{name}.onnx")

    started = perf_counter()
    model = sherpa.OfflineTtsSupertonicModelConfig(
        duration_predictor=graph("duration_predictor"), text_encoder=graph("text_encoder"),
        vector_estimator=graph("vector_estimator"), vocoder=graph("vocoder"),
        tts_json=str(args.model / "tts.json"),
        unicode_indexer=str(args.model / "unicode_indexer.bin"),
        voice_style=str(args.model / "voice.bin"),
    )
    tts = sherpa.OfflineTts(sherpa.OfflineTtsConfig(model=sherpa.OfflineTtsModelConfig(
        supertonic=model, num_threads=args.threads, provider="cpu", debug=False,
    )))
    load_ms = (perf_counter() - started) * 1000

    def generate(text, speaker=0):
        config = sherpa.GenerationConfig()
        config.sid = speaker
        config.speed = 1.0
        config.num_steps = args.steps
        config.extra = {"lang": "id"}
        at = perf_counter()
        result = tts.generate(text, config)
        return (perf_counter() - at) * 1000, np.asarray(result.samples), result.sample_rate

    first_ms, _, _ = generate(FIXTURES[0])
    samples = []
    for repeat in range(args.repeats):
        for index, phrase in enumerate(FIXTURES):
            elapsed, signal, rate = generate(phrase, (0, 5)[repeat % 2])
            assert signal.size > 0 and np.all(np.isfinite(signal))
            voiced = signal[np.abs(signal) >= 0.015]
            rms = math.sqrt(float(np.mean(voiced ** 2))) if voiced.size else 0.0
            gain = min(4.0, max(2.0, 0.21 / max(rms, 1e-6)))
            magnitude = np.abs(signal * gain)
            over = np.maximum(magnitude - 0.65, 0)
            limited = np.sign(signal) * np.where(magnitude <= 0.65, magnitude, 0.65 + 0.31 * over / (0.31 + over))
            change_db = 20 * math.log10(float(np.linalg.norm(limited) / np.linalg.norm(signal)))
            duration = signal.size / rate
            samples.append(dict(fixture=index, elapsed_ms=elapsed, duration_s=duration,
                                rtf=elapsed / (duration * 1000), gain_db=change_db,
                                original_peak=float(np.max(np.abs(signal))), peak=float(np.max(np.abs(limited)))))
    print(json.dumps(dict(
        model=args.model.name, runtime=sherpa.__version__, threads=args.threads, steps=args.steps,
        load_ms=load_ms, cold_first_ms=first_ms,
        median_ms=median(x["elapsed_ms"] for x in samples),
        median_rtf=median(x["rtf"] for x in samples),
        median_gain_db=median(x["gain_db"] for x in samples),
        max_peak=max(x["peak"] for x in samples), samples=samples,
    )), flush=True)


if __name__ == "__main__":
    main()
