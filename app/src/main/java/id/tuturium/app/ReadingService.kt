package id.tuturium.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Playback(
    val bookId: String? = null,
    val title: String = "",
    val index: Int = 0,
    val total: Int = 0,
    val playing: Boolean = false,
    val loading: Boolean = false,
    val completed: Boolean = false,
    val range: IntRange? = null,
    val timerUntil: Long = 0,
    val effectiveSpeed: Float = 1f,
    val error: String? = null
)

data class ReadingSettings(val voice: String?, val speed: Float, val pitch: Float, val volume: Float = 1f) {
    companion object {
        fun read(context: Context): ReadingSettings = context.getSharedPreferences("settings", 0).let {
            ReadingSettings(OfflineCatalog.supportedSelection(it.getString("voice", null)),
                it.getFloat("speed", 1f), it.getFloat("pitch", 1f), it.getFloat("tts_volume", 1f).coerceIn(0f, 1f))
        }
    }
    fun save(context: Context) {
        val previous = read(context)
        context.getSharedPreferences("settings", 0).edit()
            .putString("voice", voice).putFloat("speed", speed).putFloat("pitch", pitch)
            .putFloat("tts_volume", volume.coerceIn(0f, 1f)).apply()
        if (ReadingService.state.value.bookId != null && this != previous) {
            val action = if (previous.copy(volume = volume) == this) ReadingService.VOLUME else ReadingService.SETTINGS
            ReadingService.command(context, action)
        }
    }
}

fun TextToSpeech.availableVoices(): List<Voice> =
    runCatching { voices.orEmpty() }.getOrDefault(emptySet())
        .filter { voice -> voice.isNetworkConnectionRequired ||
            TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty() }
        .sortedWith(compareBy<Voice> { it.isNetworkConnectionRequired }.thenByDescending { it.quality })

fun TextToSpeech.offlineVoices(): List<Voice> = availableVoices()
    .filter { !it.isNetworkConnectionRequired }

fun Voice.choice() = VoiceChoice(name, locale.toLanguageTag().substringBefore('-'),
    locale.getDisplayName(Locale.forLanguageTag("id")), quality, isNetworkConnectionRequired)

/** Enumerate both local and network voices; offer Android's Indonesian-language
 *  route when the Google engine exposes language support but no specific voice. */
fun TextToSpeech.indonesianCandidates(): List<VoiceChoice> {
    val available = googleIndonesianVoices(availableVoices().map { it.choice() })
    val locale = Locale.forLanguageTag("id-ID")
    val supportsIndonesian = runCatching {
        isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE
    }.getOrDefault(false)
    return if (supportsIndonesian) available + VoiceChoice(
        GOOGLE_ID_AUTO, "id", "Indonesia", 0, network = true
    ) else available
}

/** Never silently synthesize with an English voice or a different TTS engine. */
fun TextToSpeech.activateIndonesianVoice(choice: VoiceChoice): Boolean {
    if (!isIndonesian(choice.language)) return false
    if (choice.name == GOOGLE_ID_AUTO) {
        val locale = Locale.forLanguageTag("id-ID")
        val result = runCatching { setLanguage(locale) }.getOrDefault(TextToSpeech.ERROR)
        return result >= TextToSpeech.LANG_AVAILABLE &&
            runCatching { isIndonesian(language?.language.orEmpty()) }.getOrDefault(false)
    }
    val voice = availableVoices().firstOrNull {
        it.name == choice.name && isIndonesian(it.locale.language)
    } ?: return false
    return runCatching { setVoice(voice) == TextToSpeech.SUCCESS }.getOrDefault(false)
}

class ReadingService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var session: MediaSession
    private lateinit var audio: AudioManager
    private lateinit var focus: AudioFocusRequest
    private lateinit var exclusiveFocus: AudioFocusRequest
    private lateinit var duckingFocus: AudioFocusRequest
    private lateinit var lock: PowerManager.WakeLock
    private var tts: TextToSpeech? = null
    private var narrationEnhancer: android.media.audiofx.LoudnessEnhancer? = null
    private val narrationSessionId by lazy {
        val id = audio.generateAudioSessionId()
        if (id <= 0) null else runCatching {
            val effect = android.media.audiofx.LoudnessEnhancer(id)
            try {
                effect.setTargetGain(narrationBoostMb(ReadingSettings.read(this).volume))
                check(effect.setEnabled(true) == android.media.audiofx.AudioEffect.SUCCESS)
                narrationEnhancer = effect
                id
            } catch (error: Exception) {
                effect.release()
                throw error
            }
        }.getOrNull()
    }
    private val offlineAudio by lazy { OfflineAudio(applicationContext) }
    private var ready = false
    private var initializationFailed = false
    // A stable voice configuration is reused across consecutive sentences.
    // Re-enumerating and re-selecting network voices on each onDone can disrupt
    // a working Google TTS session mid-book.
    private var cachedVoices: List<VoiceChoice> = emptyList()
    private var appliedVoiceName: String? = null
    private var startupStage = "persiapan"
    private var normalSpeedRetryPass = -1
    private var document: ReadingDocument? = null
    private var request = 0
    private var utterance = 0
    private var activeUtterance: String? = null
    private var offlineSession: OfflineSession? = null

    private data class OfflineSession(var settings: ReadingSettings, var options: ListeningOptions)
    private var speechOffset = 0
    private var lastCheckpoint = 0L
    private var resumeOnFocus = false
    private var hasFocus = false
    private var foreground = false
    private var activePrepared = PreparedSpeech("", IntArray(0))
    private var preparedStart = 0
    private var preparedParagraph: PreparedSpeech? = null
    private var preparedLocation: Pair<Int, Int>? = null
    private var preparedOptions: ListeningOptions? = null
    private var continuation: Job? = null
    private val automaticSpeed = AutomaticSpeed()
    private var listeningStart = 0L
    private var listeningSpeed = 1f
    private var current = Playback()
        set(value) {
            if (field.playing && !value.playing && listeningStart > 0L) {
                recordListeningTime()
                listeningStart = 0L
            } else if (!field.playing && value.playing) {
                listeningStart = SystemClock.elapsedRealtime()
                listeningSpeed = value.effectiveSpeed
            }
            if (value.playing) listeningSpeed = value.effectiveSpeed
            field = value
            mutable.value = value
        }
    private val leaveForeground = Runnable {
        if (foreground && !current.playing && !current.loading) {
            stopForeground(STOP_FOREGROUND_DETACH)
            foreground = false
        }
    }
    private val timer = Runnable {
        current = current.copy(timerUntil = 0)
        pause()
    }
    private val initTimeout = Runnable {
        if (!ready) {
            initializationFailed = true
            if (current.loading && activeUtterance == null &&
                OfflineCatalog.voice(ReadingSettings.read(this).voice) == null)
                fail("Mesin TTS Android tidak merespons.")
        }
    }
    private val noisy = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) = pause()
    }

    override fun onCreate() {
        super.onCreate()
        audio = getSystemService(AudioManager::class.java)
        lock = getSystemService(PowerManager::class.java)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "$packageName:reading")
            .apply { setReferenceCounted(false) }
        val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
        fun requestFor(gain: Int) = AudioFocusRequest.Builder(gain)
            .setAudioAttributes(attributes).setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        if (current.playing || current.loading) {
                            resumeOnFocus = true
                            pause(keepFocus = true)
                        }
                    }
                    AudioManager.AUDIOFOCUS_LOSS -> pause()
                    AudioManager.AUDIOFOCUS_GAIN -> if (resumeOnFocus) {
                        resumeOnFocus = false
                        startCurrent()
                    }
                }
            }, handler).build()
        exclusiveFocus = requestFor(AudioManager.AUDIOFOCUS_GAIN)
        duckingFocus = requestFor(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        focus = exclusiveFocus
        ContextCompat.registerReceiver(this, noisy, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED)
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Pemutar Tuturium", NotificationManager.IMPORTANCE_LOW)
        )
        session = MediaSession(this, "Tuturium").apply {
            setSessionActivity(openApp())
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() = startCurrent()
                override fun onPause() = pause()
                override fun onStop() = finish()
                override fun onSkipToPrevious() = seek(current.index - 1)
                override fun onSkipToNext() = seek(current.index + 1)
            }, handler)
            isActive = true
        }
        tts = TextToSpeech(this, { result ->
            handler.post {
                handler.removeCallbacks(initTimeout)
                ready = result == TextToSpeech.SUCCESS &&
                    runCatching { tts?.engines.orEmpty().any { it.name == GOOGLE_TTS_ENGINE } }
                        .getOrDefault(false)
                initializationFailed = !ready
                if (ready) {
                    runCatching { tts?.setAudioAttributes(attributes) }
                        .onFailure { android.util.Log.w("TuturiumPlayback", "TTS audio attributes unavailable", it) }
                    tts?.setOnUtteranceProgressListener(listener)
                    if (current.loading && document != null &&
                        OfflineCatalog.voice(ReadingSettings.read(this).voice) == null) startCurrent()
                } else if (current.loading && OfflineCatalog.voice(ReadingSettings.read(this).voice) == null) {
                    fail("Google TTS belum tersedia. Buka Pustaka Suara untuk memasangnya.")
                }
            }
        }, GOOGLE_TTS_ENGINE)
        handler.postDelayed(initTimeout, 15_000)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PLAY -> {
                val id = intent.getStringExtra("book")
                if (id != null) open(id, intent.getIntExtra("index", -1), intent.getIntExtra("offset", -1))
                else if (document != null) startCurrent()
                else {
                    promote()
                    finish()
                }
            }
            PAUSE -> pause()
            STOP -> finish()
            NEXT -> seek(current.index + 1)
            PREVIOUS -> seek(current.index - 1)
            SEEK -> seek(intent.getIntExtra("index", 0), offset = intent.getIntExtra("offset", 0).coerceAtLeast(0))
            VOLUME -> {
                val volume = ReadingSettings.read(this).volume
                runCatching { narrationEnhancer?.setTargetGain(narrationBoostMb(volume)) }
                offlineSession?.let {
                    it.settings = it.settings.copy(volume = volume)
                    offlineAudio.setVolume(volume)
                }
            }
            SETTINGS, RESET_SPEED_RAMP -> {
                val nextSettings = ReadingSettings.read(this)
                val nextOptions = ListeningOptions.read(this)
                automaticSpeed.configure(nextSettings.speed, nextOptions.autoRamp)
                if (intent.action == RESET_SPEED_RAMP) automaticSpeed.reset()
                val session = offlineSession
                if (!current.playing && !current.loading) {
                    if (current.bookId != null) current = current.copy(effectiveSpeed = automaticSpeed.rate)
                } else if (session != null && session.settings.voice == nextSettings.voice &&
                    session.options.copy(autoRamp = nextOptions.autoRamp) == nextOptions
                ) {
                    session.settings = nextSettings
                    session.options = nextOptions
                    applyOfflineRate(session)
                    offlineAudio.setVolume(nextSettings.volume)
                } else {
                    speechOffset = current.range?.first ?: speechOffset
                    stopUtterance()
                    startCurrent()
                }
            }
            TIMER -> setTimer(intent.getIntExtra("minutes", 0))
        }
        return START_NOT_STICKY
    }

    private fun open(id: String, index: Int, offset: Int = -1) {
        if (current.bookId == id && document != null) {
            if (index >= 0 && (index != current.index || offset >= 0 || current.completed))
                seek(index, keepPlaying = false, offset = offset.coerceAtLeast(0))
            startCurrent()
            return
        }
        if (current.total > 0 && !current.completed)
            persist(current.index, current.range?.first ?: speechOffset)
        stopUtterance()
        normalSpeedRetryPass = -1
        cachedVoices = emptyList()
        appliedVoiceName = null
        speechOffset = offset.coerceAtLeast(0)
        automaticSpeed.reset()
        document = null
        preparedParagraph = null
        preparedLocation = null
        val ticket = ++request
        current = Playback(bookId = id, title = "Tuturium", loading = true, timerUntil = current.timerUntil)
        if (!promote()) return
        scope.launch {
            try {
                val (book, content) = withContext(Dispatchers.IO) {
                    val library = Library.get(this@ReadingService)
                    val book = library.book(id) ?: error("Dokumen sudah dihapus.")
                    book to library.document(id)
                }
                if (ticket != request) return@launch
                document = content
                val start = (if (index >= 0) index else book.position.takeIf { it < book.count } ?: 0)
                    .coerceIn(0, content.passes.lastIndex)
                speechOffset = (if (offset >= 0) offset else if (start == book.position) book.wordOffset else 0)
                    .coerceIn(0, content.passes[start].text.lastIndex.coerceAtLeast(0))
                current = current.copy(title = book.title, index = start, total = content.passes.size)
                persist(start, speechOffset)
                if (current.loading) startCurrent() else publish()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                android.util.Log.e("TuturiumPlayback", "Document read failed", error)
                if (ticket == request) fail("Dokumen gagal dibuka. Coba impor ulang jika berkas rusak.")
            }
        }
    }

    /** Errors in a TTS engine must not crash the app or look like damaged PDF files. */
    private fun startCurrent() {
        try {
            startCurrentInternal()
        } catch (error: Exception) {
            android.util.Log.e("TuturiumPlayback", "Cannot start speech", error)
            val kind = error.javaClass.simpleName.take(45)
            fail("Gagal pada tahap $startupStage ($kind). Coba ulangi atau kirim pesan ini.")
        }
    }

    private fun startCurrentInternal() {
        if (offlineSession != null) return
        startupStage = "menyiapkan dokumen"
        val document = document ?: return
        val pass = document.passes.getOrNull(current.index) ?: return
        startupStage = "layanan audio"
        if (!promote()) return
        val settings = ReadingSettings.read(this)
        val offline = OfflineCatalog.voice(settings.voice)
        if (offline == null && initializationFailed) {
            fail("Google TTS belum siap. Buka Pustaka Suara dan periksa suara Indonesia.")
            return
        }
        if (offline == null && !ready) {
            current = current.copy(loading = true, error = null)
            publish()
            return
        }
        startupStage = "membaca pengaturan"
        val options = ListeningOptions.read(this)
        automaticSpeed.configure(settings.speed, options.autoRamp)
        startupStage = "menyiapkan teks"
        val sourceSpeech = preparePass(document, pass, options)
        val prepared = if (offline == null && options.matchLanguage) sourceSpeech.normalizedForNarration()
            else sourceSpeech
        val from = if (speechOffset <= 0) 0 else prepared.offsets.indexOfFirst { it >= speechOffset }
        if (prepared.text.isBlank() || from < 0) {
            speechOffset = 0
            if (current.index < current.total - 1) {
                val next = current.index + 1
                current = current.copy(index = next, range = null, playing = false, loading = true)
                val skipped = current
                handler.post { if (current === skipped) startCurrent() }
            } else completeBook()
            return
        }
        activePrepared = prepared
        preparedStart = from.coerceAtLeast(0)
        startupStage = "memilih suara Indonesia"
        if (offline == null) {
            val engine = tts ?: return
            if (cachedVoices.isEmpty() || (settings.voice != null &&
                    cachedVoices.none { it.name == settings.voice })) {
                cachedVoices = engine.indonesianCandidates()
            }
            val choice = resolveIndonesianVoice(cachedVoices, settings.voice)
            val defaultVoice = if (settings.voice == null && !options.matchLanguage) {
                val language = java.util.Locale.getDefault().language
                engine.voice?.takeIf { it.locale.language == language && !it.isNetworkConnectionRequired }
                    ?: engine.availableVoices().firstOrNull {
                        it.locale.language == language && !it.isNetworkConnectionRequired
                    }
            } else null
            val name = defaultVoice?.name ?: choice?.name
            if (name == null) {
                fail("Google TTS tidak menyediakan suara Indonesia. Periksa Pustaka Suara.")
                return
            }
            if (appliedVoiceName != name) {
                val activated = if (defaultVoice != null) {
                    runCatching { engine.setVoice(defaultVoice) == TextToSpeech.SUCCESS }.getOrDefault(false)
                } else engine.activateIndonesianVoice(checkNotNull(choice))
                if (!activated) {
                    fail("Suara tidak dapat dipakai. Periksa data Google TTS.")
                    return
                }
                appliedVoiceName = name
            }
        }
        // Unlike preview, the reader owns audio focus, a media notification and
        // a wakelock. An OEM exception in these optional components must not
        // prevent the same successfully previewed voice from speaking.
        startupStage = "fokus audio"
        val desiredFocus = if (options.mixWithMusic) duckingFocus else exclusiveFocus
        if (focus !== desiredFocus) {
            if (hasFocus) runCatching { audio.abandonAudioFocusRequest(focus) }
                .onFailure { android.util.Log.w("TuturiumPlayback", "Abandon focus failed", it) }
            hasFocus = false
            focus = desiredFocus
        }
        if (!hasFocus) {
            val focusResult = runCatching { audio.requestAudioFocus(focus) }
                .onFailure { android.util.Log.w("TuturiumPlayback", "Audio focus request failed", it) }
                .getOrNull()
            when (focusResult) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> hasFocus = true
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                    fail("Audio sedang dipakai aplikasi lain. Coba tutup audio lain atau aktifkan Padukan dengan Musik.")
                    return
                }
                null -> {
                    // Some OEM audio services throw instead of returning a
                    // result. Google TTS can still speak without owning focus.
                    hasFocus = false
                }
            }
        }
        startupStage = "mengatur suara"
        val requestedSpeed = if (normalSpeedRetryPass == current.index) 1f
            else automaticSpeed.rate
        if (offline != null) {
            startOffline(document, offline, settings, options, prepared, from)
            return
        }
        val engine = tts ?: return
        val usableSpeed = runCatching {
            if (engine.setSpeechRate(requestedSpeed) == TextToSpeech.SUCCESS) requestedSpeed
            else if (engine.setSpeechRate(1f) == TextToSpeech.SUCCESS) 1f else requestedSpeed
        }.onFailure { android.util.Log.w("TuturiumPlayback", "Set speech rate failed", it) }
            .getOrDefault(1f)
        runCatching { engine.setPitch(settings.pitch) }
            .onFailure { android.util.Log.w("TuturiumPlayback", "Set TTS pitch failed", it) }
        startupStage = "mengaktifkan pemutar"
        val token = (++utterance).toString()
        activeUtterance = token
        runCatching { lock.acquire(300_000) }
            .onFailure { android.util.Log.w("TuturiumPlayback", "WakeLock unavailable", it) }
        current = current.copy(playing = false, loading = true, completed = false,
            effectiveSpeed = usableSpeed, range = null, error = null)
        startupStage = "mengirim teks ke Google TTS"
        val sessionId = narrationSessionId
        runCatching { narrationEnhancer?.setTargetGain(narrationBoostMb(settings.volume)) }
        val result = runCatching {
            engine.speak(prepared.text.substring(preparedStart),
                TextToSpeech.QUEUE_FLUSH, android.os.Bundle().apply {
                    putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, settings.volume)
                    if (sessionId != null) putInt(TextToSpeech.Engine.KEY_PARAM_SESSION_ID, sessionId)
                }, token)
        }.onFailure { android.util.Log.e("TuturiumPlayback", "Google TTS speak exception", it) }
            .getOrDefault(TextToSpeech.ERROR)
        if (result == TextToSpeech.ERROR) {
            if (!retryAtNormalSpeed(TextToSpeech.ERROR)) {
                fail("Google TTS menolak pembacaan. Periksa koneksi dan coba pratinjau suara lainnya.")
            }
        } else {
            startupStage = "memperbarui pemutar"
            publish()
        }
    }

    private fun preparePass(document: ReadingDocument, pass: Pass, options: ListeningOptions): PreparedSpeech {
        val location = pass.section to pass.paragraph
        return if (options.skipEnabled) {
            if (location != preparedLocation || options != preparedOptions || preparedParagraph == null) {
                preparedParagraph = safePreparedSpeech(document.paragraphs[pass.section][pass.paragraph], options)
                preparedLocation = location
                preparedOptions = options
            }
            checkNotNull(preparedParagraph).forPass(pass)
        } else prepareSpeech(pass.text, options)
    }

    private fun applyOfflineRate(session: OfflineSession) {
        val speed = automaticSpeed.rate
        if (current.playing && current.effectiveSpeed != speed) recordListeningTime()
        offlineAudio.setRate(speed, session.settings.pitch)
        current = current.copy(effectiveSpeed = speed)
    }

    private fun startOffline(
        document: ReadingDocument, voice: OfflineVoice, settings: ReadingSettings, options: ListeningOptions,
        initial: PreparedSpeech, offset: Int
    ) {
        val session = OfflineSession(settings, options)
        offlineSession = session
        val startIndex = current.index
        val first = PreparedSpeech(initial.text.substring(offset),
            initial.offsets.copyOfRange(offset, initial.offsets.size))
        val passages = sequence {
            yield(OfflinePass(startIndex, first, document.pauseAfter(startIndex, options).totalMs))
            for (index in startIndex + 1 until document.passes.size) {
                val speech = preparePass(document, document.passes[index], options)
                if (speech.text.isNotBlank()) yield(OfflinePass(index, speech, document.pauseAfter(index, options).totalMs))
            }
        }
        val speed = automaticSpeed.rate
        current = current.copy(playing = false, loading = true, completed = false,
            effectiveSpeed = speed, range = null, error = null)
        runCatching { lock.acquire(300_000) }
        publish()
        offlineAudio.play(scope, voice, passages, speed, settings.pitch, volume = settings.volume,
            onPass = { pass ->
                speechOffset = pass.speech.offsets.first()
                current = current.copy(index = pass.index, range = null)
                applyOfflineRate(session)
                persist(pass.index, speechOffset)
                runCatching { lock.acquire(300_000) }
            },
            onSpoken = { pass ->
                val words = countDocumentWords(pass.speech.text)
                val rate = current.effectiveSpeed
                val context = applicationContext
                automaticSpeed.spoken(words)
                positionWrites.launch { ListeningStats.recordUtterance(context, words, rate) }
            },
            onRange = { range ->
                current = current.copy(range = range)
                val now = SystemClock.elapsedRealtime()
                if (now - lastCheckpoint >= 2_000L) {
                    lastCheckpoint = now
                    persist(current.index, range.first)
                }
            },
            onRendering = { rendering ->
                current = current.copy(playing = rendering, loading = !rendering)
                publish()
            },
            onDone = { offlineSession = null; completeBook() },
            onError = { fail("Suara Supertonic gagal: " + it.message.orEmpty()) }
        )
    }

    private fun seek(index: Int, keepPlaying: Boolean = true, offset: Int = 0) {
        if (document == null) return
        val next = index.coerceIn(0, current.total - 1)
        val active = keepPlaying && (current.playing || current.loading)
        stopUtterance()
        normalSpeedRetryPass = -1
        speechOffset = offset.coerceIn(0,
            (document?.passes?.getOrNull(next)?.text?.lastIndex ?: 0).coerceAtLeast(0))
        current = current.copy(index = next, completed = false, range = null, error = null)
        persist(next, speechOffset)
        if (active) startCurrent() else publish()
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            handler.post {
                if (utteranceId != null && utteranceId == activeUtterance) {
                    current = current.copy(playing = true, loading = false)
                    publish()
                }
            }
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            handler.post {
                if (utteranceId != null && utteranceId == activeUtterance && start >= 0 && end > start) {
                    val mappedStart = activePrepared.offsets.getOrNull(preparedStart + start)
                    val mappedEnd = activePrepared.offsets.getOrNull(preparedStart + end - 1)
                    if (mappedStart != null && mappedEnd != null) {
                        current = current.copy(range = mappedStart..mappedEnd)
                        val now = SystemClock.elapsedRealtime()
                        if (now - lastCheckpoint >= 2000L) {
                            lastCheckpoint = now
                            persist(current.index, mappedStart)
                        }
                    }
                }
            }
        }

        override fun onDone(utteranceId: String?) {
            handler.post {
                if (utteranceId == null || utteranceId != activeUtterance) return@post
                val words = countDocumentWords(activePrepared.text.substring(preparedStart))
                val speed = current.effectiveSpeed
                val context = applicationContext
                positionWrites.launch { ListeningStats.recordUtterance(context, words, speed) }
                automaticSpeed.spoken(words)
                runCatching { if (lock.isHeld) lock.release() }
                    .onFailure { android.util.Log.w("TuturiumPlayback", "Release wakelock failed", it) }
                if (current.index + 1 < current.total) {
                    normalSpeedRetryPass = -1
                    speechOffset = 0
                    val extra = document?.pauseAfter(current.index, ListeningOptions.read(this@ReadingService))
                        ?.extraMs ?: 0
                    val finishedIndex = current.index
                    val advance = Runnable {
                        if (current.index == finishedIndex && activeUtterance == utteranceId &&
                            (current.playing || current.loading)) {
                            current = current.copy(index = current.index + 1, range = null)
                            persist(current.index)
                            startCurrent()
                        }
                    }
                    if (extra > 0) handler.postDelayed(advance, (extra / speed.coerceAtLeast(0.5f)).toLong())
                    else advance.run()
                } else completeBook()
            }
        }

        @Deprecated("Required by the Android listener")
        override fun onError(utteranceId: String?) {
            reportSpeechError(utteranceId, TextToSpeech.ERROR)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            reportSpeechError(utteranceId, errorCode)
        }
    }

    private fun completeBook() {
        persist(current.total)
        normalSpeedRetryPass = -1
        speechOffset = 0
        current = current.copy(index = 0, completed = true, range = null)
        pause()
        val completed = current
        continuation = scope.launch {
            val next = withContext(positionDispatcher) {
                var result: String? = null
                for (attempt in 0 until 50) {
                    val candidate = BookActions.dequeue(applicationContext) ?: break
                    if (Library.get(applicationContext).book(candidate) != null) {
                        result = candidate
                        break
                    }
                }
                result
            }
            if (next != null && current == completed) open(next, -1)
        }
    }

    private fun recordListeningTime() {
        if (listeningStart == 0L) return
        val now = SystemClock.elapsedRealtime()
        val elapsed = now - listeningStart
        val speed = listeningSpeed
        val context = applicationContext
        listeningStart = now
        positionWrites.launch { ListeningStats.recordDuration(context, elapsed, speed) }
    }

    /** Retry only genuine synthesis errors: a successful 1× preview may not
     * support the user's faster reader rate. Never loop on connectivity errors. */
    private fun retryAtNormalSpeed(code: Int): Boolean {
        val recoverable = code !in setOf(
            TextToSpeech.ERROR_NETWORK, TextToSpeech.ERROR_NETWORK_TIMEOUT,
            TextToSpeech.ERROR_NOT_INSTALLED_YET)
        if (!shouldRetrySpeechAtNormalSpeed(current.effectiveSpeed,
                normalSpeedRetryPass == current.index, recoverable)) return false
        normalSpeedRetryPass = current.index
        speechOffset = current.range?.first ?: speechOffset
        android.util.Log.w("TuturiumPlayback", "Retrying pass at 1x after TTS error $code")
        stopUtterance()
        current = current.copy(playing = false, loading = true, range = null, error = null)
        handler.post { if (current.loading) startCurrent() }
        return true
    }

    private fun reportSpeechError(id: String?, code: Int) {
        handler.post {
            if (id != null && id == activeUtterance) {
                if (retryAtNormalSpeed(code)) return@post
                val message = when (code) {
                    TextToSpeech.ERROR_NOT_INSTALLED_YET -> "Suara Google TTS belum diunduh."
                    TextToSpeech.ERROR_NETWORK, TextToSpeech.ERROR_NETWORK_TIMEOUT ->
                        "Suara Indonesia online tidak tersedia. Periksa koneksi atau pilih suara offline."
                    else -> "Google TTS gagal membacakan teks. Coba ganti suara di Pustaka Suara."
                }
                android.util.Log.e("TuturiumPlayback", "TTS error code $code")
                fail(message)
            }
        }
    }

    private fun persist(index: Int, offset: Int = 0) {
        val id = current.bookId ?: return
        val context = applicationContext
        positionWrites.launch {
            runCatching { Library.get(context).position(id, index, offset) }
                .onFailure { android.util.Log.e("TuturiumPlayback", "Position save failed", it) }
        }
    }

    private fun pause(keepFocus: Boolean = false) {
        speechOffset = current.range?.first ?: speechOffset
        if (current.total > 0 && !current.completed) persist(current.index, speechOffset)
        stopUtterance()
        if (!keepFocus) {
            resumeOnFocus = false
            if (hasFocus) runCatching { audio.abandonAudioFocusRequest(focus) }
                .onFailure { android.util.Log.w("TuturiumPlayback", "Abandon focus failed", it) }
            hasFocus = false
        }
        current = current.copy(playing = false, loading = false)
        handler.removeCallbacks(leaveForeground)
        if (foreground && !keepFocus) handler.postDelayed(leaveForeground, 60_000)
        publish()
    }

    private fun stopUtterance() {
        activeUtterance = null
        offlineSession = null
        offlineAudio.stop()
        runCatching { tts?.stop() }.onFailure {
            android.util.Log.w("TuturiumPlayback", "TTS stop failed", it)
        }
        runCatching { if (lock.isHeld) lock.release() }
            .onFailure { android.util.Log.w("TuturiumPlayback", "Release wakelock failed", it) }
    }

    private fun setTimer(minutes: Int) {
        handler.removeCallbacks(timer)
        val delay = minutes.coerceIn(0, 120) * 60_000L
        current = current.copy(timerUntil = if (delay > 0) SystemClock.elapsedRealtime() + delay else 0)
        if (delay > 0) handler.postDelayed(timer, delay)
    }

    private fun fail(message: String) {
        current = current.copy(error = message)
        runCatching { pause() }.onFailure {
            android.util.Log.e("TuturiumPlayback", "Failed to pause after playback error", it)
            current = current.copy(playing = false, loading = false, error = message)
        }
    }

    private fun finish() {
        pause()
        continuation?.cancel()
        continuation = null
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION)
        stopSelf()
    }

    private fun promote(): Boolean {
        handler.removeCallbacks(leaveForeground)
        if (foreground) return true
        return try {
            val type = if (Build.VERSION.SDK_INT >= 29) ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK else 0
            ServiceCompat.startForeground(this, NOTIFICATION, notification(), type)
            foreground = true
            true
        } catch (error: Exception) {
            android.util.Log.e("TuturiumPlayback", "Could not start foreground media service", error)
            current = current.copy(playing = false, loading = false,
                error = "Pemutar audio diblokir Android. Buka Tuturium dan coba lagi.")
            getSystemService(NotificationManager::class.java).cancel(NOTIFICATION)
            stopSelf()
            false
        }
    }

    // Android exempts MediaStyle notifications backed by an active media session.
    @SuppressLint("NotificationPermission")
    private fun publish() {
        if (current.bookId == null) return
        runCatching {
        val state = when {
            current.loading -> PlaybackState.STATE_BUFFERING
            current.playing -> PlaybackState.STATE_PLAYING
            else -> PlaybackState.STATE_PAUSED
        }
        session.setMetadata(MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, current.title)
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "Tuturium").build())
        session.setPlaybackState(PlaybackState.Builder().setActions(
            PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE or PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS or PlaybackState.ACTION_STOP
        ).setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, if (current.playing) 1f else 0f).build())
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION, notification())
        }.onFailure { android.util.Log.e("TuturiumPlayback", "Media notification failed", it) }
    }

    private fun openApp(): PendingIntent = PendingIntent.getActivity(
        this, 10, Intent(this, MainActivity::class.java).setAction(OPEN_PLAYER)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private fun action(icon: Int, label: String, action: String): Notification.Action {
        val intent = Intent(this, ReadingService::class.java).setAction(action)
        val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val pending = if (action == PLAY) PendingIntent.getForegroundService(this, 0, intent, flags)
            else PendingIntent.getService(this, 0, intent, flags)
        return Notification.Action.Builder(android.graphics.drawable.Icon.createWithResource(this, icon), label, pending).build()
    }

    private fun notification(): Notification = Notification.Builder(this, CHANNEL)
        .setSmallIcon(R.drawable.status_audio)
        .setContentTitle(current.title.ifBlank { "Tuturium" })
        .setContentText(if (current.loading) "Menyiapkan suara" else if (current.total > 0) {
            "Bagian ${current.index + 1} dari ${current.total}"
        } else "Pemutar dokumen")
        .setContentIntent(openApp())
        .setOnlyAlertOnce(true)
        .setVisibility(Notification.VISIBILITY_PUBLIC)
        .setOngoing(current.playing || current.loading)
        .setStyle(Notification.MediaStyle().setMediaSession(session.sessionToken).setShowActionsInCompactView(0, 1, 2))
        .addAction(action(android.R.drawable.ic_media_previous, "Sebelumnya", PREVIOUS))
        .addAction(if (current.playing || current.loading) action(android.R.drawable.ic_media_pause, "Jeda", PAUSE)
            else action(android.R.drawable.ic_media_play, "Putar", PLAY))
        .addAction(action(android.R.drawable.ic_media_next, "Selanjutnya", NEXT))
        .addAction(action(android.R.drawable.ic_menu_close_clear_cancel, "Tutup pemutar", STOP))
        .build()

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        recordListeningTime()
        if (current.total > 0 && !current.completed) persist(current.index, current.range?.first ?: speechOffset)
        stopUtterance()
        if (hasFocus) runCatching { audio.abandonAudioFocusRequest(focus) }
        unregisterReceiver(noisy)
        tts?.shutdown()
        narrationEnhancer?.release()
        session.release()
        scope.cancel()
        getSystemService(NotificationManager::class.java).cancel(NOTIFICATION)
        mutable.value = Playback(error = current.error)
        super.onDestroy()
    }

    companion object {
        const val OPEN_PLAYER = "id.tuturium.app.OPEN_PLAYER"
        const val PLAY = "PLAY"
        const val PAUSE = "PAUSE"
        const val STOP = "STOP"
        const val NEXT = "NEXT"
        const val PREVIOUS = "PREVIOUS"
        const val SEEK = "SEEK"
        const val TIMER = "TIMER"
        const val SETTINGS = "SETTINGS"
        internal const val RESET_SPEED_RAMP = "RESET_SPEED_RAMP"
        const val VOLUME = "VOLUME"
        private const val CHANNEL = "reading"
        private const val NOTIFICATION = 41
        internal val positionDispatcher = Dispatchers.IO.limitedParallelism(1)
        private val positionWrites = CoroutineScope(SupervisorJob() + positionDispatcher)
        private val mutable = MutableStateFlow(Playback())
        val state = mutable.asStateFlow()
        val chromeState = state.map { it.copy(range = null, timerUntil = 0L, effectiveSpeed = 1f) }.distinctUntilChanged()

        fun command(context: Context, action: String, book: String? = null,
            index: Int = -1, minutes: Int = 0, offset: Int = -1): Boolean {
            val intent = Intent(context, ReadingService::class.java).setAction(action)
                .putExtra("book", book).putExtra("index", index).putExtra("minutes", minutes)
                .putExtra("offset", offset)
            return try {
                if (action == PLAY) ContextCompat.startForegroundService(context, intent)
                else context.startService(intent)
                true
            } catch (error: Exception) {
                android.util.Log.e("TuturiumPlayback", "Cannot send $action to service", error)
                false
            }
        }
    }
}
