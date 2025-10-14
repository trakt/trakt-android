package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.util.Locale
import kotlin.time.Duration

private val EmptyList = emptyList<String>().toImmutableList()

@Composable
internal fun DetailsMetaInfo(
    modifier: Modifier = Modifier,
    show: Show,
    showStudios: ImmutableList<String>? = null,
) {
    DetailsMetaInfo(
        modifier = modifier,
        released = show.released?.toLocalDate(),
        runtime = show.runtime,
        status = show.status,
        languages = show.languages,
        titleOriginal = show.titleOriginal,
        country = show.country,
        genres = show.genres,
        studios = showStudios ?: EmptyList,
    )
}

@Composable
internal fun DetailsMetaInfo(
    modifier: Modifier = Modifier,
    movie: Movie,
    movieStudios: ImmutableList<String>? = null,
) {
    DetailsMetaInfo(
        modifier = modifier,
        released = movie.released,
        runtime = movie.runtime,
        status = movie.status,
        languages = movie.languages,
        titleOriginal = movie.titleOriginal,
        country = movie.country,
        genres = movie.genres,
        studios = movieStudios ?: EmptyList,
    )
}

@Composable
internal fun DetailsMetaInfo(
    modifier: Modifier = Modifier,
    released: LocalDate? = null,
    runtime: Duration? = null,
    status: String? = null,
    country: String? = null,
    titleOriginal: String? = null,
    languages: ImmutableList<String> = EmptyList,
    genres: ImmutableList<String> = EmptyList,
    studios: ImmutableList<String> = EmptyList,
) {
    val runtimeString = remember(runtime) {
        runtime?.inWholeMinutes?.durationFormat()
    }

    val languagesStrings = remember(languages) {
        languages.mapNotNull {
            runCatching {
                Locale.forLanguageTag(it).displayLanguage
            }.getOrNull()
        }.take(5)
    }

    @Suppress("DEPRECATION")
    val countryString = remember(country) {
        country?.let {
            runCatching {
                Locale("", it).displayCountry
            }.getOrNull()
        }
    }

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            DetailsMeta(
                title = stringResource(R.string.header_premiered),
                values = listOf(released?.format(longDateFormat) ?: "N/A"),
                modifier = Modifier.weight(1F),
            )
            DetailsMeta(
                title = stringResource(R.string.header_runtime),
                values = listOf(runtimeString ?: "N/A"),
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            DetailsMeta(
                title = stringResource(R.string.header_status),
                values = listOf(status ?: "N/A"),
                modifier = Modifier.weight(1F),
            )
            DetailsMeta(
                title = stringResource(R.string.header_language),
                values = languagesStrings.ifEmpty { listOf("N/A") },
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            DetailsMeta(
                title = stringResource(R.string.header_country),
                values = listOf(countryString ?: "N/A"),
                modifier = Modifier.weight(1F),
            )
            DetailsMeta(
                title = stringResource(R.string.header_original_title),
                values = listOf(titleOriginal ?: "N/A"),
                modifier = Modifier.weight(1F),
            )
        }

        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            DetailsMeta(
                title = stringResource(R.string.header_studio),
                values = studios
                    .take(5)
                    .ifEmpty { listOf("N/A") },
                modifier = Modifier.weight(1F),
            )
            DetailsMeta(
                title = stringResource(R.string.header_genre),
                values = genres
                    .take(5)
                    .ifEmpty { listOf("N/A") },
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
internal fun DetailsMeta(
    title: String,
    values: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(2.dp),
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            text = title.uppercase(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary
                .copy(alpha = 0.7f),
            maxLines = 1,
            overflow = Ellipsis,
        )
        for (value in values) {
            Text(
                text = value.replaceFirstChar {
                    it.titlecase()
                },
                style = TraktTheme.typography.paragraphSmaller,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = Ellipsis,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        DetailsMetaInfo(
            movie = PreviewData.movie1,
        )
    }
}
