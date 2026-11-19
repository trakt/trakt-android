package tv.trakt.trakt.core.summary.livingposter.usecases

import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.data.local.LivingPosterLocalSource
import tv.trakt.trakt.common.data.remote.MediaImagesRemoteSource
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.livingposter.LivingPoster
import tv.trakt.trakt.common.model.livingposter.LivingPosterIndex

internal class GetLivingPosterUseCase(
    private val remoteSource: MediaImagesRemoteSource,
    private val localSource: LivingPosterLocalSource,
) {
    suspend fun getLivingPoster(
        type: MediaType,
        mediaId: TraktId,
    ): LivingPoster? {
        val index = getIndex() ?: return null
        if (!index.contains(type, mediaId)) return null

        val folder = when (type) {
            MediaType.Movie -> "movies"
            MediaType.Show -> "shows"
            MediaType.Season, MediaType.Episode -> return null
        }
        val baseUrl = "${Config.MEDIA_BASE_URL}$folder/${mediaId.value}/live"

        localSource.getPoster(baseUrl)?.let { return it }

        val poster = LivingPoster.fromDto(
            dto = remoteSource.getLivingPoster(baseUrl),
            baseUrl = baseUrl,
        ) ?: return null

        localSource.setPoster(baseUrl, poster)
        return poster
    }

    private suspend fun getIndex(): LivingPosterIndex? {
        localSource.getIndex()?.let { return it }

        val index = LivingPosterIndex.fromDto(remoteSource.getLivingPosterIndex()) ?: return null
        localSource.setIndex(index)
        return index
    }
}
