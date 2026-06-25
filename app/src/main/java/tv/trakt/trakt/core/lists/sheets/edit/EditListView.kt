package tv.trakt.trakt.core.lists.sheets.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.CustomList.Privacy.Private
import tv.trakt.trakt.common.model.CustomList.Privacy.Public
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EditListView(
    initialList: CustomList,
    viewModel: EditListViewModel,
    onListEdited: () -> Unit = {},
    onListDeleted: () -> Unit = {},
    onError: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var confirmationSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.loadingEdit, state.loadingDelete) {
        when {
            state.loadingEdit == Done -> onListEdited()
            state.loadingDelete == Done -> onListDeleted()
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            onError()
        }
    }

    EditListContent(
        state = state,
        initialList = initialList,
        onApplyClick = { name, description, privacy ->
            viewModel.editList(
                id = initialList.ids.trakt,
                name = name,
                description = description,
                privacy = privacy,
            )
        },
        onDeleteClick = {
            confirmationSheet = true
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    RemoveConfirmationSheet(
        active = confirmationSheet,
        onYes = {
            confirmationSheet = false
            viewModel.deleteList(id = initialList.ids.trakt)
        },
        onNo = { confirmationSheet = false },
        title = stringResource(R.string.button_text_delete_list),
        message = stringResource(R.string.warning_prompt_delete_list, initialList.name),
    )
}

@Composable
private fun EditListContent(
    state: EditListState,
    initialList: CustomList,
    onApplyClick: (String, String, CustomList.Privacy) -> Unit = { _, _, _ -> },
    onDeleteClick: () -> Unit = { },
) {
    val nameInputState = rememberTextFieldState(initialList.name)
    val descriptionInputState = rememberTextFieldState(initialList.description ?: "")

    val initialPrivacy = initialList.privacy ?: Public
    var selectedPrivacy by remember(initialList) { mutableStateOf(initialPrivacy) }

    val inputValid by remember(initialList) {
        derivedStateOf {
            val isDifferentName = initialList.name.trim() != nameInputState.text.trim()
            val isDifferentDescription = (initialList.description ?: "").trim() != descriptionInputState.text.trim()
            val isDifferentPrivacy = initialPrivacy != selectedPrivacy
            nameInputState.text.isNotBlank() &&
                (isDifferentName || isDifferentDescription || isDifferentPrivacy)
        }
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
        ) {
            TraktHeader(
                title = stringResource(R.string.page_title_edit_list),
                subtitle = stringResource(R.string.page_description_edit_list),
            )
            if (state.loadingDelete.isLoading) {
                FilmProgressIndicator(
                    size = 22.dp,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_trash),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .size(22.dp)
                        .onClick { onDeleteClick() },
                )
            }
        }

        val isLoadingOrDone =
            (state.loadingEdit.isLoading || state.loadingEdit.isDone) ||
                (state.loadingDelete.isLoading || state.loadingDelete.isDone)

        InputField(
            state = nameInputState,
            enabled = !isLoadingOrDone,
            placeholder = stringResource(R.string.input_placeholder_lists_name),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        InputField(
            state = descriptionInputState,
            enabled = !isLoadingOrDone,
            placeholder = stringResource(R.string.input_placeholder_lists_description),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            verticalArrangement = spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            PrivacyButtons(
                enabled = !isLoadingOrDone,
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

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.button_text_apply),
                enabled = inputValid && !isLoadingOrDone,
                loading = state.loadingEdit.isLoading,
                onClick = {
                    onApplyClick(
                        nameInputState.text.toString(),
                        descriptionInputState.text.toString(),
                        selectedPrivacy,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )
        }
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
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        EditListContent(
            state = EditListState(),
            initialList = PreviewData.customList1,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        EditListContent(
            initialList = PreviewData.customList1,
            state = EditListState(
                loadingEdit = Loading,
            ),
        )
    }
}
