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
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

data class ReadingSettings(val voice: String?, val speed: Float, val pitch: Float) {
    companion object {
        fun read(context: Context): ReadingSettings = context.getSharedPreferences("settings", 0).let {
            ReadingSettings(it.getString("voice", null), it.getFloat("speed", 1f), it.getFloat("pitch", 1f))
        }
    }
    fun save(context: Context) {
        context.getSharedPreferences("settings", 0).edit()
            .putString("voice", voice).putFloat("speed", speed).putFloat("pitch", pitch).apply()
        if (ReadingService.state.value.bookId != null) ReadingService.command(context, ReadingService.SETTINGS)
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
    private lateinit var lock: PowerManager.WakeLock
    private var tts: TextToSpeech? = null
    private var ready = false
    private var initializationFailed = false
    // A stable voice configuration is reused across consecutive sentences.
    // Re-enumerating and re-selecting network voices on each onDone can disrupt
    // a working Google TTS session mid-book.
    private var cachedVoices: List<VoiceChoice> = emptyList()
    private var appliedVoiceName: String? = null
    private var startupStage = "persiapan"
    private var document: ReadingDocument? = null
    private var request = 0
    private var utterance = 0
    private var activeUtterance: String? = null
    private var speechOffset = 0
    private var resumeOnFocus = false
    private var hasFocus = false
    private var foreground = false
    private var activePrepared = PreparedSpeech("", IntArray(0))
    private var preparedStart = 0
    private var listenedWords = 0
    private var listeningStart = 0L
    private var listeningSpeed = 1f
    private var current = Playback()
        set(value) {
            if (field.playing && !value.playing && listeningStart > 0L) {
                runCatching {
                    ListeningStats.recordDuration(this,
                        SystemClock.elapsedRealtime() - listeningStart, listeningSpeed)
                }.onFailure { android.util.Log.w("TuturiumPlayback", "Listening stats failed", it) }
                listeningStart = 0L
            } else if (!field.playing && value.playing) {
                listeningStart = SystemClock.elapsedRealtime()
                listeningSpeed = value.effectiveSpeed
            }
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
            if (current.loading) fail("Mesin TTS Android tidak merespons.")
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
        focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(attributes).setWillPauseWhenDucked(true)
            .setOnAudioFocusChangeListener({ change ->
                when (change) {
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                        if (current.playing) {
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
                    if (current.loading && document != null) startCurrent()
                } else if (current.loading) {
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
                if (id != null) open(id, intent.getIntExtra("index", -1), intent.getIntExtra("offset", 0))
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
            SEEK -> seek(intent.getIntExtra("index", 0), offset = intent.getIntExtra("offset", 0))
            SETTINGS -> if (current.playing) {
                speechOffset = current.range?.first ?: speechOffset
                stopUtterance()
                startCurrent()
            }
            TIMER -> setTimer(intent.getIntExtra("minutes", 0))
        }
        return START_NOT_STICKY
    }

    private fun open(id: String, index: Int, offset: Int = 0) {
        if (current.bookId == id && document != null) {
            if (index >= 0) seek(index, keepPlaying = false, offset = offset)
            startCurrent()
            return
        }
        stopUtterance()
        speechOffset = offset.coerceAtLeast(0)
        listenedWords = 0
        document = null
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
                current = current.copy(title = book.title, index = start, total = content.passes.size)
                persist(start)
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
            fail("Pemutar gagal pada tahap $startupStage. Coba putar lagi; " +
                "jika masih gagal, laporkan tahap ini.")
        }
    }

    private fun startCurrentInternal() {
        startupStage = "menyiapkan dokumen"
        val pass = document?.passes?.getOrNull(current.index) ?: return
        startupStage = "layanan audio"
        if (!promote()) return
        if (initializationFailed) {
            fail("Google TTS belum siap. Buka Pustaka Suara dan periksa suara Indonesia.")
            return
        }
        if (!ready) {
            current = current.copy(loading = true, error = null)
            publish()
            return
        }
        startupStage = "membaca pengaturan"
        val engine = tts ?: return
        val settings = ReadingSettings.read(this)
        val options = ListeningOptions.read(this)
        startupStage = "menyiapkan teks"
        val prepared = prepareSpeech(pass.text, options)
        val from = prepared.offsets.indexOfFirst { it >= speechOffset }
        if (prepared.text.isEmpty() || (speechOffset > 0 && from < 0)) {
            speechOffset = 0
            if (current.index < current.total - 1) {
                current = current.copy(index = current.index + 1, range = null)
                persist(current.index)
                handler.post { startCurrent() }
            } else {
                persist(current.total)
                current = current.copy(index = 0, completed = true, range = null)
                pause()
            }
            return
        }
        activePrepared = prepared
        preparedStart = from.coerceAtLeast(0)
        startupStage = "memilih suara Indonesia"
        if (cachedVoices.isEmpty() || (settings.voice != null &&
                cachedVoices.none { it.name == settings.voice })) {
            cachedVoices = engine.indonesianCandidates()
        }
        val choice = resolveIndonesianVoice(cachedVoices, settings.voice)
        if (choice == null) {
            fail("Google TTS tidak menyediakan suara Indonesia. Periksa Pustaka Suara.")
            return
        }
        if (appliedVoiceName != choice.name) {
            if (!engine.activateIndonesianVoice(choice)) {
                fail("Suara Indonesia tidak dapat dipakai. Periksa jaringan atau data Google TTS.")
                return
            }
            appliedVoiceName = choice.name
        }
        // Unlike preview, the reader owns audio focus, a media notification and
        // a wakelock. An OEM exception in these optional components must not
        // prevent the same successfully previewed voice from speaking.
        startupStage = "fokus audio"
        if (options.mixWithMusic) {
            if (hasFocus) runCatching { audio.abandonAudioFocusRequest(focus) }
                .onFailure { android.util.Log.w("TuturiumPlayback", "Abandon focus failed", it) }
            hasFocus = false
        } else if (!hasFocus) {
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
        val requestedSpeed = rampedSpeed(settings.speed, listenedWords, options.autoRamp)
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
        current = current.copy(playing = true, loading = false, completed = false,
            effectiveSpeed = usableSpeed, range = null, error = null)
        startupStage = "mengirim teks ke Google TTS"
        val result = runCatching {
            engine.speak(prepared.text.substring(preparedStart),
                TextToSpeech.QUEUE_FLUSH, null, token)
        }.onFailure { android.util.Log.e("TuturiumPlayback", "Google TTS speak exception", it) }
            .getOrDefault(TextToSpeech.ERROR)
        if (result == TextToSpeech.ERROR) {
            fail("Google TTS menolak pembacaan. Periksa koneksi dan coba pratinjau suara lainnya.")
        } else {
            startupStage = "memperbarui pemutar"
            publish()
        }
    }

    private fun seek(index: Int, keepPlaying: Boolean = true, offset: Int = 0) {
        if (document == null) return
        val next = index.coerceIn(0, current.total - 1)
        val active = keepPlaying && (current.playing || current.loading)
        stopUtterance()
        speechOffset = offset.coerceIn(0,
            (document?.passes?.getOrNull(next)?.text?.lastIndex ?: 0).coerceAtLeast(0))
        current = current.copy(index = next, completed = false, range = null, error = null)
        persist(next)
        if (active) startCurrent() else publish()
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            handler.post {
                if (utteranceId == activeUtterance && start >= 0 && end > start) {
                    val mappedStart = activePrepared.offsets.getOrNull(preparedStart + start)
                    val mappedEnd = activePrepared.offsets.getOrNull(preparedStart + end - 1)
                    if (mappedStart != null && mappedEnd != null)
                        current = current.copy(range = mappedStart..mappedEnd)
                }
            }
        }

        override fun onDone(utteranceId: String?) {
            handler.post {
                if (utteranceId != activeUtterance || !current.playing) return@post
                runCatching {
                    ListeningStats.recordUtterance(this@ReadingService, activePrepared.text,
                        current.effectiveSpeed)
                }.onFailure {
                    android.util.Log.w("TuturiumPlayback", "Listening statistics failed", it)
                }
                listenedWords += countDocumentWords(activePrepared.text)
                runCatching { if (lock.isHeld) lock.release() }
                    .onFailure { android.util.Log.w("TuturiumPlayback", "Release wakelock failed", it) }
                if (current.index + 1 < current.total) {
                    speechOffset = 0
                    current = current.copy(index = current.index + 1, range = null)
                    persist(current.index)
                    startCurrent()
                } else {
                    persist(current.total)
                    speechOffset = 0
                    current = current.copy(index = 0, completed = true, range = null)
                    pause()
                    // Continue queued books only when the current book ends normally.
                    val library = Library.get(this@ReadingService)
                    var next: String? = null
                    repeat(50) {
                        if (next == null) {
                            val candidate = BookActions.dequeue(this@ReadingService)
                            if (candidate != null && library.book(candidate) != null) next = candidate
                        }
                    }
                    next?.let { open(it, -1) }
                }
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

    private fun reportSpeechError(id: String?, code: Int) {
        handler.post {
            if (id == activeUtterance) {
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

    private fun persist(index: Int) {
        val id = current.bookId ?: return
        val context = applicationContext
        positionWrites.launch { runCatching { Library.get(context).position(id, index) } }
    }

    private fun pause(keepFocus: Boolean = false) {
        speechOffset = current.range?.first ?: speechOffset
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
        stopUtterance()
        if (hasFocus) runCatching { audio.abandonAudioFocusRequest(focus) }
        unregisterReceiver(noisy)
        tts?.shutdown()
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
        private const val CHANNEL = "reading"
        private const val NOTIFICATION = 41
        private val positionWrites = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
        private val mutable = MutableStateFlow(Playback())
        val state = mutable.asStateFlow()

        fun command(context: Context, action: String, book: String? = null,
            index: Int = -1, minutes: Int = 0, offset: Int = 0): Boolean {
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
