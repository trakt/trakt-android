package tv.trakt.trakt.core.summary.shows.features.seasons.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CastPersonListItem(
    person: CastPerson,
    onClick: ((CastPerson) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    PanelMediaCard(
        title = person.person.name,
        titleOriginal = null,
        subtitle = person.characters.joinToString(" / ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        },
        subtitleMaxLines = 3,
        contentImageUrl = person.person.images?.getHeadshotUrl(),
        containerImageUrl = null,
        more = false,
        footerContent = {
            Text(
                text = stringResource(R.string.text_stats_episodes_count, person.episodesCount),
                style = TraktTheme.typography.cardSubtitle.copy(
                    fontWeight = W500,
                ),
                color = TraktTheme.colors.textPrimary,
            )
        },
        onClick = { onClick?.invoke(person) },
        modifier = modifier,
    )
}
