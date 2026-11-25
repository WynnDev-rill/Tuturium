#!/usr/bin/env python3
"""Compare one denoising step at the LiteRT fixed T128/L64 shape on a host CPU."""
import argparse
import json
from pathlib import Path
from statistics import median
from time import perf_counter

from ai_edge_litert.interpreter import Interpreter
import numpy as np
import onnxruntime as ort


def latency(run, repeats):
    for _ in range(8):
        run()
    times = []
    for _ in range(repeats):
        start = perf_counter()
        run()
        times.append((perf_counter() - start) * 1000)
    return {"median_ms": round(median(times), 3), "p95_ms": round(float(np.percentile(times, 95)), 3)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("baseline", type=Path)
    parser.add_argument("wilbur", type=Path)
    parser.add_argument("litert", type=Path)
    parser.add_argument("--threads", type=int, default=2)
    parser.add_argument("--repeats", type=int, default=80)
    args = parser.parse_args()

    rng = np.random.default_rng(42)
    inputs = {
        "noisy_latent": rng.standard_normal((1, 144, 64), dtype=np.float32),
        "text_emb": rng.standard_normal((1, 256, 128), dtype=np.float32),
        "style_ttl": rng.standard_normal((1, 50, 256), dtype=np.float32),
        "latent_mask": np.ones((1, 1, 64), dtype=np.float32),
        "text_mask": np.ones((1, 1, 128), dtype=np.float32),
        "current_step": np.array([3], dtype=np.float32),
        "total_step": np.array([8], dtype=np.float32),
    }
    options = ort.SessionOptions()
    options.intra_op_num_threads = args.threads
    options.inter_op_num_threads = 1
    for name, model in (("baseline_ort_cpu", args.baseline), ("wilbur_ort_cpu", args.wilbur)):
        session = ort.InferenceSession(str(model), sess_options=options, providers=["CPUExecutionProvider"])
        assert all(np.isfinite(value).all() for value in session.run(None, inputs))
        print(json.dumps({"backend": name, **latency(lambda: session.run(None, inputs), args.repeats)}))
        del session

    interpreter = Interpreter(model_path=str(args.litert), num_threads=args.threads)
    interpreter.allocate_tensors()
    input_keys = tuple(inputs)
    for tensor in interpreter.get_input_details():
        index = int(tensor["name"].removeprefix("serving_default_args_").removesuffix(":0"))
        interpreter.set_tensor(tensor["index"], inputs[input_keys[index]])
    interpreter.invoke()
    assert all(np.isfinite(interpreter.get_tensor(tensor["index"])).all()
               for tensor in interpreter.get_output_details())
    print(json.dumps({"backend": "litert_default_cpu", **latency(interpreter.invoke, args.repeats)}))


if __name__ == "__main__":
    main()
