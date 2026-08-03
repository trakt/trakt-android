package tv.trakt.trakt.core.lists.sheets.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults.toggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.CustomList.Privacy.Private
import tv.trakt.trakt.common.model.lists.CustomList.Privacy.Public
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CreateListView(
    viewModel: CreateListViewModel,
    onListCreated: () -> Unit = {},
    onListLimitError: () -> Unit = {},
    onError: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.loading) {
        if (state.loading == Done) {
            onListCreated()
        }
    }

    LaunchedEffect(state.listsLimitError, state.error) {
        when {
            state.error != null -> onError()
            state.listsLimitError != null -> onListLimitError()
        }
    }

    CreateListContent(
        state = state,
        onCreateClick = { name, description, privacy ->
            viewModel.createList(name, description, privacy)
        },
    )
}

@Composable
private fun CreateListContent(
    state: CreateListState,
    onCreateClick: (String, String, CustomList.Privacy) -> Unit = { _, _, _ -> },
) {
    val nameInputState = rememberTextFieldState()
    val descriptionInputState = rememberTextFieldState()

    val inputValid by remember {
        derivedStateOf {
            nameInputState.text.isNotBlank()
        }
    }

    var selectedPrivacy by remember { mutableStateOf(state.initialPrivacy) }

    LaunchedEffect(state.initialPrivacy) {
        selectedPrivacy = state.initialPrivacy
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        TraktHeader(
            title = stringResource(R.string.page_title_create_list),
            subtitle = stringResource(R.string.page_description_create_list),
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InputField(
            state = nameInputState,
            enabled = !state.loading.isLoading,
            placeholder = stringResource(R.string.input_placeholder_lists_name),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        InputField(
            state = descriptionInputState,
            enabled = !state.loading.isLoading,
            placeholder = stringResource(R.string.input_placeholder_lists_description),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            PrivacyButtons(
                enabled = !state.loading.isLoading,
                selected = selectedPrivacy,
                onSelect = { privacy ->
                    selectedPrivacy = privacy
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 21.dp),
            )

            Text(
                text = stringResource(selectedPrivacy.displayInfoRes),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta.copy(fontWeight = W400),
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier.padding(start = 1.dp),
            )
        }

        PrimaryButton(
            text = stringResource(R.string.button_text_create),
            enabled = inputValid && !state.loading.isLoading,
            loading = state.loading.isLoading,
            onClick = {
                onCreateClick(
                    nameInputState.text.toString(),
                    descriptionInputState.text.toString(),
                    selectedPrivacy,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun PrivacyButtons(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    selected: CustomList.Privacy,
    onSelect: (CustomList.Privacy) -> Unit,
) {
    Row(
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        CustomList.Privacy.entries.forEach { variant ->
            ToggleButton(
                enabled = enabled,
                checked = selected == variant,
                onCheckedChange = {
                    if (selected != variant) {
                        onSelect(variant)
                    }
                },
                colors = toggleButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TraktTheme.colors.textPrimary,
                    checkedContainerColor = TraktTheme.colors.accent,
                    checkedContentColor = TraktTheme.colors.textPrimary,
                    disabledContentColor = TraktTheme.colors.textPrimary,
                    disabledContainerColor = when {
                        selected == variant -> TraktTheme.colors.primaryButtonContainerDisabled
                        else -> Color.Transparent
                    },
                ),
                border = when {
                    selected == variant -> BorderStroke(
                        width = 0.dp,
                        color = Color.Transparent,
                    )
                    else -> BorderStroke(
                        width = 2.dp,
                        color = TraktTheme.colors.chipContainer,
                    )
                },
                shapes = when (variant) {
                    Public -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    Private -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
            ) {
                Text(
                    text = stringResource(variant.displayRes),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.buttonPrimary,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
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
        CreateListContent(state = CreateListState())
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview2() {
    TraktTheme {
        CreateListContent(
            state = CreateListState(
                loading = Loading,
            ),
        )
    }
}
