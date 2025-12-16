package tv.trakt.trakt.core.profile.sections.library.all.views

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun AllLibraryEpisodeView(
    item: LibraryItem.EpisodeItem,
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
        title = item.show.title,
        titleOriginal = item.show.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.show.images?.getPosterUrl(),
        containerImageUrl = item.show.images?.getFanartUrl(Images.Size.THUMB),
        more = false,
        onClick = onClick,
        footerContent = {
            Text(
                text = item.collectedAt.toLocal().format(mediumDateFormat),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.cardSubtitle.copy(
                    fontWeight = W500,
                ),
            )
        },
    )
}
