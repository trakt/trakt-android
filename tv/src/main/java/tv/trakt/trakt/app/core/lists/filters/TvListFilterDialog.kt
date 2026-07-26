package tv.trakt.trakt.app.core.lists.filters

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.app.common.ui.ConfirmationDialog
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.helpers.extensions.requestSafeFocus
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Availability
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime
import tv.trakt.trakt.resources.R
import java.time.Year
import java.util.Locale

@Composable
internal fun TvListFilterDialog(
    appliedFilter: GlobalFilter,
    modifier: Modifier = Modifier,
    initialMode: GlobalFilterMode,
    configuration: TvListFilterConfiguration,
    onApply: (GlobalFilter, GlobalFilterMode) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(appliedFilter, configuration) {
        mutableStateOf(configuration.normalize(appliedFilter))
    }
    var mode by remember(initialMode) {
        mutableStateOf(initialMode)
    }
    val sections = remember(configuration) {
        buildFilterSections(configuration)
    }
    var selectedSection by remember(sections) {
        mutableStateOf(sections.first())
    }
    var showSimpleConfirmation by remember { mutableStateOf(false) }
    val sectionFocusRequesters = remember {
        mutableMapOf<TvFilterSection, FocusRequester>()
    }
    val contentFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        sectionFocusRequesters[selectedSection]?.requestSafeFocus()
    }

    fun switchMode(newMode: GlobalFilterMode) {
        if (newMode == mode) return

        if (
            mode == GlobalFilterMode.Advanced &&
            newMode == GlobalFilterMode.Simple &&
            configuration.hasSimpleIncompatibleValues(draft)
        ) {
            showSimpleConfirmation = true
            return
        }

        if (mode == GlobalFilterMode.Advanced && newMode == GlobalFilterMode.Simple) {
            draft = configuration.toSimple(draft)
        }
        if (newMode == GlobalFilterMode.Advanced && draft.years == 0 to 0) {
            val currentYear = Year.now().value
            draft = draft.copy(years = currentYear to currentYear)
        }
        mode = newMode
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(FilterDialogSectionSpacing),
            modifier = modifier
                .fillMaxWidth(0.9F)
                .fillMaxHeight(0.9F)
                .background(
                    color = TraktTheme.colors.dialogContainer,
                    shape = RoundedCornerShape(FilterDialogCornerRadius),
                )
                .padding(FilterDialogContentPadding),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.header_filters),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
                    modifier = Modifier.focusGroup(),
                ) {
                    GlobalFilterMode.entries.forEach { option ->
                        PrimaryButton(
                            text = stringResource(option.displayStringRes),
                            containerColor = when (mode == option) {
                                true -> TraktTheme.colors.accent
                                false -> TraktTheme.colors.chipContainer
                            },
                            onClick = { switchMode(option) },
                            modifier = Modifier.width(FilterModeButtonWidth),
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(FilterDialogSectionSpacing),
                modifier = Modifier
                    .weight(1F)
                    .fillMaxWidth(),
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
                    modifier = Modifier
                        .width(FilterSectionsWidth)
                        .fillMaxHeight()
                        .focusGroup(),
                ) {
                    itemsIndexed(
                        items = sections,
                        key = { _, section -> section.name },
                    ) { _, section ->
                        val selected = section == selectedSection
                        val focusRequester = remember(section) {
                            sectionFocusRequesters.getOrPut(section) {
                                FocusRequester()
                            }
                        }

                        PrimaryButton(
                            text = stringResource(section.displayStringRes(mode)),
                            textAllCaps = false,
                            containerColor = when (selected) {
                                true -> TraktTheme.colors.accent
                                false -> TraktTheme.colors.chipContainer
                            },
                            onClick = {
                                selectedSection = section
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .focusProperties {
                                    right = contentFocusRequester
                                },
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1F)
                        .fillMaxHeight(),
                ) {
                    FilterSectionContent(
                        section = selectedSection,
                        mode = mode,
                        filter = draft,
                        allowedMediaModes = configuration.allowedMediaModes,
                        contentFocusRequester = contentFocusRequester,
                        leftFocusRequester = sectionFocusRequesters[selectedSection]
                            ?: FocusRequester.Default,
                        onFilterChanged = { draft = configuration.normalize(it) },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusGroup(),
            ) {
                PrimaryButton(
                    text = stringResource(R.string.button_text_cancel),
                    containerColor = TraktTheme.colors.chipContainer,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1F),
                )
                PrimaryButton(
                    text = stringResource(R.string.button_text_reset_all_filters),
                    containerColor = TraktTheme.colors.chipContainer,
                    enabled = draft.isActive,
                    onClick = {
                        draft = configuration.reset(draft)
                    },
                    modifier = Modifier.weight(1F),
                )
                PrimaryButton(
                    text = stringResource(R.string.button_text_apply),
                    onClick = {
                        onApply(configuration.normalize(draft), mode)
                    },
                    modifier = Modifier.weight(2F),
                )
            }
        }
    }

    if (showSimpleConfirmation) {
        Dialog(
            onDismissRequest = { showSimpleConfirmation = false },
        ) {
            ConfirmationDialog(
                title = stringResource(R.string.header_filters),
                message = stringResource(R.string.warning_prompt_simple_filters),
                onCancel = { showSimpleConfirmation = false },
                onConfirm = {
                    draft = configuration.toSimple(draft)
                    mode = GlobalFilterMode.Simple
                    showSimpleConfirmation = false
                },
            )
        }
    }
}

@Composable
private fun FilterSectionContent(
    section: TvFilterSection,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    filter: GlobalFilter,
    allowedMediaModes: ImmutableList<MediaMode>,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    when (section) {
        TvFilterSection.Media -> MediaModeFilter(
            filter = filter,
            allowedMediaModes = allowedMediaModes,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Genre -> GenreFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Availability -> AvailabilityFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Years -> YearsFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Runtime -> RuntimeFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Certification -> CertificationFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Region -> RegionFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.Rating -> RatingFilter(
            filter = filter,
            mode = mode,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onFilterChanged = onFilterChanged,
            modifier = modifier,
        )

        TvFilterSection.HideWatched -> ToggleFilter(
            text = stringResource(R.string.header_hide_watched),
            selected = filter.hideWatched,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onClick = {
                onFilterChanged(filter.copy(hideWatched = !filter.hideWatched))
            },
            modifier = modifier,
        )

        TvFilterSection.HideWatchlisted -> ToggleFilter(
            text = stringResource(R.string.header_hide_watchlisted),
            selected = filter.hideWatchlist,
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onClick = {
                onFilterChanged(filter.copy(hideWatchlist = !filter.hideWatchlist))
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun MediaModeFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    allowedMediaModes: ImmutableList<MediaMode>,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    val choices = allowedMediaModes.map {
        TvFilterChoice(
            value = it,
            text = stringResource(it.displayRes),
        )
    }
    ChoiceList(
        choices = choices,
        isSelected = { it == filter.mode },
        onChoiceClick = {
            onFilterChanged(filter.copy(mode = it))
        },
        contentFocusRequester = contentFocusRequester,
        leftFocusRequester = leftFocusRequester,
        modifier = modifier,
    )
}

@Composable
private fun GenreFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    val allChoice = TvFilterChoice<MediaGenre?>(
        value = null,
        text = stringResource(R.string.option_text_all),
    )
    val choices = listOf(allChoice) + MediaGenre.entries.map {
        TvFilterChoice<MediaGenre?>(
            value = it,
            text = stringResource(it.displayStringRes),
        )
    }

    ChoiceList(
        choices = choices,
        isSelected = { genre ->
            when (mode) {
                GlobalFilterMode.Simple -> filter.genre?.firstOrNull() == genre
                GlobalFilterMode.Advanced -> when (genre) {
                    null -> filter.genre.isNullOrEmpty()
                    else -> filter.genre?.contains(genre) == true
                }
            }
        },
        onChoiceClick = { genre ->
            val selected = when (mode) {
                GlobalFilterMode.Simple ->
                    genre
                        ?.let { listOf(it).toImmutableList() }

                GlobalFilterMode.Advanced -> filter.genre.toggle(genre)
            }
            onFilterChanged(filter.copy(genre = selected))
        },
        contentFocusRequester = contentFocusRequester,
        leftFocusRequester = leftFocusRequester,
        modifier = modifier,
    )
}

@Composable
private fun AvailabilityFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    val choices = listOf(
        TvFilterChoice<Availability?>(
            value = null,
            text = stringResource(R.string.option_text_all),
        ),
    ) + Availability.entries.map {
        TvFilterChoice<Availability?>(
            value = it,
            text = stringResource(it.displayStringRes),
        )
    }

    ChoiceList(
        choices = choices,
        isSelected = { availability ->
            when (mode) {
                GlobalFilterMode.Simple -> filter.availability?.firstOrNull() == availability
                GlobalFilterMode.Advanced -> when (availability) {
                    null -> filter.availability.isNullOrEmpty()
                    else -> filter.availability?.contains(availability) == true
                }
            }
        },
        onChoiceClick = { availability ->
            val selected = when (mode) {
                GlobalFilterMode.Simple ->
                    availability
                        ?.let { listOf(it).toImmutableList() }

                GlobalFilterMode.Advanced -> filter.availability.toggle(availability)
            }
            onFilterChanged(filter.copy(availability = selected))
        },
        contentFocusRequester = contentFocusRequester,
        leftFocusRequester = leftFocusRequester,
        modifier = modifier,
    )
}

@Composable
private fun YearsFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    when (mode) {
        GlobalFilterMode.Simple -> {
            val choices = listOf(
                TvFilterChoice<GlobalFilterDecade?>(
                    value = null,
                    text = stringResource(R.string.option_text_all),
                ),
            ) + GlobalFilterDecade.entries.map {
                TvFilterChoice<GlobalFilterDecade?>(
                    value = it,
                    text = when (it) {
                        GlobalFilterDecade.CurrentYear -> stringResource(R.string.text_this_year)
                        else -> it.years.first.toString()
                    },
                )
            }
            ChoiceList(
                choices = choices,
                isSelected = { it?.years == filter.years },
                onChoiceClick = {
                    onFilterChanged(filter.copy(years = it?.years))
                },
                contentFocusRequester = contentFocusRequester,
                leftFocusRequester = leftFocusRequester,
                modifier = modifier,
            )
        }

        GlobalFilterMode.Advanced -> RangeFilter(
            range = filter.years,
            specification = TvRangeSpecification(
                minimum = 1930,
                maximum = 2040,
                step = 1,
                label = stringResource(
                    R.string.advanced_filter_label_release_year,
                    filter.years?.first ?: 1930,
                    filter.years?.second ?: 2040,
                ),
            ),
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onRangeChanged = {
                onFilterChanged(filter.copy(years = it))
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun RuntimeFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    when (mode) {
        GlobalFilterMode.Simple -> {
            val choices = listOf(
                TvFilterChoice<GlobalFilterRuntime?>(
                    value = null,
                    text = stringResource(R.string.option_text_all),
                ),
            ) + GlobalFilterRuntime.entries.map {
                TvFilterChoice<GlobalFilterRuntime?>(
                    value = it,
                    text = when (it) {
                        GlobalFilterRuntime.Runtime120Plus -> "${it.runtime.first}+"
                        else -> "${it.runtime.first}-${it.runtime.second}"
                    },
                )
            }
            ChoiceList(
                choices = choices,
                isSelected = { it?.runtime == filter.runtime },
                onChoiceClick = {
                    onFilterChanged(filter.copy(runtime = it?.runtime))
                },
                contentFocusRequester = contentFocusRequester,
                leftFocusRequester = leftFocusRequester,
                modifier = modifier,
            )
        }

        GlobalFilterMode.Advanced -> RangeFilter(
            range = filter.runtime,
            specification = TvRangeSpecification(
                minimum = 0,
                maximum = 500,
                step = 1,
                label = stringResource(
                    R.string.advanced_filter_label_runtime,
                    filter.runtime?.first ?: 0,
                    filter.runtime?.second ?: 500,
                ),
            ),
            contentFocusRequester = contentFocusRequester,
            leftFocusRequester = leftFocusRequester,
            onRangeChanged = {
                onFilterChanged(filter.copy(runtime = it))
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun CertificationFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    val choices = listOf(
        TvFilterChoice<Certification?>(
            value = null,
            text = stringResource(R.string.option_text_all),
        ),
    ) + Certification.entries.map {
        TvFilterChoice<Certification?>(
            value = it,
            text = stringResource(it.displayStringRes),
        )
    }

    ChoiceList(
        choices = choices,
        isSelected = { certification ->
            when (mode) {
                GlobalFilterMode.Simple -> filter.certification?.firstOrNull() == certification
                GlobalFilterMode.Advanced -> when (certification) {
                    null -> filter.certification.isNullOrEmpty()
                    else -> filter.certification?.contains(certification) == true
                }
            }
        },
        onChoiceClick = { certification ->
            val selected = when (mode) {
                GlobalFilterMode.Simple ->
                    certification
                        ?.let { listOf(it).toImmutableList() }

                GlobalFilterMode.Advanced -> filter.certification.toggle(certification)
            }
            onFilterChanged(filter.copy(certification = selected))
        },
        contentFocusRequester = contentFocusRequester,
        leftFocusRequester = leftFocusRequester,
        modifier = modifier,
    )
}

@Composable
private fun RegionFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    when (mode) {
        GlobalFilterMode.Simple -> {
            val choices = listOf(
                TvFilterChoice<Region?>(
                    value = null,
                    text = stringResource(R.string.option_text_all),
                ),
            ) + Region.entries.map {
                TvFilterChoice<Region?>(
                    value = it,
                    text = stringResource(it.displayStringRes),
                )
            }
            ChoiceList(
                choices = choices,
                isSelected = { it == filter.region },
                onChoiceClick = {
                    onFilterChanged(
                        filter.copy(
                            region = it,
                            countries = null,
                        ),
                    )
                },
                contentFocusRequester = contentFocusRequester,
                leftFocusRequester = leftFocusRequester,
                modifier = modifier,
            )
        }

        GlobalFilterMode.Advanced -> {
            val allChoice = TvFilterChoice<String?>(
                value = null,
                text = stringResource(R.string.option_text_all),
            )
            val countryChoices = remember {
                Region.AllLocales
                    .map { it.language }
                    .distinct()
                    .mapNotNull { code ->
                        Locale.Builder()
                            .setRegion(code.uppercase(Locale.ROOT))
                            .build()
                            .displayCountry
                            .takeIf(String::isNotBlank)
                            ?.let { TvFilterChoice<String?>(code, it) }
                    }
                    .sortedBy { it.text }
            }
            ChoiceList(
                choices = listOf(allChoice) + countryChoices,
                isSelected = { country ->
                    when (country) {
                        null -> filter.countries.isNullOrEmpty()
                        else -> filter.countries?.contains(country) == true
                    }
                },
                onChoiceClick = { country ->
                    onFilterChanged(
                        filter.copy(
                            region = null,
                            countries = filter.countries.toggle(country),
                        ),
                    )
                },
                contentFocusRequester = contentFocusRequester,
                leftFocusRequester = leftFocusRequester,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun RatingFilter(
    filter: GlobalFilter,
    modifier: Modifier = Modifier,
    mode: GlobalFilterMode,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onFilterChanged: (GlobalFilter) -> Unit,
) {
    val currentRange = filter.rating ?: (0 to 100)
    val label = stringResource(
        when (mode) {
            GlobalFilterMode.Simple -> R.string.filter_label_ratings
            GlobalFilterMode.Advanced -> R.string.advanced_filter_label_ratings
        },
        currentRange.first,
        currentRange.second,
    )

    RangeFilter(
        range = filter.rating,
        specification = TvRangeSpecification(
            minimum = 0,
            maximum = 100,
            step = when (mode) {
                GlobalFilterMode.Simple -> 5
                GlobalFilterMode.Advanced -> 1
            },
            label = label,
        ),
        contentFocusRequester = contentFocusRequester,
        leftFocusRequester = leftFocusRequester,
        onRangeChanged = {
            onFilterChanged(filter.copy(rating = it))
        },
        modifier = modifier,
    )
}

@Composable
private fun ToggleFilter(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onClick: () -> Unit,
) {
    PrimaryButton(
        text = text,
        textAllCaps = false,
        icon = when (selected) {
            true -> painterResource(R.drawable.ic_check_2)
            false -> null
        },
        containerColor = when (selected) {
            true -> TraktTheme.colors.accent
            false -> TraktTheme.colors.chipContainer
        },
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(contentFocusRequester)
            .focusProperties {
                left = leftFocusRequester
            },
    )
}

@Composable
private fun <T> ChoiceList(
    choices: List<TvFilterChoice<T>>,
    modifier: Modifier = Modifier,
    isSelected: (T) -> Boolean,
    onChoiceClick: (T) -> Unit,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
        modifier = modifier
            .fillMaxSize()
            .focusGroup(),
    ) {
        itemsIndexed(
            items = choices,
            key = { index, choice -> "${choice.text}-$index" },
        ) { index, choice ->
            val selected = isSelected(choice.value)
            PrimaryButton(
                text = choice.text,
                textAllCaps = false,
                icon = when (selected) {
                    true -> painterResource(R.drawable.ic_check_2)
                    false -> null
                },
                containerColor = when (selected) {
                    true -> TraktTheme.colors.accent
                    false -> TraktTheme.colors.chipContainer
                },
                onClick = { onChoiceClick(choice.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        when (index) {
                            0 -> Modifier.focusRequester(contentFocusRequester)
                            else -> Modifier
                        },
                    )
                    .focusProperties {
                        left = leftFocusRequester
                    },
            )
        }
    }
}

@Composable
private fun RangeFilter(
    range: Pair<Int, Int>?,
    modifier: Modifier = Modifier,
    specification: TvRangeSpecification,
    contentFocusRequester: FocusRequester,
    leftFocusRequester: FocusRequester,
    onRangeChanged: (Pair<Int, Int>?) -> Unit,
) {
    val current = range ?: (specification.minimum to specification.maximum)

    fun update(
        start: Int,
        end: Int,
    ) {
        val newRange = start to end
        onRangeChanged(
            newRange.takeUnless {
                it.first == specification.minimum &&
                    it.second == specification.maximum
            },
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(FilterDialogSectionSpacing),
        modifier = modifier,
    ) {
        Text(
            text = specification.label,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(FilterDialogSectionSpacing),
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup(),
        ) {
            RangeValueControl(
                value = current.first,
                minimum = specification.minimum,
                maximum = current.second,
                step = specification.step,
                contentFocusRequester = contentFocusRequester,
                leftFocusRequester = leftFocusRequester,
                onValueChanged = { update(it, current.second) },
                modifier = Modifier.weight(1F),
            )
            RangeValueControl(
                value = current.second,
                minimum = current.first,
                maximum = specification.maximum,
                step = specification.step,
                contentFocusRequester = null,
                leftFocusRequester = leftFocusRequester,
                onValueChanged = { update(current.first, it) },
                modifier = Modifier.weight(1F),
            )
        }

        PrimaryButton(
            text = stringResource(R.string.option_text_all),
            textAllCaps = false,
            containerColor = when (range) {
                null -> TraktTheme.colors.accent
                else -> TraktTheme.colors.chipContainer
            },
            onClick = { onRangeChanged(null) },
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties {
                    left = leftFocusRequester
                },
        )
    }
}

@Composable
private fun RangeValueControl(
    value: Int,
    modifier: Modifier = Modifier,
    minimum: Int,
    maximum: Int,
    step: Int,
    contentFocusRequester: FocusRequester?,
    leftFocusRequester: FocusRequester,
    onValueChanged: (Int) -> Unit,
) {
    val canDecrease = value > minimum
    val canIncrease = value < maximum

    Column(
        verticalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
        modifier = modifier,
    ) {
        Text(
            text = value.toString(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading3,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(TraktTheme.spacing.mainGridSpace),
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup(),
        ) {
            PrimaryButton(
                text = "−",
                enabled = canDecrease,
                onClick = {
                    onValueChanged((value - step).coerceAtLeast(minimum))
                },
                modifier = Modifier
                    .weight(1F)
                    .then(
                        when {
                            contentFocusRequester != null && canDecrease ->
                                Modifier.focusRequester(contentFocusRequester)

                            else -> Modifier
                        },
                    )
                    .focusProperties {
                        left = leftFocusRequester
                    },
            )
            PrimaryButton(
                text = "+",
                enabled = canIncrease,
                onClick = {
                    onValueChanged((value + step).coerceAtMost(maximum))
                },
                modifier = Modifier
                    .weight(1F)
                    .then(
                        when {
                            contentFocusRequester != null && !canDecrease ->
                                Modifier.focusRequester(contentFocusRequester)

                            else -> Modifier
                        },
                    )
                    .focusProperties {
                        left = leftFocusRequester
                    },
            )
        }
    }
}

private fun buildFilterSections(configuration: TvListFilterConfiguration): List<TvFilterSection> {
    return buildList {
        if (configuration.allowedMediaModes.size > 1) {
            add(TvFilterSection.Media)
        }
        add(TvFilterSection.Genre)
        add(TvFilterSection.Availability)
        add(TvFilterSection.Years)
        add(TvFilterSection.Runtime)
        add(TvFilterSection.Certification)
        add(TvFilterSection.Region)
        add(TvFilterSection.Rating)
        if (configuration.showHideWatched) {
            add(TvFilterSection.HideWatched)
        }
        if (configuration.showHideWatchlisted) {
            add(TvFilterSection.HideWatchlisted)
        }
    }
}

private fun <T> ImmutableList<T>?.toggle(value: T?): ImmutableList<T>? {
    if (value == null) return null

    val current = orEmpty()
    val updated = when (current.contains(value)) {
        true -> current.filterNot { it == value }
        false -> current + value
    }
    return updated
        .takeIf(List<T>::isNotEmpty)
        ?.toImmutableList()
}

@Immutable
private data class TvFilterChoice<T>(
    val value: T,
    val text: String,
)

@Immutable
private data class TvRangeSpecification(
    val minimum: Int,
    val maximum: Int,
    val step: Int,
    val label: String,
)

private enum class TvFilterSection(
    @StringRes private val simpleStringRes: Int,
    @StringRes private val advancedStringRes: Int = simpleStringRes,
) {
    Media(R.string.button_text_media),
    Genre(R.string.header_genre),
    Availability(R.string.header_streaming),
    Years(
        simpleStringRes = R.string.header_decade,
        advancedStringRes = R.string.header_decade,
    ),
    Runtime(R.string.header_runtime),
    Certification(R.string.header_certification),
    Region(
        simpleStringRes = R.string.header_region,
        advancedStringRes = R.string.header_country,
    ),
    Rating(R.string.header_ratings),
    HideWatched(R.string.header_hide_watched),
    HideWatchlisted(R.string.header_hide_watchlisted),
    ;

    @StringRes
    fun displayStringRes(mode: GlobalFilterMode): Int {
        return when (mode) {
            GlobalFilterMode.Simple -> simpleStringRes
            GlobalFilterMode.Advanced -> advancedStringRes
        }
    }
}

private val FilterDialogContentPadding = 24.dp
private val FilterDialogSectionSpacing = 16.dp
private val FilterDialogCornerRadius = 20.dp
private val FilterSectionsWidth = 210.dp
private val FilterModeButtonWidth = 132.dp
