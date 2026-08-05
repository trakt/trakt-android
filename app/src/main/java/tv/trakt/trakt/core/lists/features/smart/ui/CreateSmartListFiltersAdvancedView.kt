package tv.trakt.trakt.core.lists.features.smart.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Availability
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.core.filters.ColumnsSpacing
import tv.trakt.trakt.core.filters.RowsSpacing
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownMultiView
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.core.lists.features.smart.CreateSmartListState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.switch.TraktSwitch
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

private val YearsMin = 1930
private val YearsMax = 2040
private val RuntimeMin = 0
private val RuntimeMax = 500
private val RatingMin = 0
private val RatingMax = 100

@Composable
internal fun CreateSmartListFiltersAdvancedView(
    state: CreateSmartListState,
    onFiltersChange: (SmartListFilters, Boolean) -> Unit = { _, _ -> },
) {
    val enabled = !state.creating.isLoading

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth(),
    ) {
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
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )

                AvailabilityFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = spacedBy(RowsSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CertificationFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )

                RegionFilter(
                    state = state,
                    onUpdateFilter = onFiltersChange,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
            }

            YearsFilter(
                state = state,
                onUpdateFilter = onFiltersChange,
                enabled = enabled,
            )

            RuntimeFilter(
                state = state,
                onUpdateFilter = onFiltersChange,
                enabled = enabled,
            )

            RatingFilter(
                state = state,
                onUpdateFilter = onFiltersChange,
                enabled = enabled,
            )

            SwitchesFilter(
                state = state,
                onUpdateFilter = onFiltersChange,
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun GenreFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val genresValues: List<DropdownOption<MediaGenre?>> =
        when (val genres = state.filters.genres) {
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
        active = !state.filters.genres.isNullOrEmpty(),
        values = remember(state.filters.genres) {
            genresValues.toImmutableList()
        },
        options = genresOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filters.copy(
                    genres = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun AvailabilityFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val availabilityValues: List<DropdownOption<Availability?>> =
        when (val availability = state.filters.availability) {
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
        active = !state.filters.availability.isNullOrEmpty(),
        values = remember(state.filters.availability) {
            availabilityValues.toImmutableList()
        },
        options = availabilityOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filters.copy(
                    availability = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun CertificationFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val certificationValues: List<DropdownOption<Certification?>> =
        when (val certifications = state.filters.certifications) {
            null -> listOf(
                DropdownOption(
                    raw = null,
                    displayString = stringResource(R.string.option_text_all),
                ),
            )
            else -> certifications.mapNotNull { slug ->
                Certification.entries.find { it.slug == slug }?.let {
                    DropdownOption(
                        raw = it,
                        displayString = stringResource(it.displayStringRes),
                    )
                }
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
        active = !state.filters.certifications.isNullOrEmpty(),
        values = remember(state.filters.certifications) {
            certificationValues.toImmutableList()
        },
        options = certificationOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filters.copy(
                    certifications = options
                        .mapNotNull { it.raw?.slug }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun RegionFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val regionValues: List<DropdownOption<String?>> =
        when (val countries = state.filters.countries) {
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
        active = !state.filters.countries.isNullOrEmpty(),
        values = remember(state.filters.countries) {
            regionValues.toImmutableList()
        },
        options = regionOptions,
        onOptionsSelected = { options ->
            onUpdateFilter(
                state.filters.copy(
                    countries = options
                        .mapNotNull { it.raw }
                        .takeIf { it.isNotEmpty() }
                        ?.toImmutableList(),
                ),
                true,
            )
        },
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun YearsFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeYearsValue = remember(state.filters.years) {
            mutableStateOf(
                state.filters.years?.let {
                    (it.getOrNull(0) ?: YearsMin).toFloat()..(it.getOrNull(1) ?: YearsMax).toFloat()
                } ?: (YearsMin.toFloat()..YearsMax.toFloat()),
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
            enabled = enabled,
            valueRange = YearsMin.toFloat()..YearsMax.toFloat(),
            steps = 109, // Step every 1 year
            onValueChange = { range ->
                rangeYearsValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeYearsValue.value
                val start = value.start.toInt()
                val end = value.endInclusive.toInt()
                onUpdateFilter(
                    state.filters.copy(
                        years = if (start == YearsMin && end == YearsMax) {
                            null
                        } else {
                            persistentListOf(start, end)
                        },
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
                disabledActiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledActiveTickColor = TraktTheme.colors.dialogOnContainer,
                disabledThumbColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTickColor = TraktTheme.colors.dialogOnContainer,
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
                for (i in YearsMin..YearsMax step 110) {
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
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeRuntimeValue = remember(state.filters.runtimes) {
            mutableStateOf(
                state.filters.runtimes?.let {
                    (it.getOrNull(0) ?: RuntimeMin).toFloat()..(it.getOrNull(1) ?: RuntimeMax).toFloat()
                } ?: (RuntimeMin.toFloat()..RuntimeMax.toFloat()),
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
            enabled = enabled,
            valueRange = RuntimeMin.toFloat()..RuntimeMax.toFloat(),
            steps = 499, // Step every 1 min
            onValueChange = { range ->
                rangeRuntimeValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeRuntimeValue.value
                val start = value.start.toInt()
                val end = value.endInclusive.toInt()
                onUpdateFilter(
                    state.filters.copy(
                        runtimes = if (start == RuntimeMin && end == RuntimeMax) {
                            null
                        } else {
                            persistentListOf(start, end)
                        },
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
                disabledActiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledActiveTickColor = TraktTheme.colors.dialogOnContainer,
                disabledThumbColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTickColor = TraktTheme.colors.dialogOnContainer,
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
                for (i in RuntimeMin..RuntimeMax step 500) {
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
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
    ) {
        val rangeRatingValue = remember(state.filters.ratings) {
            mutableStateOf(
                state.filters.ratings?.let {
                    (it.getOrNull(0) ?: RatingMin).toFloat()..(it.getOrNull(1) ?: RatingMax).toFloat()
                } ?: (RatingMin.toFloat()..RatingMax.toFloat()),
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
            enabled = enabled,
            valueRange = RatingMin.toFloat()..RatingMax.toFloat(),
            steps = 99, // Step every 1%
            onValueChange = { range ->
                rangeRatingValue.value = range
            },
            onValueChangeFinished = {
                val value = rangeRatingValue.value
                val start = value.start.toInt()
                val end = value.endInclusive.toInt()
                onUpdateFilter(
                    state.filters.copy(
                        ratings = if (start == RatingMin && end == RatingMax) {
                            null
                        } else {
                            persistentListOf(start, end)
                        },
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
                disabledActiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledActiveTickColor = TraktTheme.colors.dialogOnContainer,
                disabledThumbColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTickColor = TraktTheme.colors.dialogOnContainer,
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
                for (i in RatingMin..RatingMax step 100) {
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
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters, Boolean) -> Unit,
    enabled: Boolean,
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
            val onCheckedChange: (Boolean) -> Unit = remember(state.filters) {
                {
                    onUpdateFilter(
                        state.filters.copy(
                            ignoreWatched = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filters.ignoreWatched,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
            Text(
                text = stringResource(R.string.header_hide_watched),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable(enabled = enabled) {
                    onCheckedChange(!state.filters.ignoreWatched)
                },
            )
        }

        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            val onCheckedChange: (Boolean) -> Unit = remember(state.filters) {
                {
                    onUpdateFilter(
                        state.filters.copy(
                            ignoreWatchlisted = it,
                        ),
                        false,
                    )
                }
            }

            TraktSwitch(
                checked = state.filters.ignoreWatchlisted,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
            Text(
                text = stringResource(R.string.header_hide_watchlisted),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 2,
                overflow = Ellipsis,
                modifier = Modifier.clickable(enabled = enabled) {
                    onCheckedChange(!state.filters.ignoreWatchlisted)
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
        CreateSmartListFiltersAdvancedView(state = CreateSmartListState())
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
        CreateSmartListFiltersAdvancedView(
            state = CreateSmartListState(
                filters = SmartListFilters.Default.copy(
                    genres = persistentListOf(
                        MediaGenre.Action,
                        MediaGenre.Comedy,
                    ),
                    availability = persistentListOf(
                        Availability.AllDigitalReleases,
                    ),
                    years = persistentListOf(2000, 2020),
                    ratings = persistentListOf(60, 100),
                    ignoreWatched = true,
                ),
            ),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewDisabled() {
    TraktTheme {
        CreateSmartListFiltersAdvancedView(
            state = CreateSmartListState(
                creating = LoadingState.Loading,
                filters = SmartListFilters.Default.copy(
                    genres = persistentListOf(
                        MediaGenre.Action,
                        MediaGenre.Comedy,
                    ),
                    availability = persistentListOf(
                        Availability.AllDigitalReleases,
                    ),
                    years = persistentListOf(2000, 2020),
                    ratings = persistentListOf(60, 100),
                    ignoreWatched = true,
                ),
            ),
        )
    }
}
