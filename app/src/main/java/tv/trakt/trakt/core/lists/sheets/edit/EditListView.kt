package tv.trakt.trakt.core.lists.sheets.edit

import InputField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.confirmation.ConfirmationSheet
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
            state.loadingEdit == DONE -> onListEdited()
            state.loadingDelete == DONE -> onListDeleted()
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
        onApplyClick = { name, description ->
            viewModel.editList(
                id = initialList.ids.trakt,
                name = name,
                description = description,
            )
        },
        onDeleteClick = {
            confirmationSheet = true
        },
    )

    @OptIn(ExperimentalMaterial3Api::class)
    ConfirmationSheet(
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
    onApplyClick: (String, String) -> Unit = { _, _ -> },
    onDeleteClick: () -> Unit = { },
) {
    val nameInputState = rememberTextFieldState(initialList.name)
    val descriptionInputState = rememberTextFieldState(initialList.description ?: "")

    val inputValid by remember(initialList) {
        derivedStateOf {
            val isDifferentName = initialList.name.trim() != nameInputState.text.trim()
            val isDifferentDescription = (initialList.description ?: "").trim() != descriptionInputState.text.trim()
            nameInputState.text.isNotBlank() &&
                (isDifferentName || isDifferentDescription)
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

        InputField(
            state = nameInputState,
            enabled = !state.loadingEdit.isLoading && !state.loadingDelete.isLoading,
            placeholder = stringResource(R.string.input_placeholder_lists_name),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        InputField(
            state = descriptionInputState,
            enabled = !state.loadingEdit.isLoading && !state.loadingDelete.isLoading,
            placeholder = stringResource(R.string.input_placeholder_lists_description),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.button_text_apply),
                enabled = inputValid && !state.loadingEdit.isLoading && !state.loadingDelete.isLoading,
                loading = state.loadingEdit.isLoading,
                onClick = {
                    onApplyClick(
                        nameInputState.text.toString(),
                        descriptionInputState.text.toString(),
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
            )
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
                loadingEdit = LOADING,
            ),
        )
    }
}
