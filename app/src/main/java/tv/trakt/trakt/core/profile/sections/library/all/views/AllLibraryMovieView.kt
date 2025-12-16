package tv.trakt.trakt.core.profile.sections.library.all.views

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
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllLibraryMovieView(
    item: LibraryItem.MovieItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val genresText = remember(item.movie.genres) {
        item.movie.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.movie.images?.getFanartUrl(Images.Size.THUMB),
        more = false,
        onClick = onClick,
        footerContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_library_check),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = item.collectedAt.toLocal().format(mediumDateFormat),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.cardSubtitle.copy(
                        fontWeight = W500,
                    ),
                )
            }
        },
    )
}
