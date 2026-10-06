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
import tv.trakt.trakt.common.helpers.extensions.timeFormat
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.EpisodeType
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaStatus
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.PostCreditsScene
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.people.model.PersonCreditsRole
import tv.trakt.trakt.core.summary.people.model.crewJobStringRes
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TextLineLoadingIndicator
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration

private const val COLLAPSED_VALUES_COUNT = 2
private const val EMPTY_VALUE = "-"
private val VALUES_SPACING = 2.dp

@Composable
internal fun DetailsMetaInfo(
    show: Show,
    modifier: Modifier = Modifier,
    showStudios: ImmutableList<String>? = null,
    showNetworks: ImmutableList<String>? = null,
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
        titleOriginal = show.titleOriginal?.takeIf { it.isNotBlank() && it != show.title },
        country = show.country,
        genres = show.genres,
        airs = rememberAirsText(show),
        networks = showNetworks ?: listOfNotNull(show.network),
        studios = showStudios,
        creators = showCreators,
        writers = showWriters,
        episodesCount = show.airedEpisodes,
        layout = DetailsLayout.Show,
        onPersonClick = onPersonClick,
    )
}

@Composable
internal fun DetailsMetaInfo(
    episode: Episode,
    modifier: Modifier = Modifier,
    episodeNetwork: String? = null,
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
        networks = listOfNotNull(episodeNetwork),
        directors = episodeDirectors,
        writers = episodeWriters,
        postCredits = episode.postCredits,
        layout = DetailsLayout.Episode,
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
        // Web shows a movie status only while its release year is unknown.
        status = movie.status.takeIf { movie.year == null },
        languages = movie.languages,
        titleOriginal = movie.titleOriginal?.takeIf { it.isNotBlank() && it != movie.title },
        country = movie.country,
        genres = movie.genres,
        studios = movieStudios,
        directors = movieDirectors,
        writers = movieWriters,
        postCredits = movie.postCredits,
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
    airs: String? = null,
    networks: List<String> = EmptyImmutableList,
    titleOriginal: String? = null,
    episodesCount: Int? = null,
    episodeType: EpisodeType? = null,
    languages: ImmutableList<String> = EmptyImmutableList,
    genres: ImmutableList<MediaGenre> = EmptyImmutableList,
    studios: ImmutableList<String>? = null,
    creators: ImmutableList<CrewPerson>? = null,
    directors: ImmutableList<CrewPerson>? = null,
    writers: ImmutableList<CrewPerson>? = null,
    postCredits: ImmutableList<PostCreditsScene> = EmptyImmutableList,
    layout: DetailsLayout = DetailsLayout.Movie,
    onPersonClick: (person: Person, role: PersonCreditsRole) -> Unit = { _, _ -> },
) {
    val upcoming = remember(released) { released?.isTodayOrBefore() != true }
    val releasedTitle = stringResource(
        when {
            layout == DetailsLayout.Episode && upcoming -> R.string.header_airs
            layout == DetailsLayout.Episode -> R.string.header_aired
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

    val airsCell = MetaCell(stringResource(R.string.header_airs), listOfNotNull(airs))
    val releasedCell = MetaCell(releasedTitle, listOf(releasedValue))
    val runtimeCell = MetaCell(
        title = stringResource(R.string.header_runtime),
        values = listOf(runtime?.let { runtimeString } ?: EMPTY_VALUE),
    )
    val episodesString = stringResource(R.string.tag_text_number_of_episodes, episodesCount ?: 0)
    val totalRuntimeCell = MetaCell(
        title = stringResource(R.string.header_total_runtime),
        values = listOfNotNull(
            totalRuntime
                ?.takeIf { it.isPositive() }
                ?.let { "$totalRuntimeString ($episodesString)" },
        ),
    )
    val networkCell = MetaCell(stringResource(R.string.header_network), networks)
    val episodeTypeCell = MetaCell(
        title = stringResource(R.string.header_episode_type),
        values = listOfNotNull(episodeType?.let { stringResource(it.stringRes) }),
    )
    val statusCell = MetaCell(
        title = stringResource(R.string.header_status),
        values = listOfNotNull(status?.let { stringResource(it.displayStringRes) }),
    )
    val languageCell = MetaCell(stringResource(R.string.header_language), languagesStrings)
    val countryCell = MetaCell(stringResource(R.string.header_country), listOfNotNull(countryString))
    val originalTitleCell = MetaCell(stringResource(R.string.header_original_title), listOfNotNull(titleOriginal))
    val studioCell = MetaCell(
        title = stringResource(R.string.header_studio),
        values = studios ?: EmptyImmutableList,
        loading = studios == null,
    )
    val genreCell = MetaCell(
        title = stringResource(R.string.header_genre),
        values = genres.map { stringResource(it.displayStringRes) },
    )
    val postCreditsCell = MetaCell(
        title = stringResource(R.string.header_post_credits),
        values = postCredits.map { stringResource(it.stringRes) },
    )

    val people = remember(creators, directors) {
        creators ?: directors ?: EmptyImmutableList
    }
    val peopleRole = when {
        directors != null -> PersonCreditsRole.Directing
        else -> PersonCreditsRole.CreatedBy
    }
    val peopleCell = MetaCell(
        title = stringResource(
            when {
                directors != null -> R.string.header_director
                else -> R.string.header_creator
            },
        ),
        values = people.map { crewLabel(it) },
        loading = creators == null && directors == null,
        reservedLines = COLLAPSED_VALUES_COUNT,
        onValueClick = { index ->
            people
                .getOrNull(index)
                ?.let { onPersonClick(it.person, peopleRole) }
        },
    )

    val writersList = writers ?: EmptyImmutableList
    val writerCell = MetaCell(
        title = stringResource(R.string.header_writer),
        values = writersList.map { crewLabel(it) },
        loading = writers == null,
        reservedLines = COLLAPSED_VALUES_COUNT,
        onValueClick = { index ->
            writersList
                .getOrNull(index)
                ?.let { onPersonClick(it.person, PersonCreditsRole.Writing) }
        },
    )

    // Cells flow two per row in the same order as web, skipping empty ones.
    val cells = when (layout) {
        DetailsLayout.Movie -> listOf(
            releasedCell,
            statusCell,
            runtimeCell,
            peopleCell,
            writerCell,
            countryCell,
            languageCell,
            originalTitleCell,
            studioCell,
            genreCell,
            postCreditsCell,
        )
        DetailsLayout.Show -> listOf(
            airsCell,
            releasedCell,
            statusCell,
            runtimeCell,
            totalRuntimeCell,
            networkCell,
            peopleCell,
            writerCell,
            countryCell,
            languageCell,
            originalTitleCell,
            studioCell,
            genreCell,
        )
        DetailsLayout.Episode -> listOf(
            releasedCell,
            networkCell,
            runtimeCell,
            episodeTypeCell,
            peopleCell,
            writerCell,
            postCreditsCell,
        )
    }
    val rows = cells
        .filter { it.loading || it.values.isNotEmpty() }
        .chunked(2)

    Column(
        verticalArrangement = spacedBy(18.dp),
        modifier = modifier,
    ) {
        for (row in rows) {
            Row(
                horizontalArrangement = spacedBy(16.dp),
            ) {
                for (cell in row) {
                    DetailsMeta(
                        title = cell.title,
                        values = cell.values,
                        loading = cell.loading,
                        reservedLines = cell.reservedLines,
                        onValueClick = cell.onValueClick,
                        modifier = Modifier.weight(1F),
                    )
                }
                if (row.size == 1) {
                    Box(modifier = Modifier.weight(1F))
                }
            }
        }
    }
}

@Composable
private fun rememberAirsText(show: Show): String? {
    val formatter = timeFormat()
    val airing = remember(show.airs, formatter) {
        show.airs
            ?.takeIf { show.isAiring }
            ?.toZonedDateTime()
    } ?: return null

    val day = remember(airing, formatter) {
        airing.dayOfWeek.getDisplayName(TextStyle.FULL, formatter.locale)
    }
    return stringResource(R.string.text_airs_day_time, day, airing.format(formatter))
}

private enum class DetailsLayout {
    Movie,
    Show,
    Episode,
}

private data class MetaCell(
    val title: String,
    val values: List<String>,
    val loading: Boolean = false,
    val reservedLines: Int = 1,
    val onValueClick: ((index: Int) -> Unit)? = null,
)

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

@Preview
@Composable
private fun PreviewShowAiring() {
    TraktTheme {
        DetailsMetaInfo(
            show = PreviewData.show1.copy(
                status = MediaStatus.ReturningSeries,
                releasedAt = Instant.parse("2020-01-01T00:00:00Z"),
                airs = Show.Airs(
                    day = "Thursday",
                    time = "21:00",
                    timezone = "America/New_York",
                ),
            ),
            showNetworks = persistentListOf("AMC", "Netflix", "Hulu"),
        )
    }
}

@Preview
@Composable
private fun PreviewMovieExtras() {
    TraktTheme {
        DetailsMetaInfo(
            movie = PreviewData.movie1.copy(
                year = null,
                titleOriginal = "Original Title",
                postCredits = persistentListOf(PostCreditsScene.During, PostCreditsScene.After),
            ),
            movieStudios = persistentListOf(),
            movieDirectors = persistentListOf(),
            movieWriters = persistentListOf(),
        )
    }
}

@Preview
@Composable
private fun PreviewEpisode() {
    TraktTheme {
        DetailsMetaInfo(
            episode = PreviewData.episode1.copy(
                postCredits = persistentListOf(PostCreditsScene.After),
            ),
            episodeNetwork = "AMC",
            episodeDirectors = persistentListOf(),
            episodeWriters = persistentListOf(),
        )
    }
}
