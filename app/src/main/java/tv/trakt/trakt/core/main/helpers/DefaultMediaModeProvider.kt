package tv.trakt.trakt.core.main.helpers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import tv.trakt.trakt.core.main.model.MediaMode

private val KEY_MAIN_MODE = stringPreferencesKey("key_main_mode")

internal class DefaultMediaModeProvider(
    private val dataStore: DataStore<Preferences>,
) : MediaModeProvider {
    private var currentMode = runBlocking {
        val data = dataStore.data.first()
        val modeName = data[KEY_MAIN_MODE] ?: MediaMode.MEDIA.name
        MediaMode.valueOf(modeName)
    }.also {
        Timber.d("Media mode initialized: $it")
    }

    private val currentModeFlow = MutableSharedFlow<MediaMode>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override suspend fun setMode(mode: MediaMode) {
        currentMode = mode
        currentModeFlow.emit(mode)

        dataStore.edit {
            it[KEY_MAIN_MODE] = mode.name
        }

        Timber.d("Media mode set: $mode")
    }

    override fun getMode(): MediaMode {
        Timber.d("Media mode get: $currentMode")
        return currentMode
    }

    override fun observeMode(): Flow<MediaMode> {
        return currentModeFlow
    }
}
