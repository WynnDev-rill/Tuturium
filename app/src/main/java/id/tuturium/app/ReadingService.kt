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

fun TextToSpeech.offlineVoices(): List<Voice> = voices.orEmpty()
    .filter { !it.isNetworkConnectionRequired && TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in it.features }

fun Voice.choice() = VoiceChoice(name, locale.toLanguageTag().substringBefore('-'), locale.getDisplayName(Locale.forLanguageTag("id")), quality)

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
    private var document: ReadingDocument? = null
    private var request = 0
    private var utterance = 0
    private var activeUtterance: String? = null
    private var speechOffset = 0
    private var resumeOnFocus = false
    private var hasFocus = false
    private var foreground = false
    private var listeningStart = 0L
    private var listeningSpeed = 1f
    private var current = Playback()
        set(value) {
            if (field.playing && !value.playing && listeningStart > 0L) {
                ListeningStats.recordDuration(this, SystemClock.elapsedRealtime() - listeningStart, listeningSpeed)
                listeningStart = 0L
            } else if (!field.playing && value.playing) {
                listeningStart = SystemClock.elapsedRealtime()
                listeningSpeed = ReadingSettings.read(this).speed
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
        tts = TextToSpeech(this) { result ->
            handler.post {
                handler.removeCallbacks(initTimeout)
                ready = result == TextToSpeech.SUCCESS
                initializationFailed = !ready
                if (ready) {
                    tts?.setAudioAttributes(attributes)
                    tts?.setOnUtteranceProgressListener(listener)
                    if (current.loading && document != null) startCurrent()
                } else if (current.loading) {
                    fail("Mesin TTS Android belum tersedia.")
                }
            }
        }
        handler.postDelayed(initTimeout, 15_000)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            PLAY -> {
                val id = intent.getStringExtra("book")
                if (id != null) open(id, intent.getIntExtra("index", -1))
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
            SEEK -> seek(intent.getIntExtra("index", 0))
            SETTINGS -> if (current.playing) {
                speechOffset = current.range?.first ?: speechOffset
                startCurrent()
            }
            TIMER -> setTimer(intent.getIntExtra("minutes", 0))
        }
        return START_NOT_STICKY
    }

    private fun open(id: String, index: Int) {
        if (current.bookId == id && document != null) {
            if (index >= 0 && current.index != index) seek(index, keepPlaying = false)
            startCurrent()
            return
        }
        stopUtterance()
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
            } catch (_: Exception) {
                if (ticket == request) fail("Dokumen tidak dapat dibuka.")
            }
        }
    }

    private fun startCurrent() {
        val pass = document?.passes?.getOrNull(current.index) ?: return
        if (!promote()) return
        if (initializationFailed) {
            fail("Mesin TTS Android belum tersedia. Periksa setelan perangkat.")
            return
        }
        if (!ready) {
            current = current.copy(loading = true, error = null)
            publish()
            return
        }
        val engine = tts ?: return
        val settings = ReadingSettings.read(this)
        val available = engine.offlineVoices()
        val selected = chooseVoice(available.map { it.choice() }, settings.voice, Locale.getDefault().language)
        val voice = available.firstOrNull { it.name == selected?.name }
        if (voice == null || engine.setVoice(voice) != TextToSpeech.SUCCESS) {
            fail("Belum ada suara offline. Unduh data suara melalui setelan TTS Android.")
            return
        }
        if (!hasFocus) {
            hasFocus = audio.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            if (!hasFocus) {
                fail("Audio sedang digunakan aplikasi lain.")
                return
            }
        }
        engine.setSpeechRate(settings.speed)
        engine.setPitch(settings.pitch)
        val token = (++utterance).toString()
        activeUtterance = token
        lock.acquire(300_000)
        val offset = speechOffset.coerceIn(0, pass.text.lastIndex)
        current = current.copy(playing = true, loading = false, completed = false, range = null, error = null)
        if (engine.speak(pass.text.substring(offset), TextToSpeech.QUEUE_FLUSH, null, token) == TextToSpeech.ERROR) {
            fail("Suara gagal diputar. Coba pilih suara offline lainnya.")
        } else publish()
    }

    private fun seek(index: Int, keepPlaying: Boolean = true) {
        if (document == null) return
        val next = index.coerceIn(0, current.total - 1)
        val active = keepPlaying && (current.playing || current.loading)
        stopUtterance()
        speechOffset = 0
        current = current.copy(index = next, completed = false, range = null, error = null)
        persist(next)
        if (active) startCurrent() else publish()
    }

    private val listener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) = Unit

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            handler.post {
                if (utteranceId == activeUtterance && start >= 0 && end > start) {
                    current = current.copy(range = (speechOffset + start) until (speechOffset + end))
                }
            }
        }

        override fun onDone(utteranceId: String?) {
            handler.post {
                if (utteranceId != activeUtterance || !current.playing) return@post
                document?.passes?.getOrNull(current.index)?.let { pass ->
                    ListeningStats.recordUtterance(this@ReadingService, pass.text,
                        ReadingSettings.read(this@ReadingService).speed)
                }
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
            handler.post {
                if (utteranceId == activeUtterance) fail("Suara gagal diputar. Periksa data suara offline.")
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
            if (hasFocus) audio.abandonAudioFocusRequest(focus)
            hasFocus = false
        }
        current = current.copy(playing = false, loading = false)
        handler.removeCallbacks(leaveForeground)
        if (foreground && !keepFocus) handler.postDelayed(leaveForeground, 60_000)
        publish()
    }

    private fun stopUtterance() {
        activeUtterance = null
        tts?.stop()
        if (lock.isHeld) lock.release()
    }

    private fun setTimer(minutes: Int) {
        handler.removeCallbacks(timer)
        val delay = minutes.coerceIn(0, 120) * 60_000L
        current = current.copy(timerUntil = if (delay > 0) SystemClock.elapsedRealtime() + delay else 0)
        if (delay > 0) handler.postDelayed(timer, delay)
    }

    private fun fail(message: String) {
        current = current.copy(error = message)
        pause()
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
        } catch (_: IllegalStateException) {
            current = current.copy(playing = false, loading = false, error = "Buka Tuturium untuk memulai pemutaran.")
            getSystemService(NotificationManager::class.java).cancel(NOTIFICATION)
            stopSelf()
            false
        }
    }

    // Android exempts MediaStyle notifications backed by an active media session.
    @SuppressLint("NotificationPermission")
    private fun publish() {
        if (current.bookId == null) return
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
        if (hasFocus) audio.abandonAudioFocusRequest(focus)
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

        fun command(context: Context, action: String, book: String? = null, index: Int = -1, minutes: Int = 0) {
            val intent = Intent(context, ReadingService::class.java).setAction(action)
                .putExtra("book", book).putExtra("index", index).putExtra("minutes", minutes)
            if (action == PLAY) ContextCompat.startForegroundService(context, intent)
            else context.startService(intent)
        }
    }
}
