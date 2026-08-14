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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.DeviceSheetPreview
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListSource.Discover
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

@Composable
internal fun CreateSmartListSheetView(
    state: CreateSmartListState,
    nameState: TextFieldState,
    modifier: Modifier = Modifier,
    onCreateClick: (String) -> Unit = {},
    onFiltersChange: (filter: SmartListFilters) -> Unit = {},
) {
    val inputValid by remember {
        derivedStateOf {
            nameState.text.isNotBlank()
        }
    }

    var filtersMode by rememberSaveable { mutableStateOf(GlobalFilterMode.Simple) }

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
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = spacedBy(24.dp),
            modifier = Modifier
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                horizontalArrangement = spacedBy(32.dp),
                verticalAlignment = CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TraktHeader(
                    title = stringResource(R.string.page_title_create_smart_list),
                    subtitle = stringResource(R.string.text_cta_smart_lists),
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
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = 10.dp.toPx()
                            },
                    ) {
                        for (filter in remember { arrayOf(Shows, Movies) }) {
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
            text = stringResource(R.string.button_text_create),
            enabled = inputValid && enabled,
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
        .filter { it != Unknown && it != Discover }
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
