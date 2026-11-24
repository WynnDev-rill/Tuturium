#!/usr/bin/env python3
"""Benchmark the existing Supertonic INT8 pack and score Indonesian WAVs with optional host ASR."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import json
from pathlib import Path
import re
from statistics import median
from time import perf_counter
import wave

import numpy as np
import sherpa_onnx as sherpa

FIXTURES = [
    "Dewi memeriksa kembali jadwal kereta menuju Yogyakarta.",
    "Bu Ratna menyeduh teh, lalu menutup jendela sebelum hujan turun.",
    "Perpustakaan menerima dua puluh tujuh buku sejarah dari seorang peneliti.",
    "Nelayan menyusuri sungai yang dangkal sambil mencari jalan kembali ke desa.",
    "Ketika lampu padam, anak-anak mengambil senter. Mereka berjalan pelan, menghindari genangan air di dekat pintu, lalu memanggil penjaga yang sedang memeriksa persediaan di gudang.",
    "Pramugari mengingatkan para penumpang agar menyimpan tas di bawah kursi dan memasang sabuk pengaman. Setelah pesawat tiba di Makassar, kami melanjutkan perjalanan ke rumah nenek.",
]


def engine(model, threads):
    def audio_path(name):
        return str(model / name)
    return sherpa.OfflineTts(sherpa.OfflineTtsConfig(model=sherpa.OfflineTtsModelConfig(
        num_threads=threads, provider="cpu", supertonic=sherpa.OfflineTtsSupertonicModelConfig(
            duration_predictor=audio_path("duration_predictor.int8.onnx"),
            text_encoder=audio_path("text_encoder.int8.onnx"),
            vector_estimator=audio_path("vector_estimator.int8.onnx"),
            vocoder=audio_path("vocoder.int8.onnx"), tts_json=audio_path("tts.json"),
            unicode_indexer=audio_path("unicode_indexer.bin"), voice_style=audio_path("voice.bin"),
        ),
    )))


def chunks(phrase, limit):
    while phrase:
        boundary = len(phrase) if len(phrase) <= limit else phrase.rfind(" ", 0, limit + 1)
        if boundary <= 0:
            boundary = limit
        yield phrase[:boundary].strip()
        phrase = phrase[boundary:].lstrip()


def run_synthesis(args):
    if args.shared:
        models = [engine(args.model, args.threads)] * args.workers
    else:
        models = [engine(args.model, args.threads) for _ in range(args.workers)]

    def generate(model, index, phrase):
        config = sherpa.GenerationConfig()
        config.sid = (0, 5)[index % 2]
        config.speed = args.speed
        config.num_steps = args.steps
        config.extra = {"lang": "id", "seed": "42"}
        started = perf_counter()
        result = [model.generate(part, config) for part in chunks(phrase, args.chunk)]
        return perf_counter() - started, np.concatenate([x.samples for x in result]), result[0].sample_rate

    for model in models:
        generate(model, 0, FIXTURES[0])
    at = perf_counter()
    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        for repeat in range(args.repeats):
            pending = [
                executor.submit(generate, models[index % args.workers], index, phrase)
                for index, phrase in enumerate(FIXTURES)
            ] if args.workers == 1 else [
                executor.submit(lambda model, group: [generate(model, i, FIXTURES[i]) for i in group],
                                model, range(worker, len(FIXTURES), args.workers))
                for worker, model in enumerate(models)
            ]
            if args.workers == 1:
                outcomes = [job.result() for job in pending]
            else:
                groups = [job.result() for job in pending]
                outcomes = [groups[i % args.workers][i // args.workers] for i in range(len(FIXTURES))]
            if repeat == 0:
                all_outcomes = outcomes
            else:
                all_outcomes.extend(outcomes)
    wall = perf_counter() - at
    ratios = []
    samples = []
    for index, (elapsed, pcm, rate) in enumerate(all_outcomes):
        assert len(pcm) and np.all(np.isfinite(pcm))
        duration = len(pcm) / rate
        ratios.append(elapsed / duration)
        samples.append([round(elapsed, 5), round(duration, 5)])
        if args.wav and index < len(FIXTURES):
            args.wav.mkdir(parents=True, exist_ok=True)
            with wave.open(str(args.wav / f"{index}.wav"), "wb") as wav:
                wav.setparams((1, 2, rate, 0, "NONE", ""))
                wav.writeframes((np.clip(pcm, -1, 1) * 32767).astype("<i2").tobytes())
    return dict(threads=args.threads, workers=args.workers, shared=args.shared, steps=args.steps, native=args.speed,
                chunk=args.chunk, rtf=median(ratios),
                throughput=sum(d for _, d in samples) / wall, samples=samples)


def word_errors(reference, hypothesis):
    tokenize = lambda text: re.findall(r"[^\W_]+", text.lower())
    expected, actual = tokenize(reference), tokenize(hypothesis)
    row = list(range(len(actual) + 1))
    for i, word in enumerate(expected, 1):
        next_row = [i]
        for j, token in enumerate(actual, 1):
            next_row.append(min(next_row[-1] + 1, row[j] + 1, row[j - 1] + (token != word)))
        row = next_row
    return row[-1], len(expected)


def score(args):
    recognizer = sherpa.OfflineRecognizer.from_whisper(
        encoder=str(next(args.asr.glob("*encoder*int8.onnx"))),
        decoder=str(next(args.asr.glob("*decoder*int8.onnx"))),
        tokens=str(next(args.asr.glob("*tokens.txt"))), language="id", task="transcribe", num_threads=2,
    )
    rows = []
    for index, reference in enumerate(FIXTURES):
        with wave.open(str(args.wav / f"{index}.wav"), "rb") as wav:
            signal = np.frombuffer(wav.readframes(wav.getnframes()), "<i2").astype(np.float32) / 32768
            rate = wav.getframerate()
        stream = recognizer.create_stream()
        stream.accept_waveform(rate, signal)
        recognizer.decode_stream(stream)
        result = stream.result.text.strip()
        errors, words = word_errors(reference, result)
        rows.append(dict(text=result, errors=errors, words=words))
    return dict(wav=args.wav.name, errors=sum(x["errors"] for x in rows),
                words=sum(x["words"] for x in rows), rows=rows)


def replay(trace_path, passes=120):
    rows = [json.loads(line) for line in trace_path.read_text().splitlines()]
    traces = {(x["threads"], x["steps"]): x["samples"] for x in rows
              if x["workers"] == 1 and x["native"] == 1 and x["chunk"] == 180}
    results = []
    for rate in (2, 3, 4):
        for adaptive in (False, True):
            now = queued = waiting = 0.0
            first = started = None
            buffering = False
            underruns = count = 0
            ratio, measured, steps = 0.0, 8, 8
            for index in range(passes):
                def estimate(target):
                    return ratio * (0.25 + 0.75 * target / 8) / (0.25 + 0.75 * measured / 8)

                if adaptive:
                    runway = queued / rate
                    if started is None or buffering or runway < (16 if steps == 5 else 8):
                        steps = 5
                    elif estimate(6) * rate >= (0.86 if steps == 5 else 1):
                        steps = 5
                    elif runway < (36 if steps < 8 else 20):
                        steps = 6
                    elif estimate(8) * rate >= (0.74 if steps < 8 else 0.88):
                        steps = 6
                    else:
                        steps = 8
                if started is not None and not buffering:
                    if adaptive:
                        demand = ratio * rate
                        capacity = min(750, rate * (300 if demand >= .85 else 180 if demand >= .55 else 90))
                    else:
                        capacity = rate * (300 if ratio * rate >= .75 else 180)
                    idle = max(0, queued - capacity)
                    queued -= idle
                    now += idle / rate
                elapsed, duration = traces[(2 if adaptive else 3, steps)][index % len(FIXTURES)]
                sample = elapsed / duration
                weight = .70 if adaptive else .75
                ratio = estimate(steps) * weight + sample * (1 - weight) if ratio else sample
                measured = steps
                now += elapsed
                if first is None:
                    first = now
                if started is not None:
                    if buffering:
                        waiting += elapsed
                    elif queued < elapsed * rate:
                        waiting += elapsed - queued / rate
                        queued = 0
                        buffering = True
                        count = 0
                        underruns += 1
                    else:
                        queued -= elapsed * rate
                queued += duration
                count += 1
                demand = ratio * rate
                if started is None:
                    initial = (12 if demand >= .95 else 6) if adaptive else 12
                    if index == passes - 1 or queued >= initial * rate or count >= 12 or (count >= 4 and demand < .45):
                        started = now
                elif buffering:
                    refill = (12 if demand >= .9 else 6) if adaptive else 8
                    if index == passes - 1 or queued >= refill * rate or count >= 10:
                        buffering = False
            results.append(dict(rate=rate, adaptive=adaptive, passes=passes, onset_s=round(started, 2),
                                underruns=underruns, stalled_s=round(waiting, 2)))
    return results


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("model", type=Path)
    parser.add_argument("--threads", type=int, default=2)
    parser.add_argument("--steps", type=int, default=8)
    parser.add_argument("--workers", type=int, default=1)
    parser.add_argument("--shared", action="store_true")
    parser.add_argument("--trace", type=Path, help="Replay the captured matrix through the 2–4× buffer policies")
    parser.add_argument("--speed", type=float, default=1)
    parser.add_argument("--chunk", type=int, default=180)
    parser.add_argument("--repeats", type=int, default=1)
    parser.add_argument("--wav", type=Path)
    parser.add_argument("--asr", type=Path)
    args = parser.parse_args()
    result = replay(args.trace) if args.trace else score(args) if args.asr else run_synthesis(args)
    print(json.dumps(result, ensure_ascii=False), flush=True)
