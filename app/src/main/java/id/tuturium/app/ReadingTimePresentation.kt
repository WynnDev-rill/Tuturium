package id.tuturium.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
internal fun readingPosition(model: TuturiumModel, book: Book, index: Int, offset: Int,
    document: ReadingDocument? = null): ReadingPosition? {
    val options = model.listeningOptions.timelineOptions()
    val atStart = index == 0 && offset == 0
    if (book.count > 0 && index >= book.count && document == null) {
        return ReadingPosition(1f, 0)
    }
    val initial = if (atStart && book.words > 0 && !options.skipEnabled &&
        options.sentencePauseMs == 0 && options.paragraphPauseMs == 0
    ) ReadingPosition(0f, book.words) else null
    if (document == null && initial != null) return initial
    var timeline by remember(book.id, book.updated, options, document) { mutableStateOf<ReadingTimeline?>(null) }
    LaunchedEffect(book.id, book.updated, options, document) {
        timeline = model.readingTimeline(book, document, options)
    }
    return timeline?.position(index, offset) ?: initial
}

internal fun Playback.hasPosition(book: Book): Boolean = bookId == book.id && total > 0

internal fun Playback.readingSpeed(book: Book, preferred: Float): Float =
    if (hasPosition(book) && !completed) effectiveSpeed else preferred

internal fun Playback.estimatedReadingSpeed(book: Book, preferred: Float): Float =
    if (hasPosition(book)) narrationSpeed ?: preferred else preferred

@Composable
internal fun narrationMillisPerWord(model: TuturiumModel, speed: Float,
    snapshot: NarrationPaceSnapshot? = null): Double {
    val context = LocalContext.current
    val profile = PaceProfile.forPlayback(model.settings, model.listeningOptions, speed)
    if (snapshot != null) return snapshot.millisPerWord(profile)
    val revision by ReadingPace.revision.collectAsState()
    return remember(context, profile, revision) { ReadingPace.millisPerWord(context, profile) }
}

@Composable
internal fun libraryReadingLabel(model: TuturiumModel, book: Book, playback: Playback): String {
    val active = playback.hasPosition(book)
    val index = if (active) (if (playback.completed) book.count else playback.index) else book.position
    val offset = if (active) playback.offset else book.wordOffset
    val position = readingPosition(model, book, index, offset) ?: return book.format
    val speed = playback.estimatedReadingSpeed(book, model.settings.speed)
    val minutes = position.remainingMinutes(narrationMillisPerWord(model, speed), speed)
    return "${position.percent}% · ≈$minutes menit · ${book.format}"
}
