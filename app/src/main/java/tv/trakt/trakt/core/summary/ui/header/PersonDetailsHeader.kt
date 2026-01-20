package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    person: Person,
    loading: Boolean,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    val socialIds = remember(person.socialIds) {
        buildList {
            if (!person.socialIds?.facebook.isNullOrBlank()) add("facebook" to person.socialIds?.facebook)
            if (!person.socialIds?.instagram.isNullOrBlank()) add("instagram" to person.socialIds?.instagram)
            if (!person.socialIds?.twitter.isNullOrBlank()) add("twitter" to person.socialIds?.twitter)
            if (!person.socialIds?.wikipedia.isNullOrBlank()) add("wikipedia" to person.socialIds?.wikipedia)
        }
    }

    DetailsHeader(
        loading = loading,
        title = person.name,
        titleFooter = {
            Text(
                text = person.knownForDepartment
                    ?.replaceFirstChar { it.uppercaseChar() }
                    ?: "-",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
            )
        },
        extraRightColumn = {
            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(18.dp),
                modifier = Modifier
                    .padding(top = 17.dp)
                    .fillMaxWidth(),
            ) {
                socialIds.forEach { (social, id) ->
                    if (id != null) {
                        Icon(
                            painter = painterResource(
                                when (social) {
                                    "facebook" -> R.drawable.ic_facebook
                                    "instagram" -> R.drawable.ic_instagram
                                    "twitter" -> R.drawable.ic_x_twitter
                                    "wikipedia" -> R.drawable.ic_wikipedia
                                    else -> R.drawable.ic_share
                                },
                            ),
                            tint = TraktTheme.colors.textPrimary,
                            contentDescription = null,
                            modifier = Modifier
                                .size(
                                    when {
                                        social == "wikipedia" -> 24.dp
                                        else -> 26.dp
                                    },
                                )
                                .onClick {
                                    when (social) {
                                        "facebook" -> uriHandler.openUri(Config.webFacebookPersonUrl(id))
                                        "instagram" -> uriHandler.openUri(Config.webInstagramPersonUrl(id))
                                        "twitter" -> uriHandler.openUri(Config.webTwitterPersonUrl(id))
                                        "wikipedia" -> uriHandler.openUri(Config.webWikipediaMediaUrl(id))
                                    }
                                },
                        )
                    }
                }
            }
        },
        genres = EmptyImmutableList,
        imageUrl = person.images?.getHeadshotUrl(),
        imageHorizontal = false,
        onShareClick = onShareClick,
        onBackClick = onBackClick,
        date = null,
        status = null,
        runtime = null,
        externalRatings = null,
        externalRatingsVisible = false,
        externalRottenVisible = false,
        episodesCount = null,
        playsCount = null,
        certification = null,
        accentColor = Shade700,
        creditsCount = null,
        traktRatings = null,
        personImdb = person.ids.imdb,
        onImdbClick = {},
        onRottenClick = {},
        modifier = modifier,
    )
}
