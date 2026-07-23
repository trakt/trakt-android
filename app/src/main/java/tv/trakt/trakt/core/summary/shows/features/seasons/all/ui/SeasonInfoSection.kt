@file:OptIn(ExperimentalFoundationApi::class)
@file:Suppress("FunctionName")

package tv.trakt.trakt.core.summary.shows.features.seasons.all.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.CrewPerson
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsState
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsPeopleMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsPeopleMode.Cast
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsPeopleMode.Crew
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.CastPersonListItem
import tv.trakt.trakt.core.summary.shows.features.seasons.ui.CrewPersonListItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.EmptyListCard
import tv.trakt.trakt.ui.components.EmptyVerticalPanelHeight
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.components.mediacards.skeletons.PanelMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

internal fun LazyListScope.SeasonInfoSection(
    contentPadding: PaddingValues,
    state: AllShowSeasonsState,
    searchState: TextFieldState,
    onPersonClick: (person: Person) -> Unit,
    onModeClick: ((SeasonsPeopleMode) -> Unit),
    onRatingClick: (Int) -> Unit = {},
    onRatingRemoveClick: () -> Unit = {},
) {
    item(
        key = "season_info",
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
                .graphicsLayer {
                    translationY = -2.dp.toPx()
                },
        ) {
            val userRatingVisible = state.seasonUserRating.loading == Done &&
                (state.items.isSelectedSeasonWatched || state.seasonUserRating.rating != null)

            if (userRatingVisible) {
                UserRatingBar(
                    key = state.items.selectedSeason?.ids?.trakt?.value?.toString(),
                    rating = state.seasonUserRating.rating?.rating,
                    favoriteVisible = false,
                    spacing = 5.dp,
                    textSpacing = 38.dp,
                    onRatingClick = onRatingClick,
                    onRatingRemoveClick = onRatingRemoveClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp)
                        .graphicsLayer {
                            translationY = 4.dp.toPx()
                        },
                )
            }

            Column(
                verticalArrangement = spacedBy(12.dp),
            ) {
                Box(
                    contentAlignment = CenterStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 3.dp),
                ) {
                    TraktSectionHeader(
                        title = stringResource(R.string.section_title_seasons_overview),
                        chevron = false,
                        collapsable = false,
                    )
                }

                SeasonRatingView(
                    state = state,
                )

                val overview = state.items.selectedSeason?.overview ?: ""
                var overviewCollapsed by remember { mutableStateOf(true) }
                Text(
                    text = overview
                        .ifBlank { stringResource(R.string.text_overview_placeholder) },
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textSecondary,
                    minLines = 3,
                    maxLines = if (overviewCollapsed) 3 else Int.MAX_VALUE,
                    textAlign = TextAlign.Start,
                    overflow = Ellipsis,
                    modifier = Modifier.onClick {
                        overviewCollapsed = !overviewCollapsed
                    },
                )
            }
        }
    }

    item(
        key = "season_people",
    ) {
        Column(
            verticalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .padding(contentPadding)
                .padding(
                    top = 16.dp,
                    bottom = 12.dp,
                ),
        ) {
            TraktSectionHeader(
                title = stringResource(R.string.drawer_title_people),
                chevron = false,
                collapsable = false,
            )
            FilterChipGroup(
                paddingVertical = PaddingValues(bottom = 1.dp),
            ) {
                for (mode in SeasonsPeopleMode.entries) {
                    FilterChip(
                        selected = state.peopleMode == mode,
                        text = stringResource(mode.displayRes),
                        leadingContent = {
                            Icon(
                                painter = painterResource(mode.iconRes),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(19.dp),
                            )
                        },
                        onClick = {
                            onModeClick(mode)
                        },
                    )
                }
            }

            val hasPeople = when (state.peopleMode) {
                Cast -> state.items.selectedSeasonCast.isNotEmpty()
                Crew -> state.items.selectedSeasonCrew.isNotEmpty()
            }
            if (hasPeople) {
                InputField(
                    state = searchState,
                    border = 1.dp,
                    icon = painterResource(R.drawable.ic_search_off),
                    placeholder = stringResource(R.string.input_placeholder_search_credit_members),
                    endSlot = {
                        if (searchState.text.isNotBlank()) {
                            Icon(
                                painter = painterResource(R.drawable.ic_close),
                                contentDescription = null,
                                tint = TraktTheme.colors.textSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .onClick {
                                        searchState.clearText()
                                    },
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (state.items.isSeasonPeopleLoading) {
        items(count = 6) {
            PanelMediaSkeletonCard(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(bottom = 12.dp)
                    .animateItem(
                        fadeInSpec = null,
                        fadeOutSpec = null,
                    ),
            )
        }
    } else {
        @Composable
        fun EmptyView(modifier: Modifier = Modifier) {
            EmptyListCard(
                modifier = modifier
                    .height(EmptyVerticalPanelHeight)
                    .padding(contentPadding)
                    .padding(bottom = 12.dp),
            )
        }

        val query = searchState.text.toString().trim()
        when (state.peopleMode) {
            Cast -> {
                val cast = state.items.selectedSeasonCast.filter { it.matchesQuery(query) }
                if (cast.isNotEmpty()) {
                    items(
                        items = cast,
                        key = { it.person.ids.trakt.value },
                    ) { item ->
                        CastPersonListItem(
                            person = item,
                            onClick = { onPersonClick(it.person) },
                            modifier = Modifier
                                .padding(contentPadding)
                                .padding(bottom = 12.dp)
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                } else {
                    item {
                        EmptyView(
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                }
            }
            Crew -> {
                val crew = state.items.selectedSeasonCrew.filter { it.matchesQuery(query) }
                if (crew.isNotEmpty()) {
                    items(
                        items = crew,
                        key = { it.person.ids.trakt.value },
                    ) { item ->
                        CrewPersonListItem(
                            person = item,
                            onClick = { onPersonClick(it.person) },
                            modifier = Modifier
                                .padding(contentPadding)
                                .padding(bottom = 12.dp)
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                } else {
                    item {
                        EmptyView(
                            modifier = Modifier
                                .animateItem(
                                    fadeInSpec = null,
                                    fadeOutSpec = null,
                                ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SeasonRatingView(state: AllShowSeasonsState) {
    Row(
        horizontalArrangement = spacedBy(3.dp, Alignment.Start),
        verticalAlignment = CenterVertically,
    ) {
        val traktRating = state.items.selectedSeason?.rating?.ratingPercent ?: 0
        val textStyle = TraktTheme.typography.cardTitle.copy(fontSize = 12.sp)

        Icon(
            painter = painterResource(R.drawable.ic_star_trakt_on),
            contentDescription = null,
            modifier = Modifier.height(16.dp),
            tint = when {
                traktRating > 0 -> Purple400
                else -> TraktTheme.colors.textSecondary
            },
        )

        Box {
            Text(
                text = when {
                    traktRating > 0 -> "$traktRating%"
                    else -> "—"
                },
                color = when {
                    traktRating > 0 -> TraktTheme.colors.textPrimary
                    else -> TraktTheme.colors.textSecondary
                },
                style = textStyle,
            )
            Text(
                text = "00",
                color = Color.Transparent,
                style = textStyle,
            )
        }
    }
}

private fun CastPerson.matchesQuery(query: String): Boolean =
    query.isBlank() ||
        person.name.contains(query, ignoreCase = true) ||
        characters.any { it.contains(query, ignoreCase = true) }

private fun CrewPerson.matchesQuery(query: String): Boolean =
    query.isBlank() ||
        person.name.contains(query, ignoreCase = true) ||
        jobs.any { it.contains(query, ignoreCase = true) }

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun PreviewSeasonInfoSection() {
    TraktTheme {
        val contentPadding = PaddingValues(
            horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
        )
        val cast = listOf(
            CastPerson(person = PreviewData.person1, characters = listOf("Walter White")),
            CastPerson(
                person = PreviewData.person2.copy(name = "Jane Roe"),
                characters = listOf("Skyler White"),
            ),
        ).toImmutableList()
        val searchState = rememberTextFieldState()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.backgroundPrimary),
        ) {
            SeasonInfoSection(
                contentPadding = contentPadding,
                state = AllShowSeasonsState(
                    show = PreviewData.show1,
                    peopleMode = Cast,
                    items = ShowSeasons(
                        selectedSeason = PreviewData.season1.copy(number = 1),
                        selectedSeasonCast = cast,
                    ),
                ),
                searchState = searchState,
                onPersonClick = {},
                onModeClick = {},
            )
        }
    }
}
