@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.filters

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktDark
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLight
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode.Advanced
import tv.trakt.trakt.common.model.globalfilter.GlobalFilterMode.Simple
import tv.trakt.trakt.core.filters.views.GlobalFiltersAdvancedView
import tv.trakt.trakt.core.filters.views.GlobalFiltersSimpleView
import tv.trakt.trakt.helpers.extensions.TraktThemeLight
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

internal val RowsSpacing = 8.dp
internal val ColumnsSpacing = 8.dp

@Composable
internal fun GlobalFiltersView(
    viewModel: GlobalFiltersViewModel,
    onUpdate: (GlobalFilter) -> Unit = { _ -> },
) {
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmationSheet by remember { mutableStateOf(false) }

    state.mode?.let {
        when (it) {
            Simple -> {
                GlobalFiltersSimpleView(
                    state = state,
                    onToggleMode = viewModel::applyMode,
                    onUpdateFilter = { filter, delay ->
                        scope.launch {
                            delay(if (delay) 100.milliseconds else 0.milliseconds)
                            viewModel.applyFilter(filter)
                            onUpdate(filter)
                        }
                    },
                    onResetFilter = {
                        scope.launch {
                            viewModel.applyFilter(
                                GlobalFilter.Default.copy(
                                    mode = state.filter.mode,
                                ),
                            )
                            onUpdate(GlobalFilter.Default)
                        }
                    },
                )
            }
            Advanced -> {
                GlobalFiltersAdvancedView(
                    state = state,
                    onToggleMode = { mode ->
                        if (state.filter.isAdvancedActive) {
                            confirmationSheet = true
                        } else {
                            viewModel.applyMode(mode)
                        }
                    },
                    onUpdateFilter = { filter, delay ->
                        scope.launch {
                            delay(if (delay) 100.milliseconds else 0.milliseconds)
                            viewModel.applyFilter(filter)
                            onUpdate(filter)
                        }
                    },
                    onResetFilter = {
                        scope.launch {
                            viewModel.applyFilter(
                                GlobalFilter.Default.copy(
                                    mode = state.filter.mode,
                                ),
                            )
                            onUpdate(GlobalFilter.Default)
                        }
                    },
                )
            }
        }
    }

    RemoveConfirmationSheet(
        active = confirmationSheet,
        onYes = {
            viewModel.applyMode(Simple)
            confirmationSheet = false
        },
        onNo = { confirmationSheet = false },
        title = stringResource(R.string.header_filters),
        message = stringResource(R.string.warning_prompt_simple_filters),
    )
}

@PreviewTraktDark
@Composable
private fun PreviewSimple() {
    TraktTheme {
        GlobalFiltersSimpleView(state = GlobalFiltersState())
    }
}

@PreviewTraktLight
@Composable
private fun PreviewSimpleLight() {
    TraktThemeLight {
        GlobalFiltersSimpleView(state = GlobalFiltersState())
    }
}

@PreviewTraktDark
@Composable
private fun PreviewAdvanced() {
    TraktTheme {
        GlobalFiltersAdvancedView(state = GlobalFiltersState())
    }
}

@PreviewTraktLight
@Composable
private fun PreviewAdvancedLight() {
    TraktThemeLight {
        GlobalFiltersAdvancedView(state = GlobalFiltersState())
    }
}
