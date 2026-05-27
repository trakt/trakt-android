package tv.trakt.trakt.core.filters.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Availability
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade.CurrentYear
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.filters.ColumnsSpacing
import tv.trakt.trakt.core.filters.GlobalFiltersState
import tv.trakt.trakt.core.filters.RowsSpacing
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownMultiView
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeFilters
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

@Composable
internal fun GlobalFiltersAdvancedView(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit = { _, _ -> },
    onResetFilter: () -> Unit = { },
    onToggleMode: (GlobalFilterMode) -> Unit = { },
) {
    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TraktHeader(
                title = stringResource(R.string.header_filters),
                subtitle = stringResource(GlobalFilterMode.Advanced.displayStringRes),
            )

            Row(
                horizontalArrangement = spacedBy(16.dp),
                verticalAlignment = CenterVertically,
            ) {
                if (state.filter.isActive) {
                    Text(
                        text = stringResource(R.string.button_text_reset_all_filters),
                        color = Red400,
                        style = TraktTheme.typography.buttonTertiary,
                        modifier = Modifier.clickable(onClick = onResetFilter),
                    )
                }

                FilterChip(
                    selected = false,
                    text = stringResource(GlobalFilterMode.Simple.displayStringRes),
                    height = 32.dp,
                    leadingAlwaysVisible = true,
                    leadingContent = {
                        Icon(
                            painter = painterResource(R.drawable.ic_filters_simple),
                            contentDescription = null,
                            tint = TraktTheme.colors.textPrimary,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    onClick = {
                        onToggleMode(GlobalFilterMode.Simple)
                    },
                )
            }
        }

        MediaModeFilters(
            selected = state.filter.mode,
            onClick = {
                onUpdateFilter(state.filter.copy(mode = it), false)
            },
            height = 32.dp,
            modifier = Modifier.padding(
                top = 4.dp,
                bottom = 5.dp,
            ),
        )

        Column(
            verticalArrangement = spacedBy(ColumnsSpacing),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                GenreFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )

                AvailabilityFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CertificationFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )

                RegionFilter(
                    state = state,
                    onUpdateFilter = onUpdateFilter,
                    modifier = Modifier.weight(1f),
                )
            }

            YearsFilter(
                state = state,
                onUpdateFilter = onUpdateFilter,
            )

            RuntimeFilter(
                state = state,
                onUpdateFilter = onUpdateFilter,
            )

            RatingFilter(
                state = state,
                onUpdateFilter = onUpdateFilter,
            )

            SwitchesFilter(
                state = state,
                onUpdateFilter = onUpdateFilter,
            )
        }
    }
}

@Composable
private fun GenreFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val genresValues: List<DropdownOption<MediaGenre?>> =
        when (val genres = state.filter.genre) {
            null -> listOf(
                DropdownOption(
                    raw = null,
                    displayString = stringResource(R.string.option_text_all),
                ),
            )
            else -> genres.map {
                DropdownOption(
                    raw = it,
                    displayString = stringResource(it.displayStringRes),
                )
            }
        }

    val genresOptions = buildList<DropdownOption<MediaGenre?>> {
        add(DropdownOption(null, stringResource(R.string.option_text_all)))
        addAll(
            MediaGenre.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownMultiView(
        header = stringResource(R.string.header_genre),
        active = !state.filter.genre.isNullOrEmpty(),
        values = remember(state.filter.genre) {
            genresValues.toImmutableList()
        },
        options = genresOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filter.copy(
                    genre = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun AvailabilityFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val availabilityValues: List<DropdownOption<Availability?>> =
        when (val availability = state.filter.availability) {
            null -> listOf(
                DropdownOption(
                    raw = null,
                    displayString = stringResource(R.string.option_text_all),
                ),
            )
            else -> availability.map {
                DropdownOption(
                    raw = it,
                    displayString = stringResource(it.displayStringRes),
                )
            }
        }

    val availabilityOptions = buildList<DropdownOption<Availability?>> {
        add(DropdownOption(null, stringResource(R.string.option_text_all)))
        addAll(
            Availability.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownMultiView(
        header = stringResource(R.string.header_streaming),
        active = !state.filter.availability.isNullOrEmpty(),
        values = remember(state.filter.availability) {
            availabilityValues.toImmutableList()
        },
        options = availabilityOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filter.copy(
                    availability = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun CertificationFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val certificationValues: List<DropdownOption<Certification?>> =
        when (val certification = state.filter.certification) {
            null -> listOf(
                DropdownOption(
                    raw = null,
                    displayString = stringResource(R.string.option_text_all),
                ),
            )
            else -> certification.map {
                DropdownOption(
                    raw = it,
                    displayString = stringResource(it.displayStringRes),
                )
            }
        }

    val certificationOptions = buildList<DropdownOption<Certification?>> {
        add(DropdownOption(null, stringResource(R.string.option_text_all)))
        addAll(
            Certification.entries
                .map {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                },
        )
    }.toImmutableList()

    DropdownMultiView(
        header = stringResource(R.string.header_certification),
        active = !state.filter.certification.isNullOrEmpty(),
        values = remember(state.filter.certification) {
            certificationValues.toImmutableList()
        },
        options = certificationOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filter.copy(
                    certification = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun RegionFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val regionValues: List<DropdownOption<String?>> =
        when (val countries = state.filter.countries) {
            null -> listOf(
                DropdownOption(
                    raw = null,
                    displayString = stringResource(R.string.option_text_all),
                ),
            )
            else -> countries.map { code ->
                DropdownOption(
                    raw = code,
                    displayString = Locale("", code).displayCountry,
                )
            }
        }

    val regionOptions = buildList<DropdownOption<String?>> {
        add(DropdownOption(null, stringResource(R.string.option_text_all)))
        addAll(
            Region.AllLocales.map { locale ->
                DropdownOption(
                    raw = locale.language,
                    displayString = Locale("", locale.language).displayCountry,
                )
            },
        )
    }.toImmutableList()

    DropdownMultiView(
        header = stringResource(R.string.header_country),
        active = !state.filter.countries.isNullOrEmpty(),
        values = remember(state.filter.countries) {
            regionValues.toImmutableList()
        },
        options = regionOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filter.copy(
                    countries = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        modifier = modifier,
    )
}

@Composable
private fun YearsFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeYearsValue = remember(state.filter.years) {
            mutableStateOf(
                state.filter.years?.let {
                    it.first.toFloat()..it.second.toFloat()
                } ?: (1930f..2040f),
            )
        }

        Text(
            text = stringResource(
                R.string.advanced_filter_label_release_year,
                rangeYearsValue.value.start.toInt(),
                rangeYearsValue.value.endInclusive.toInt(),
            ),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        RangeSlider(
            value = rangeYearsValue.value,
            valueRange = 1930f..2040f,
            steps = 109, // Step every 1 year
            onValueChange = { range ->
                rangeYearsValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeYearsValue.value
                val start = value.start.toInt()
                val end = value.endInclusive.toInt()
                onUpdateFilter(
                    state.filter.copy(
                        years = if (start == 1930 && end == 2040) null else start to end,
                    ),
                    true,
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = TraktTheme.colors.accent,
                inactiveTrackColor = Color.White,
                thumbColor = TraktTheme.colors.accent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )

        Box(
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (i in 1930..2040 step 110) {
                    Text(
                        text = "$i",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 46.dp),
            ) {
                for (i in 1950..2010 step 20) {
                    Text(
                        text = "$i",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RuntimeFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeRuntimeValue = remember(state.filter.runtime) {
            mutableStateOf(
                state.filter.runtime?.let {
                    it.first.toFloat()..it.second.toFloat()
                } ?: (0f..500f),
            )
        }

        Text(
            text = stringResource(
                R.string.advanced_filter_label_runtime,
                rangeRuntimeValue.value.start.toInt(),
                rangeRuntimeValue.value.endInclusive.toInt(),
            ),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        RangeSlider(
            value = rangeRuntimeValue.value,
            valueRange = 0f..500f,
            steps = 499, // Step every 1 min
            onValueChange = { range ->
                rangeRuntimeValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeRuntimeValue.value
                val start = value.start.toInt()
                val end = value.endInclusive.toInt()
                onUpdateFilter(
                    state.filter.copy(
                        runtime = if (start == 0 && end == 500) null else start to end,
                    ),
                    true,
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = TraktTheme.colors.accent,
                inactiveTrackColor = Color.White,
                thumbColor = TraktTheme.colors.accent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )

        Box(
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (i in 0..500 step 500) {
                    Text(
                        text = "${i}m",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 46.dp),
            ) {
                for (i in 100..400 step 100) {
                    Text(
                        text = "${i}m",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun RatingFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeRatingValue = remember(state.filter.rating) {
            mutableStateOf(
                state.filter.rating?.let {
                    it.first.toFloat()..it.second.toFloat()
                } ?: (0f..100f),
            )
        }

        Text(
            text = stringResource(
                R.string.advanced_filter_label_ratings,
                rangeRatingValue.value.start.toInt(),
                rangeRatingValue.value.endInclusive.toInt(),
            ),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        RangeSlider(
            value = rangeRatingValue.value,
            valueRange = 0f..100f,
            steps = 99, // Step every 1%
            onValueChange = { range ->
                rangeRatingValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeRatingValue.value
                onUpdateFilter(
                    state.filter.copy(
                        rating = value.start.toInt() to value.endInclusive.toInt(),
                    ),
                    true,
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = TraktTheme.colors.accent,
                inactiveTrackColor = Color.White,
                thumbColor = TraktTheme.colors.accent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )

        Box(
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                for (i in 0..100 step 100) {
                    Text(
                        text = "$i%",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 46.dp),
            ) {
                for (i in 20..80 step 20) {
                    Text(
                        text = "$i%",
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchesFilter(
    state: GlobalFiltersState,
    onUpdateFilter: (GlobalFilter, Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(RowsSpacing),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            val onCheckedChange: (Boolean) -> Unit = remember(state.filter) {
                {
                    onUpdateFilter(
                        state.filter.copy(
                            hideWatched = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filter.hideWatched,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = stringResource(R.string.header_hide_watched),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable {
                    onCheckedChange(!state.filter.hideWatched)
                },
            )
        }

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            val onCheckedChange: (Boolean) -> Unit = remember(state.filter) {
                {
                    onUpdateFilter(
                        state.filter.copy(
                            hideWatchlist = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filter.hideWatchlist,
                onCheckedChange = onCheckedChange,
            )
            Text(
                text = stringResource(R.string.header_hide_watchlisted),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable {
                    onCheckedChange(!state.filter.hideWatchlist)
                },
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        GlobalFiltersAdvancedView(state = GlobalFiltersState())
    }
}

@Preview(
    device = "id:pixel_2_xl",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        GlobalFiltersAdvancedView(
            state = GlobalFiltersState(
                filter = GlobalFilter.Default.copy(
                    genre = persistentListOf(
                        MediaGenre.Action,
                        MediaGenre.Comedy,
                    ),
                    availability = persistentListOf(
                        Availability.AllDigitalReleases,
                    ),
                    years = CurrentYear.years,
                ),
            ),
        )
    }
}
