@file:OptIn(ExperimentalSerializationApi::class)

package tv.trakt.trakt.core.home.sections.recommended.data.media

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import timber.log.Timber
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem

private val KEY_RECOMMENDED_MEDIA = byteArrayPreferencesKey("key_recommended_media")

internal class RecommendedMediaStorage(
    private val dataStore: DataStore<Preferences>,
) : RecommendedMediaLocalDataSource {
    private val mutex = Mutex()
    private var cache: List<RecommendedItem>? = null

    override suspend fun setItems(items: List<RecommendedItem>) {
        mutex.withLock {
            write(items)
        }
    }

    override suspend fun getItems(): List<RecommendedItem> {
        return mutex.withLock {
            cache ?: read().also { cache = it }
        }
    }

    override suspend fun removeItem(
        id: TraktId,
        type: MediaType,
    ) {
        mutex.withLock {
            val items = cache ?: read()
            write(items.filterNot { it.id == id && it.type == type })
        }
    }

    private suspend fun write(items: List<RecommendedItem>) {
        cache = items
        dataStore.edit {
            it[KEY_RECOMMENDED_MEDIA] = ProtoBuf.encodeToByteArray(items)
        }
    }

    private suspend fun read(): List<RecommendedItem> {
        return try {
            dataStore.data.first()[KEY_RECOMMENDED_MEDIA]
                ?.let { ProtoBuf.decodeFromByteArray<List<RecommendedItem>>(it) }
                ?: emptyList()
        } catch (exception: SerializationException) {
            dataStore.edit { it.remove(KEY_RECOMMENDED_MEDIA) }
            Timber.e(exception)
            emptyList()
        }
    }
}
