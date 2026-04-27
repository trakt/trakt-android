package tv.trakt.trakt.core.profile.sections.progress.all.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem
import tv.trakt.trakt.core.shows.ui.ShowMetaFooter
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllProgressShowView(
    item: ProfileProgressItem.ShowItem,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val genresText = remember(item.show.genres) {
        item.show.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        modifier = modifier,
        enabled = enabled,
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.show.images?.getFanartUrl(Images.Size.THUMB),
        more = false,
        onClick = onClick,
        onImageClick = onClick,
        footerContent = {
            when {
                item.hiddenAt != null -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_drop),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = item.hiddenAt.toLocal().format(mediumDateFormat),
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontWeight = W500,
                            ),
                        )
                    }
                }

                item.remainingEpisodes != null && item.remainingEpisodes > 0 -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_hourglass),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.tag_text_remaining_episodes, item.remainingEpisodes),
                            color = TraktTheme.colors.textPrimary,
                            style = TraktTheme.typography.cardSubtitle.copy(
                                fontWeight = W500,
                            ),
                        )
                    }
                }

                else -> {
                    ShowMetaFooter(
                        show = item.show,
                        mediaIcon = true,
                    )
                }
            }
        },
    )
}
