# Keep PDFBox and its font parsers intact. Documents may contain font/crypto
# formats that are exercised only by user-imported PDFs, not by unit tests.
-keep class com.tom_roush.pdfbox.** { *; }
-keep class com.tom_roush.fontbox.** { *; }

# ML Kit's text recognition is used for scanned pages and camera OCR.
# Preserve its runtime-selected classes. The Latin OCR model is unchanged.
-keep class com.google.mlkit.** { *; }

# PDF encryption support can load this provider by class name.
-keep class org.bouncycastle.jce.provider.BouncyCastleProvider { *; }

# PDFBox has an optional Gemalto JPEG2000 encoder/decoder that is not bundled
# by pdfbox-android and was unavailable in Tuturium 1.1.0 as well.
-dontwarn com.gemalto.jp2.**

# Native ONNX runtimes use JNI names and reflection.
-keep class com.k2fsa.sherpa.onnx.** { *; }
-dontwarn org.apache.commons.compress.compressors.zstandard.**
-dontwarn com.github.luben.zstd.**
-dontwarn org.tukaani.xz.**
-dontwarn org.brotli.dec.**
