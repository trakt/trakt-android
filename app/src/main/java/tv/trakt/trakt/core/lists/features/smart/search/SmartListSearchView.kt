package tv.trakt.trakt.core.lists.features.smart.search

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.globalfilter.EXCLUSION_PREFIX
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.common.ui.theme.colors.Red400
import tv.trakt.trakt.core.filters.views.dropdowns.ExclusionToggle
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.theme.TraktTheme

private enum class SelectionState {
    Included,
    Excluded,
}

@Composable
internal fun SmartListSearchView(
    kind: SmartListSearchKind,
    state: SmartListSearchState,
    values: ImmutableList<String>,
    onQueryChange: (String) -> Unit,
    onValuesChange: (ImmutableList<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputState = rememberTextFieldState(state.query)
    val knownLabels = remember { mutableStateMapOf<String, String>() }

    LaunchedEffect(Unit) {
        snapshotFlow { inputState.text.toString() }
            .collect(onQueryChange)
    }

    LaunchedEffect(state.results) {
        state.results.forEach { knownLabels[it.value] = it.label }
    }

    val resultValues = state.results.map { it.value }.toSet()
    val selectedOnly = values
        .map { it.removePrefix(EXCLUSION_PREFIX) }
        .filterNot { it in resultValues }
        .map { SmartListSearchOption(value = it, label = knownLabels[it] ?: it.toSearchValueLabel()) }
    val rows = (selectedOnly + state.results).toImmutableList()

    fun selectionOf(value: String): SelectionState? {
        return when {
            value in values -> SelectionState.Included
            "$EXCLUSION_PREFIX$value" in values -> SelectionState.Excluded
            else -> null
        }
    }

    fun setSelection(
        value: String,
        selection: SelectionState?,
    ) {
        val others = values.filterNot { it == value || it == "$EXCLUSION_PREFIX$value" }
        val token = when (selection) {
            SelectionState.Included -> value
            SelectionState.Excluded -> "$EXCLUSION_PREFIX$value"
            null -> null
        }
        onValuesChange((others + listOfNotNull(token)).toImmutableList())
    }

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(kind.headerRes),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        InputField(
            state = inputState,
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = stringResource(kind.placeholderRes),
            containerColor = TraktTheme.colors.dialogContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )

        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                overscrollEffect = null,
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(rows, key = { it.value }) { option ->
                    val selection = selectionOf(option.value)
                    SearchOptionRow(
                        option = option,
                        selection = selection,
                        onSelectionChange = { setSelection(option.value, it) },
                    )
                }
            }

            when {
                state.loading.isLoading && rows.isEmpty() -> FilmProgressIndicator(
                    size = 32.dp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
                )
                rows.isEmpty() -> Text(
                    text = stringResource(
                        when {
                            state.isQueryTooShort -> R.string.text_smart_list_filter_search_hint
                            else -> R.string.text_smart_list_filter_no_results
                        },
                    ),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 2.dp, start = 24.dp, end = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchOptionRow(
    option: SmartListSearchOption,
    selection: SelectionState?,
    onSelectionChange: (SelectionState?) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(12.dp),
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        ExclusionToggle(
            icon = R.drawable.ic_plus,
            selected = selection == SelectionState.Included,
            selectedColor = TraktTheme.colors.accent,
            onClick = {
                onSelectionChange(SelectionState.Included.takeIf { selection != it })
            },
        )

        Text(
            text = option.label,
            style = TraktTheme.typography.buttonTertiary,
            color = when (selection) {
                SelectionState.Included -> TraktTheme.colors.accent
                SelectionState.Excluded -> Red400
                null -> TraktTheme.colors.textPrimary
            },
            textDecoration = TextDecoration.LineThrough.takeIf {
                selection == SelectionState.Excluded
            },
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier.weight(1f),
        )

        ExclusionToggle(
            icon = R.drawable.ic_minus,
            selected = selection == SelectionState.Excluded,
            selectedColor = Red400,
            onClick = {
                onSelectionChange(SelectionState.Excluded.takeIf { selection != it })
            },
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListSearchView(
            kind = SmartListSearchKind.Keywords,
            state = SmartListSearchState(
                query = "time",
                results = persistentListOf(
                    SmartListSearchOption("time travel", "time travel"),
                    SmartListSearchOption("time loop", "time loop"),
                ),
            ),
            values = persistentListOf("time travel", "${EXCLUSION_PREFIX}dystopia"),
            onQueryChange = {},
            onValuesChange = {},
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewEmpty() {
    TraktThemePreview {
        SmartListSearchView(
            kind = SmartListSearchKind.People,
            state = SmartListSearchState(loading = LoadingState.Idle),
            values = persistentListOf(),
            onQueryChange = {},
            onValuesChange = {},
        )
    }
}
