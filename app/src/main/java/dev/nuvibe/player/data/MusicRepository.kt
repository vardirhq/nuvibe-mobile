package dev.nuvibe.player.data

import android.content.Context
import android.net.Uri
import dev.nuvibe.player.data.mediastore.MediaStoreScanner
import dev.nuvibe.player.data.mediastore.MetadataWriteResult
import dev.nuvibe.player.data.mediastore.MetadataWriter
import dev.nuvibe.player.data.mediastore.TrackEdit
import dev.nuvibe.player.data.model.Library
import dev.nuvibe.player.data.model.SmartMix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Scan states so the UI can distinguish "empty" from "still loading". */
enum class ScanState { IDLE, SCANNING, READY }

/**
 * Owns the in-memory [Library] snapshot and derives the generated mixes that
 * populate the Home screen — all from real on-device media.
 */
class MusicRepository(context: Context) {

    private val scanner = MediaStoreScanner(context.applicationContext)
    private val metadataWriter = MetadataWriter(context.applicationContext)
    private val scanMutex = Mutex()

    private val _library = MutableStateFlow(Library())
    val library: StateFlow<Library> = _library.asStateFlow()

    private val _scanState = MutableStateFlow(ScanState.IDLE)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    suspend fun refresh() = scanMutex.withLock {
        _scanState.value = ScanState.SCANNING
        _library.value = scanner.scan()
        _scanState.value = ScanState.READY
    }

    /** Writes [edit] to [uri], rescanning the library on success. */
    suspend fun writeMetadata(uri: Uri, edit: TrackEdit): MetadataWriteResult {
        val result = metadataWriter.write(uri, edit)
        if (result is MetadataWriteResult.Success) refresh()
        return result
    }

    /** Deterministic "smart" mixes derived from the current library. */
    fun smartMixes(library: Library): List<SmartMix> {
        if (library.isEmpty) return emptyList()
        val mixes = mutableListOf<SmartMix>()

        mixes += SmartMix(
            key = "shuffle_all",
            name = "Shuffle all",
            description = "${library.tracks.size} songs · your whole room",
            trackIds = library.tracks.map { it.id }.shuffled(kotlin.random.Random(1)),
            gradientSeed = "Shuffle all",
        )

        val recentlyAdded = library.tracks.sortedByDescending { it.dateAddedSec }.take(30)
        if (recentlyAdded.size >= 4) {
            mixes += SmartMix(
                key = "recently_added",
                name = "Fresh additions",
                description = "Newest ${recentlyAdded.size} in your collection",
                trackIds = recentlyAdded.map { it.id },
                gradientSeed = "Fresh additions",
            )
        }

        // A mix per top artist by track count.
        library.artists.sortedByDescending { it.trackCount }
            .take(2)
            .filter { it.trackCount >= 3 }
            .forEach { artist ->
                val ids = library.tracks.filter { it.artistId == artist.id }.map { it.id }
                mixes += SmartMix(
                    key = "artist_${artist.id}",
                    name = "${artist.name} mix",
                    description = "${ids.size} songs · deep cut",
                    trackIds = ids.shuffled(kotlin.random.Random(artist.id)),
                    gradientSeed = artist.name,
                )
            }

        return mixes
    }
}
