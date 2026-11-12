package tv.trakt.trakt.core.lists.features.smart.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Certification
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter.Region
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterDecade
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterRuntime
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListSource.Unknown
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownOption
import tv.trakt.trakt.core.filters.views.dropdowns.DropdownView
import tv.trakt.trakt.core.lists.features.smart.CreateSmartListState
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.components.chips.FilterChipGroup
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.ZonedDateTime

@Composable
internal fun CreateSmartListSheetView(
    state: CreateSmartListState,
    nameState: TextFieldState,
    modifier: Modifier = Modifier,
    onCreateClick: (String) -> Unit = {},
    onFiltersChange: (filter: SmartListFilters) -> Unit = {},
    onParentalGuideExpandedChange: (Boolean) -> Unit = {},
) {
    val inputValid by remember {
        derivedStateOf {
            nameState.text.isNotBlank()
        }
    }

    var filtersMode by rememberSaveable {
        mutableStateOf(
            when {
                state.filters.fitsSimpleView() -> GlobalFilterMode.Simple
                else -> GlobalFilterMode.Advanced
            },
        )
    }

    val backgroundColor = TraktTheme.colors.dialogContainer
    val backgroundGradient = remember(backgroundColor) {
        verticalGradient(
            colors = listOf(
                Color.Transparent,
                backgroundColor.copy(alpha = 0.85f),
                backgroundColor,
            ),
        )
    }

    val enabled = !state.creating.isLoading
    val isEditing = state.editList != null
    val canSave = inputValid && (!isEditing || state.hasChanges(nameState.text.toString()))
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = spacedBy(24.dp),
            modifier = Modifier
                .verticalScroll(
                    rememberScrollState(),
                    overscrollEffect = null,
                ),
        ) {
            Row(
                horizontalArrangement = spacedBy(32.dp),
                verticalAlignment = CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TraktHeader(
                    title = when {
                        isEditing -> stringResource(R.string.header_edit_smart_list)
                        else -> stringResource(R.string.page_title_create_smart_list)
                    },
                    subtitle = when {
                        isEditing -> null
                        else -> stringResource(R.string.text_cta_smart_lists)
                    },
                    maxSubtitleLines = 3,
                    modifier = Modifier.weight(1f),
                )

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = CenterVertically,
                ) {
                    val currentIcon = when (filtersMode) {
                        GlobalFilterMode.Simple -> R.drawable.ic_filters_simple
                        GlobalFilterMode.Advanced -> R.drawable.ic_filters_advanced
                    }
                    val targetMode = when (filtersMode) {
                        GlobalFilterMode.Simple -> GlobalFilterMode.Advanced
                        GlobalFilterMode.Advanced -> GlobalFilterMode.Simple
                    }

                    FilterChip(
                        selected = false,
                        text = stringResource(filtersMode.displayStringRes),
                        height = 32.dp,
                        leadingAlwaysVisible = true,
                        leadingContent = {
                            Icon(
                                painter = painterResource(currentIcon),
                                contentDescription = null,
                                tint = TraktTheme.colors.textPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            if (!enabled) {
                                return@FilterChip
                            }
                            filtersMode = targetMode
                            onFiltersChange(state.filters.cleared())
                        },
                    )
                }
            }

            InputField(
                state = nameState,
                enabled = enabled,
                placeholder = stringResource(R.string.input_placeholder_lists_name),
                border = 1.dp,
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth(),
            )

            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier,
            ) {
                Row(
                    verticalAlignment = CenterVertically,
                    horizontalArrangement = spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TargetFilter(
                        state = state,
                        onUpdateFilter = { onFiltersChange(it) },
                        modifier = Modifier.weight(1f),
                    )

                    FilterChipGroup(
                        paddingVertical = PaddingValues.Zero,
                    ) {
                        for (filter in MediaMode.entries) {
                            FilterChip(
                                selected = state.filters.media == filter,
                                animated = true,
                                height = 36.dp,
                                text = stringResource(filter.displayRes),
                                unselectedTextVisible = false,
                                leadingContent = {
                                    Icon(
                                        painter = painterResource(filter.offIcon),
                                        contentDescription = null,
                                        tint = when {
                                            state.filters.media == filter -> TraktTheme.colors.textPrimaryOnAccent
                                            else -> TraktTheme.colors.textPrimary
                                        },
                                        modifier = Modifier.size(16.dp),
                                    )
                                },
                                onClick = {
                                    if (!enabled) {
                                        return@FilterChip
                                    }
                                    onFiltersChange(state.filters.copy(media = filter))
                                },
                            )
                        }
                    }
                }

                when (filtersMode) {
                    GlobalFilterMode.Simple -> CreateSmartListFiltersSimpleView(
                        state = state,
                        onFiltersChange = { filters, _ ->
                            onFiltersChange(filters)
                        },
                    )
                    GlobalFilterMode.Advanced -> CreateSmartListFiltersAdvancedView(
                        state = state,
                        onParentalGuideExpandedChange = onParentalGuideExpandedChange,
                        onFiltersChange = { filters, _ ->
                            onFiltersChange(filters)
                        },
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .height(112.dp),
            )
        }

        // Gradient scrim to fade out the content behind the button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(132.dp)
                .background(
                    brush = backgroundGradient,
                ),
        )

        PrimaryButton(
            text = when {
                isEditing -> stringResource(R.string.button_text_apply)
                else -> stringResource(R.string.button_text_create)
            },
            enabled = canSave && enabled,
            loading = state.creating.isLoading,
            onClick = {
                onCreateClick(nameState.text.toString())
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun TargetFilter(
    state: CreateSmartListState,
    onUpdateFilter: (SmartListFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = DropdownOption(
        raw = state.filters.source,
        displayString = stringResource(state.filters.source.displayRes),
    )

    val options = SmartListSource.entries
        .filter { it != Unknown }
        .map {
            DropdownOption(
                raw = it,
                displayString = stringResource(it.displayRes),
            )
        }.toImmutableList()

    DropdownView(
        header = stringResource(R.string.header_target),
        enabled = !state.creating.isLoading,
        active = state.filters.source != SmartListFilters.Default.source,
        value = value,
        options = options,
        onOptionSelected = { option ->
            onUpdateFilter(
                state.filters.copy(source = option.raw),
            )
        },
        modifier = modifier,
    )
}

private fun SmartListFilters.fitsSimpleView(): Boolean {
    val unsupported = listOf(
        excludedGenres,
        subgenres,
        excludedCertifications,
        languages,
        excludedCountries,
        statuses,
        networks,
        imdbRatings,
        rtMeters,
        rtUserMeters,
    ) + ParentalGuideCategory.entries.map(::parentalRange)

    if (unsupported.any { !it.isNullOrEmpty() }) return false
    if (genres.orEmpty().size > 1) return false
    if (availability.orEmpty().size > 1) return false
    if (ratings.orEmpty().size > 2) return false

    val certification = certifications.orEmpty()
    if (certification.size > 1) return false
    if (certification.any { slug -> Certification.entries.none { it.slug == slug } }) return false

    val countryCodes = countries.orEmpty()
    if (countryCodes.isNotEmpty() && Region.fromCountries(countryCodes) == null) return false

    val yearRange = years.orEmpty()
    val isDecade = GlobalFilterDecade.entries.any { listOf(it.years.first, it.years.second) == yearRange }
    if (yearRange.isNotEmpty() && !isDecade) return false

    val runtimeRange = runtimes.orEmpty()
    val isRuntimeSpan = GlobalFilterRuntime.entries.any { listOf(it.runtime.first, it.runtime.second) == runtimeRange }
    if (runtimeRange.isNotEmpty() && !isRuntimeSpan) return false

    return true
}

@DeviceSheetPreview
@Composable
private fun Preview() {
    TraktTheme {
        CreateSmartListSheetView(
            state = CreateSmartListState(),
            nameState = TextFieldState(),
        )
    }
}

@DeviceSheetPreview
@Composable
private fun MediaPreview() {
    TraktTheme {
        CreateSmartListSheetView(
            state = CreateSmartListState(
                filters = SmartListFilters.Default.copy(media = MediaMode.Media),
            ),
            nameState = TextFieldState(),
        )
    }
}

@DeviceSheetPreview
@Composable
private fun EditPreview() {
    val filters = SmartListFilters.Default.copy(
        genres = persistentListOf(MediaGenre.Action),
        ignoreWatched = true,
    )

    TraktTheme {
        CreateSmartListSheetView(
            state = CreateSmartListState(
                editList = SmartList(
                    ids = Ids(trakt = TraktId(1), slug = SlugId("smart")),
                    name = "Unwatched action",
                    privacy = null,
                    createdAt = ZonedDateTime.now(),
                    updatedAt = ZonedDateTime.now(),
                    images = null,
                    filters = filters,
                ),
                initialFilters = filters,
                filters = filters,
            ),
            nameState = TextFieldState("Unwatched action"),
        )
    }
}
