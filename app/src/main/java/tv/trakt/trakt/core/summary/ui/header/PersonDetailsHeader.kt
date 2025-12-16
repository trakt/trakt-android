package tv.trakt.trakt.core.summary.ui.header

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    person: Person,
    loading: Boolean,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier,
    )
}
