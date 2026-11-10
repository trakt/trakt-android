package tv.trakt.trakt.core.summary.people.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.model.SocialIds
import tv.trakt.trakt.resources.R

private const val INLINE_LINKS_LIMIT = 3

internal enum class PersonLinkType(
    val target: String,
    @param:DrawableRes val iconRes: Int,
    @param:StringRes val labelRes: Int,
    val iconSize: Dp,
) {
    YouTube(
        target = "youtube",
        iconRes = R.drawable.ic_youtube,
        labelRes = R.string.link_label_youtube,
        iconSize = 24.dp,
    ),
    Wikipedia(
        target = "wikipedia",
        iconRes = R.drawable.ic_wikipedia,
        labelRes = R.string.link_label_wikipedia,
        iconSize = 24.dp,
    ),
    Instagram(
        target = "instagram",
        iconRes = R.drawable.ic_instagram,
        labelRes = R.string.link_label_instagram,
        iconSize = 26.dp,
    ),
    X(
        target = "x",
        iconRes = R.drawable.ic_x_twitter,
        labelRes = R.string.link_label_x,
        iconSize = 26.dp,
    ),
    TikTok(
        target = "tiktok",
        iconRes = R.drawable.ic_tiktok,
        labelRes = R.string.link_label_tiktok,
        iconSize = 22.dp,
    ),
    Facebook(
        target = "facebook",
        iconRes = R.drawable.ic_facebook,
        labelRes = R.string.link_label_facebook,
        iconSize = 26.dp,
    ),
    Wikidata(
        target = "wikidata",
        iconRes = R.drawable.ic_wikidata,
        labelRes = R.string.link_label_wikidata,
        iconSize = 24.dp,
    ),
}

@Immutable
internal data class PersonLink(
    val type: PersonLinkType,
    val url: String,
)

@Immutable
internal data class PersonLinks(
    val inline: ImmutableList<PersonLink>,
    val overflow: ImmutableList<PersonLink>,
)

internal fun SocialIds?.toPersonLinks(): PersonLinks {
    val links = listOfNotNull(
        this?.youtube.toLink(PersonLinkType.YouTube, Config::webYouTubeChannelUrl),
        this?.wikipedia.toLink(PersonLinkType.Wikipedia) {
            Config.webWikipediaMediaUrl(it, this?.wikipediaLanguage)
        },
        this?.instagram.toLink(PersonLinkType.Instagram, Config::webInstagramPersonUrl),
        this?.twitter.toLink(PersonLinkType.X, Config::webTwitterPersonUrl),
        this?.tiktok.toLink(PersonLinkType.TikTok, Config::webTikTokPersonUrl),
        this?.facebook.toLink(PersonLinkType.Facebook, Config::webFacebookPersonUrl),
        this?.wikidata.toLink(PersonLinkType.Wikidata, Config::webWikidataUrl),
    )

    val inlineCount = when (links.size) {
        INLINE_LINKS_LIMIT + 1 -> links.size
        else -> INLINE_LINKS_LIMIT
    }

    return PersonLinks(
        inline = links.take(inlineCount).toImmutableList(),
        overflow = links.drop(inlineCount).toImmutableList(),
    )
}

private inline fun String?.toLink(
    type: PersonLinkType,
    url: (String) -> String,
): PersonLink? {
    if (isNullOrBlank()) return null
    return PersonLink(type, url(this))
}
