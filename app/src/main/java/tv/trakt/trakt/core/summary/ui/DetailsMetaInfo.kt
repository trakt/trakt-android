package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.isTodayOrBefore
import tv.trakt.trakt.common.helpers.extensions.longDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.EpisodeType
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaStatus
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.model.PersonCreditsRole
import tv.trakt.trakt.core.summary.people.model.crewJobStringRes
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TextLineLoadingIndicator
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.LocalDate
import java.util.Locale
import kotlin.time.Duration

private const val COLLAPSED_VALUES_COUNT = 2
private val VALUES_SPACING = 2.dp

@Composable
internal fun DetailsMetaInfo(
    show: Show,
    modifier: Modifier = Modifier,
    showStudios: ImmutableList<String>? = null,
    showCreators: ImmutableList<CrewPerson>? = null,
    showWriters: ImmutableList<CrewPerson>? = null,
    onPersonClick: (person: Person, role: PersonCreditsRole) -> Unit = { _, _ -> },
) {
    DetailsMetaInfo(
        modifier = modifier,
        released = remember(show.releasedAt) {
            show.releasedAt?.toLocal()?.toLocalDate()
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
        creators = showCreators,
        writers = showWriters,
        episodesCount = show.airedEpisodes,
        onPersonClick = onPersonClick,
    )
}

@Composable
internal fun DetailsMetaInfo(
    episode: Episode,
    modifier: Modifier = Modifier,
    episodeDirectors: ImmutableList<CrewPerson>? = null,
    episodeWriters: ImmutableList<CrewPerson>? = null,
    onPersonClick: (person: Person, role: PersonCreditsRole) -> Unit = { _, _ -> },
) {
    DetailsMetaInfo(
        modifier = modifier,
        released = remember(episode.releasedAt) {
            episode.releasedAt?.toLocal()?.toLocalDate()
        },
        runtime = episode.runtime,
        episodeType = episode.type,
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
    movieDirectors: ImmutableList<CrewPerson>? = null,
    movieWriters: ImmutableList<CrewPerson>? = null,
    onPersonClick: (person: Person, role: PersonCreditsRole) -> Unit = { _, _ -> },
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
    status: MediaStatus? = null,
    country: String? = null,
    network: String? = null,
    titleOriginal: String? = null,
    episodesCount: Int? = null,
    episodeType: EpisodeType? = null,
    languages: ImmutableList<String> = EmptyImmutableList,
    genres: ImmutableList<MediaGenre> = EmptyImmutableList,
    studios: ImmutableList<String>? = null,
    creators: ImmutableList<CrewPerson>? = null,
    directors: ImmutableList<CrewPerson>? = null,
    writers: ImmutableList<CrewPerson>? = null,
    episodeRowsOnly: Boolean = false,
    onPersonClick: (person: Person, role: PersonCreditsRole) -> Unit = { _, _ -> },
) {
    val upcoming = remember(released) { released?.isTodayOrBefore() != true }
    val releasedTitle = stringResource(
        when {
            episodeRowsOnly && upcoming -> R.string.header_airs
            episodeRowsOnly -> R.string.header_aired
            upcoming -> R.string.header_expected_premiere
            else -> R.string.header_premiered
        },
    )
    val releasedValue = released?.format(longDateFormat()) ?: stringResource(R.string.tag_text_tba)

    val runtimeString = rememberDurationFormat(runtime?.inWholeMinutes)
    val totalRuntimeString = rememberDurationFormat(totalRuntime?.inWholeMinutes)

    val languagesStrings = remember(languages) {
        languages.mapNotNull {
            runCatching {
                Locale.forLanguageTag(it).displayLanguage
            }.getOrNull()
        }
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
                    title = releasedTitle,
                    values = listOf(releasedValue),
                    modifier = Modifier.weight(1F),
                )
                DetailsMeta(
                    title = stringResource(R.string.header_network),
                    values = listOf(network),
                    modifier = Modifier.weight(1F),
                )
            }

            Row(
                horizontalArrangement = spacedBy(16.dp),
            ) {
                DetailsMeta(
                    title = stringResource(R.string.header_runtime),
                    values = listOf(runtimeString),
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
                    title = releasedTitle,
                    values = listOf(releasedValue),
                    modifier = Modifier.weight(1F),
                )
                DetailsMeta(
                    title = stringResource(R.string.header_runtime),
                    values = listOf(runtimeString),
                    modifier = Modifier.weight(1F),
                )
            }
        }

        if (episodeType != null) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
            ) {
                DetailsMeta(
                    title = stringResource(R.string.header_episode_type),
                    values = listOf(stringResource(episodeType.stringRes)),
                    modifier = Modifier.weight(1F),
                )
                Box(modifier = Modifier.weight(1F))
            }
        }

        Row(
            horizontalArrangement = spacedBy(16.dp),
        ) {
            val people = remember(creators, directors) {
                when {
                    creators != null -> creators
                    directors != null -> directors
                    else -> EmptyImmutableList
                }
            }
            val peopleRole = when {
                directors != null -> PersonCreditsRole.Directing
                else -> PersonCreditsRole.CreatedBy
            }

            DetailsMeta(
                title = stringResource(
                    when {
                        directors != null -> R.string.header_director
                        else -> R.string.header_creator
                    },
                ),
                values = people
                    .map { crewLabel(it) }
                    .ifEmpty { listOf("N/A") },
                loading = creators == null && directors == null,
                reservedLines = COLLAPSED_VALUES_COUNT,
                onValueClick = { index ->
                    people
                        .getOrNull(index)
                        ?.let { onPersonClick(it.person, peopleRole) }
                },
                modifier = Modifier.weight(1F),
            )

            val writersList = writers ?: EmptyImmutableList
            DetailsMeta(
                title = stringResource(R.string.header_writer),
                values = writersList
                    .map { crewLabel(it) }
                    .ifEmpty { listOf("N/A") },
                loading = writers == null,
                reservedLines = COLLAPSED_VALUES_COUNT,
                onValueClick = { index ->
                    writersList
                        .getOrNull(index)
                        ?.let { onPersonClick(it.person, PersonCreditsRole.Writing) }
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
                    values = listOf(
                        status?.let { stringResource(it.displayStringRes) } ?: "N/A",
                    ),
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
                        ?.ifEmpty { listOf("N/A") } ?: EmptyImmutableList,
                    modifier = Modifier.weight(1F),
                )
                DetailsMeta(
                    title = stringResource(R.string.header_genre),
                    values = genres
                        .map { stringResource(it.displayStringRes) }
                        .ifEmpty { listOf("N/A") },
                    modifier = Modifier.weight(1F),
                )
            }
        }
    }
}

@Composable
private fun crewLabel(crew: CrewPerson): String {
    if (crew.jobs.isEmpty()) return crew.person.name

    val jobs = crew.jobs
        .map { job -> crewJobStringRes(job)?.let { stringResource(it) } ?: job }
        .joinToString(", ")
    return "${crew.person.name} ($jobs)"
}

@Composable
private fun DetailsMeta(
    title: String,
    values: List<String>,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    reservedLines: Int = 1,
    onValueClick: ((index: Int) -> Unit)? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val hiddenCount = values.size - COLLAPSED_VALUES_COUNT
    val collapsed = !loading && !expanded && hiddenCount > 0
    val visibleValues = when {
        collapsed -> values.take(COLLAPSED_VALUES_COUNT)
        else -> values
    }

    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = spacedBy(2.dp),
        modifier = modifier
            .onClick(enabled = collapsed) {
                expanded = true
            },
    ) {
        Text(
            text = title.uppercase(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier.padding(bottom = 1.dp),
        )

        Box {
            // Reserve value lines so rows keep their height while values load.
            Column(
                verticalArrangement = spacedBy(VALUES_SPACING),
            ) {
                repeat(reservedLines) {
                    Text(
                        text = "",
                        style = TraktTheme.typography.paragraphSmaller,
                        maxLines = 1,
                    )
                }
            }

            Column(
                verticalArrangement = spacedBy(VALUES_SPACING),
            ) {
                if (loading) {
                    TextLineLoadingIndicator(
                        style = TraktTheme.typography.paragraphSmaller,
                        color = TraktTheme.colors.textSecondary,
                    )
                } else {
                    for ((index, value) in visibleValues.withIndex()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(6.dp),
                        ) {
                            Text(
                                text = value.replaceFirstChar {
                                    it.titlecase()
                                },
                                style = TraktTheme.typography.paragraphSmaller,
                                color = TraktTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = Ellipsis,
                                modifier = Modifier
                                    .weight(1F, fill = false)
                                    .then(
                                        when (onValueClick) {
                                            null -> Modifier
                                            else -> Modifier.onClick { onValueClick(index) }
                                        },
                                    ),
                            )

                            if (collapsed && index == visibleValues.lastIndex) {
                                Text(
                                    text = "+${stringResource(R.string.button_text_more, hiddenCount)}",
                                    style = TraktTheme.typography.meta,
                                    color = TraktTheme.colors.textSecondary,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
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

@Preview
@Composable
private fun PreviewUpcoming() {
    TraktTheme {
        DetailsMetaInfo(
            movie = PreviewData.movie1.copy(
                released = LocalDate.now().plusMonths(2),
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewTba() {
    TraktTheme {
        DetailsMetaInfo(
            movie = PreviewData.movie1.copy(
                released = null,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewCrew() {
    TraktTheme {
        DetailsMetaInfo(
            movie = PreviewData.movie1,
            movieDirectors = persistentListOf(
                CrewPerson(
                    person = PreviewData.person1,
                    jobs = persistentListOf("Director"),
                ),
            ),
            movieWriters = persistentListOf(
                CrewPerson(
                    person = PreviewData.person1,
                    jobs = persistentListOf("Screenplay", "Novel"),
                ),
                CrewPerson(
                    person = PreviewData.person1.copy(name = "Unknown Job Writer"),
                    jobs = persistentListOf("Some Untranslated Job"),
                ),
                CrewPerson(
                    person = PreviewData.person1.copy(name = "Collapsed Writer"),
                    jobs = persistentListOf("Story"),
                ),
            ),
        )
    }
}
