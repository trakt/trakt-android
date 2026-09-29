package tv.trakt.trakt.core.summary.ui.views.info

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SocialIds
import tv.trakt.trakt.resources.R

@Immutable
internal data class MediaLink(
    val type: MediaLinkType,
    val url: String,
)

internal enum class MediaLinkType(
    val official: Boolean,
    val brandColored: Boolean,
    @param:DrawableRes val iconRes: Int,
) {
    Homepage(
        official = true,
        brandColored = false,
        iconRes = R.drawable.ic_official_site,
    ),
    Facebook(
        official = true,
        brandColored = false,
        iconRes = R.drawable.ic_facebook,
    ),
    X(
        official = true,
        brandColored = false,
        iconRes = R.drawable.ic_x_twitter,
    ),
    Instagram(
        official = true,
        brandColored = false,
        iconRes = R.drawable.ic_instagram,
    ),
    Imdb(
        official = false,
        brandColored = true,
        iconRes = R.drawable.ic_imdb_color,
    ),
    Tmdb(
        official = false,
        brandColored = true,
        iconRes = R.drawable.ic_tmdb,
    ),
    Wikipedia(
        official = false,
        brandColored = false,
        iconRes = R.drawable.ic_wikipedia,
    ),
}

internal fun Movie.toMediaLinks(): ImmutableList<MediaLink> {
    return mediaLinksOf(
        homepage = homepage,
        social = socialIds,
        ids = ids,
        tmdbUrl = Config::webTmdbMovieUrl,
    )
}

internal fun Show.toMediaLinks(): ImmutableList<MediaLink> {
    return mediaLinksOf(
        homepage = homepage,
        social = socialIds,
        ids = ids,
        tmdbUrl = Config::webTmdbShowUrl,
    )
}

private fun mediaLinksOf(
    homepage: String?,
    social: SocialIds?,
    ids: Ids,
    tmdbUrl: (Int) -> String,
): ImmutableList<MediaLink> {
    return listOfNotNull(
        homepage?.let { MediaLink(MediaLinkType.Homepage, it) },
        social?.facebook.ifNotBlank { MediaLink(MediaLinkType.Facebook, Config.webFacebookPersonUrl(it)) },
        social?.twitter.ifNotBlank { MediaLink(MediaLinkType.X, Config.webTwitterPersonUrl(it)) },
        social?.instagram.ifNotBlank { MediaLink(MediaLinkType.Instagram, Config.webInstagramPersonUrl(it)) },
        ids.imdb?.let { MediaLink(MediaLinkType.Imdb, Config.webImdbMediaUrl(it.value)) },
        ids.tmdb?.let { MediaLink(MediaLinkType.Tmdb, tmdbUrl(it.value)) },
        social?.wikipedia.ifNotBlank { MediaLink(MediaLinkType.Wikipedia, Config.webWikipediaMediaUrl(it)) },
    ).toImmutableList()
}

private inline fun String?.ifNotBlank(block: (String) -> MediaLink): MediaLink? {
    if (isNullOrBlank()) return null
    return block(this)
}
