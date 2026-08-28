package tv.trakt.trakt.core.search.views.items

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchShowGridItem(
    item: SearchItem.Show,
    filter: SearchFilter,
    watched: Boolean,
    watching: Boolean,
    watchlist: Boolean,
    plays: Int,
    onShowClick: (Show) -> Unit,
    onShowLongClick: (Show) -> Unit,
    modifier: Modifier,
) {
    var currentFilter by remember { mutableStateOf(filter) }

    LaunchedEffect(filter) {
        if (filter != SearchFilter.PEOPLE) {
            currentFilter = filter
        }
    }

    VerticalMediaCard(
        title = item.show.title,
        imageUrl = item.show.images?.getPosterUrl(),
        watched = watched,
        watching = watching,
        watchlist = watchlist,
        plays = plays,
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = modifier,
            ) {
                if (currentFilter == SearchFilter.MEDIA) {
                    Icon(
                        painter = painterResource(R.drawable.ic_shows_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier
                            .size(13.dp),
                    )
                }

                val airedEpisodes = stringResource(
                    R.string.tag_text_number_of_episodes,
                    item.show.airedEpisodes,
                )

                val footerText = remember {
                    buildString {
                        item.show.releasedAt?.let {
                            append(it.toLocal().year.toString())
                        } ?: append("TBA")

                        if (item.show.airedEpisodes > 0) {
                            append(" • ")
                            append(airedEpisodes)
                        }
                    }
                }

                Text(
                    text = footerText,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = { onShowClick(item.show) },
        onLongClick = { onShowLongClick(item.show) },
        modifier = modifier,
    )
}
