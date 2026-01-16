package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import tv.trakt.trakt.common.Config.webImdbMediaUrl
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openExternalAppLink
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    episode: Episode,
    show: Show,
    ratings: ExternalRating?,
    creator: Person?,
    playsCount: Int?,
    loading: Boolean,
    onShowClick: (Show) -> Unit,
    onCreatorClick: (Person) -> Unit,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val isReleased = remember {
        episode.firstAired?.isNowOrBefore() ?: false
    }

    DetailsHeader(
        title = episode.title,
        titleHeader = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                modifier = Modifier.padding(
                    top = 4.dp,
                    bottom = 3.dp,
                ),
            ) {
                val font = TraktTheme.typography.heading6.copy(fontSize = 13.sp)
                Text(
                    text = show.title.uppercase(),
                    color = TraktTheme.colors.textPrimary,
                    style = font.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = font.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                    modifier = Modifier
                        .onClick(onClick = { onShowClick(show) }),
                )

                Text(
                    text = "/",
                    color = TraktTheme.colors.textSecondary,
                    style = font.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = font.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                )

                Text(
                    text = episode.seasonEpisode.toDisplayString(),
                    color = TraktTheme.colors.textPrimary,
                    style = font.copy(fontSize = 13.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    autoSize = TextAutoSize.StepBased(
                        maxFontSize = font.fontSize,
                        minFontSize = 10.sp,
                        stepSize = 1.sp,
                    ),
                )
            }
        },
        titleFooter = {
            val animatedAlpha by animateFloatAsState(
                targetValue = if (creator == null) 0f else 1f,
                animationSpec = tween(delayMillis = 50),
                label = "alpha",
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .alpha(animatedAlpha),
            ) {
                Text(
                    text = "${stringResource(R.string.text_directed_by_short)} ",
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.paragraphSmaller,
                )

                Text(
                    text = (creator?.name ?: "").ifBlank { "-" },
                    color = when {
                        creator?.name?.isNotBlank() == true -> TraktTheme.colors.textPrimary
                        else -> TraktTheme.colors.textSecondary
                    },
                    style = TraktTheme.typography.paragraphSmaller,
                    modifier = Modifier
                        .onClick(
                            enabled = (creator != Person.Unknown),
                        ) {
                            creator?.let {
                                onCreatorClick(it)
                            }
                        },
                )
            }
        },
        status = show.status,
        certification = show.certification,
        date = {
            Text(
                text = when {
                    isReleased -> (episode.firstAired?.year ?: show.year).toString()
                    else -> episode.firstAired?.format(mediumDateFormat) ?: show.year.toString()
                },
                color = when {
                    isReleased -> TraktTheme.colors.textSecondary
                    else -> TraktTheme.colors.textPrimary
                },
                style = when {
                    isReleased -> TraktTheme.typography.paragraphSmaller
                    else -> TraktTheme.typography.paragraphSmaller.copy(fontWeight = FontWeight.W700)
                },
                maxLines = 1,
                modifier = Modifier.padding(
                    end = if (!isReleased) 1.dp else 0.dp,
                ),
            )
        },
        genres = show.genres,
        runtime = episode.runtime,
        imageUrl = episode.images?.getScreenshotUrl()
            ?: show.images?.getFanartUrl(),
        imageHorizontal = true,
        accentColor = null,
        traktRatings = when {
            isReleased -> episode.rating.ratingPercent
            else -> null
        },
        externalRatings = ratings,
        externalRatingsVisible = true,
        externalRottenVisible = false,
        episodesCount = null,
        playsCount = playsCount,
        creditsCount = 0,
        loading = loading,
        onBackClick = onBackClick,
        onShareClick = onShareClick,
        onImdbClick = {
            val uri = when {
                episode.ids.imdb != null -> webImdbMediaUrl(episode.ids.imdb!!.value)
                show.ids.imdb != null -> webImdbMediaUrl(show.ids.imdb!!.value)
                else -> return@DetailsHeader
            }
            openExternalAppLink(
                context = context,
                packageId = "com.imdb.mobile",
                packageName = "imdb",
                uri = uri.toUri(),
            )
        },
        onRottenClick = {},
        modifier = modifier,
    )
}
