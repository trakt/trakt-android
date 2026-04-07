package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.durationFormat
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.util.Locale
import kotlin.time.Duration

@Composable
internal fun DetailsMetaInfo(
    show: Show,
    modifier: Modifier = Modifier,
    showStudios: ImmutableList<String>? = null,
    showDirectors: ImmutableList<Person>? = null,
    showWriters: ImmutableList<Person>? = null,
    onPersonClick: (person: Person) -> Unit = {},
) {
    DetailsMetaInfo(
        modifier = modifier,
        released = remember(show.released) {
            show.released?.toLocal()?.toLocalDate()
        },
        runtime = show.runtime,
        totalRuntime = show.totalRuntime,
        status = show.status,
        languages = show.languages,
        titleOriginal = show.titleOriginal,
        country = show.country,
        genres = show.genres,
        network = show.network,
        studios = showStudios ?: EmptyImmutableList,
        directors = showDirectors,
        writers = showWriters,
        episodesCount = show.airedEpisodes,
        onPersonClick = onPersonClick,
    )
}

@Composable
internal fun DetailsMetaInfo(
    episode: Episode,
    modifier: Modifier = Modifier,
    episodeDirectors: ImmutableList<Person>? = null,
    episodeWriters: ImmutableList<Person>? = null,
    onPersonClick: (person: Person) -> Unit = {},
) {
    DetailsMetaInfo(
        modifier = modifier,
        released = remember(episode.firstAired) {
            episode.firstAired?.toLocal()?.toLocalDate()
        },
        runtime = episode.runtime,
        directors = episodeDirectors,
        writers = episodeWriters,
        episodeRowsOnly = true,
        onPersonClick = onPersonClick,
    )
}

@Composable
internal fun DetailsMetaInfo(
    movie: Movie,
    modifier: Modifier = Modifier,
    movieStudios: ImmutableList<String>? = null,
    movieDirectors: ImmutableList<Person>? = null,
    movieWriters: ImmutableList<Person>? = null,
    onPersonClick: (person: Person) -> Unit = {},
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
        studios = movieStudios,
        directors = movieDirectors,
        writers = movieWriters,
        onPersonClick = onPersonClick,
    )
}

@Composable
private fun DetailsMetaInfo(
    modifier: Modifier = Modifier,
    released: LocalDate? = null,
    runtime: Duration? = null,
    totalRuntime: Duration? = null,
    status: String? = null,
    country: String? = null,
    network: String? = null,
    titleOriginal: String? = null,
    episodesCount: Int? = null,
    languages: ImmutableList<String> = EmptyImmutableList,
    genres: ImmutableList<String> = EmptyImmutableList,
    studios: ImmutableList<String>? = null,
    directors: ImmutableList<Person>? = null,
    writers: ImmutableList<Person>? = null,
    episodeRowsOnly: Boolean = false,
    onPersonClick: (person: Person) -> Unit = {},
) {
    val runtimeString = remember(runtime) {
        runtime?.inWholeMinutes?.durationFormat()
    }

    val totalRuntimeString = remember(totalRuntime) {
        totalRuntime?.inWholeMinutes?.durationFormat() ?: "N/A"
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
        verticalArrangement = spacedBy(18.dp),
        modifier = modifier,
    ) {
        if (!network.isNullOrBlank() && totalRuntime != null) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
            ) {
                DetailsMeta(
                    title = stringResource(R.string.header_premiered),
                    values = listOf(released?.format(longDateFormat) ?: "N/A"),
                    modifier = Modifier.weight(1F),
                )
                DetailsMeta(
                    title = stringResource(R.string.header_network),
                    values = listOf(network ?: "N/A"),
                    modifier = Modifier.weight(1F),
                )
            }

            Row(
                horizontalArrangement = spacedBy(16.dp),
            ) {
                DetailsMeta(
                    title = stringResource(R.string.header_runtime),
                    values = listOf(runtimeString ?: "N/A"),
                    modifier = Modifier.weight(1F),
                )

                val episodesCount = stringResource(R.string.tag_text_number_of_episodes, episodesCount ?: 0)
                DetailsMeta(
                    title = stringResource(R.string.header_total_runtime),
                    values = listOf("$totalRuntimeString ($episodesCount)"),
                    modifier = Modifier.weight(1F),
                )
            }
        } else {
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
        }

        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            DetailsMeta(
                title = stringResource(R.string.text_directors),
                values = (directors ?: EmptyImmutableList)
                    .map { it.name }
                    .ifEmpty { listOf("N/A") },
                loading = directors == null,
                onValueClick = { name ->
                    (directors ?: EmptyImmutableList)
                        .firstOrNull { it.name == name }
                        ?.let(onPersonClick)
                },
                modifier = Modifier.weight(1F),
            )
            DetailsMeta(
                title = stringResource(R.string.text_writers),
                values = (writers ?: EmptyImmutableList)
                    .map { it.name }
                    .ifEmpty { listOf("N/A") },
                loading = writers == null,
                onValueClick = { name ->
                    (writers ?: EmptyImmutableList)
                        .firstOrNull { it.name == name }
                        ?.let(onPersonClick)
                },
                modifier = Modifier.weight(1F),
            )
        }

        if (!episodeRowsOnly) {
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
        }

        if (!episodeRowsOnly) {
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
        }

        if (!episodeRowsOnly) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
            ) {
                DetailsMeta(
                    loading = studios == null,
                    title = stringResource(R.string.header_studio),
                    values = studios
                        ?.take(5)
                        ?.ifEmpty { listOf("N/A") } ?: EmptyImmutableList,
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
}

@Composable
private fun DetailsMeta(
    title: String,
    values: List<String>,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    onValueClick: (value: String) -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(2.dp),
        modifier = modifier,
    ) {
        Text(
            text = title.uppercase(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = Ellipsis,
        )
        if (loading) {
            FilmProgressIndicator(
                size = 12.dp,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(vertical = 1.dp),
            )
        } else {
            for (value in values) {
                Text(
                    text = value.replaceFirstChar {
                        it.titlecase()
                    },
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = Ellipsis,
                    modifier = Modifier.onClick {
                        onValueClick(value)
                    },
                )
            }
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
