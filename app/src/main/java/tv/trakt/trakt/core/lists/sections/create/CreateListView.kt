package tv.trakt.trakt.core.lists.sections.create

import InputField
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.resources.R
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
        if (state.loading == DONE) {
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
        onCreateClick = { name, description ->
            viewModel.createList(name, description)
        },
    )
}

@Composable
private fun CreateListContent(
    state: CreateListState,
    onCreateClick: (String, String) -> Unit = { _, _ -> },
) {
    val nameInputState = rememberTextFieldState()
    val descriptionInputState = rememberTextFieldState()

    val inputValid by remember {
        derivedStateOf {
            nameInputState.text.isNotBlank()
        }
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace)
            .padding(bottom = 32.dp),
    ) {
        Column(
            modifier = Modifier.padding(bottom = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.page_title_create_list),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading5,
            )
            Text(
                text = stringResource(R.string.page_subtitle_create_list),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta.copy(fontWeight = W400),
            )
        }

        InputField(
            state = nameInputState,
            placeholder = stringResource(R.string.input_placeholder_lists_name),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        InputField(
            state = descriptionInputState,
            placeholder = stringResource(R.string.input_placeholder_lists_description),
            containerColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth(),
        )

        PrimaryButton(
            text = stringResource(R.string.button_text_create),
            enabled = inputValid && !state.loading.isLoading,
            loading = state.loading.isLoading,
            onClick = {
                onCreateClick(
                    nameInputState.text.toString(),
                    descriptionInputState.text.toString(),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        )
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
        CreateListContent(state = CreateListState())
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
        CreateListContent(
            state = CreateListState(
                loading = LOADING,
            ),
        )
    }
}
