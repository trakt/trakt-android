package tv.trakt.trakt.core.summary.ui.views

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W500
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CrewPersonListItem(
    person: CrewPerson,
    onClick: ((CrewPerson) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    PanelMediaCard(
        title = person.person.name,
        titleOriginal = null,
        subtitle = person.jobs.joinToString(", ") { word ->
            word.replaceFirstChar { it.uppercaseChar() }
        },
        subtitleMaxLines = 3,
        contentImageUrl = person.person.images?.getHeadshotUrl(THUMB),
        containerImageUrl = null,
        imageWidth = TraktTheme.size.verticalSmallMediaCardSize,
        more = false,
        footerContent = if (person.episodesCount > 0) {
            {
                Text(
                    text = stringResource(R.string.text_stats_episodes_count, person.episodesCount),
                    style = TraktTheme.typography.cardSubtitle.copy(
                        fontWeight = W500,
                    ),
                    color = TraktTheme.colors.textPrimary,
                )
            }
        } else {
            null
        },
        onClick = { onClick?.invoke(person) },
        modifier = modifier,
    )
}

internal fun CrewPerson.matchesQuery(query: String): Boolean =
    query.isBlank() ||
        person.name.contains(query, ignoreCase = true) ||
        jobs.any { it.contains(query, ignoreCase = true) }

@Preview(widthDp = 350)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(12.dp),
        ) {
            CrewPersonListItem(
                person = CrewPerson(
                    person = PreviewData.person1,
                    jobs = persistentListOf("Director", "Writer"),
                    episodesCount = 4,
                ),
                onClick = null,
            )
            CrewPersonListItem(
                person = CrewPerson(
                    person = PreviewData.person2,
                    jobs = persistentListOf("Director"),
                ),
                onClick = null,
            )
        }
    }
}
