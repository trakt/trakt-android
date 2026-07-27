@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.comments.features.report

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.helpers.LaunchedUpdateEffect
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.comments.features.report.model.ReportReason
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.InputField
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ReportCommentView(
    viewModel: ReportCommentViewModel,
    modifier: Modifier = Modifier,
    onReported: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedUpdateEffect(state.reported) {
        if (state.reported) {
            onReported()
        }
    }

    ViewContent(
        state = state,
        modifier = modifier,
        onSubmitClick = { reason, message ->
            viewModel.report(reason = reason, message = message)
        },
        onErrorClick = viewModel::clearError,
    )
}

@Composable
private fun ViewContent(
    state: ReportCommentState,
    modifier: Modifier = Modifier,
    onSubmitClick: (ReportReason, String) -> Unit = { _, _ -> },
    onErrorClick: () -> Unit = {},
) {
    val messageState = rememberTextFieldState()
    var selectedReason by remember { mutableStateOf<ReportReason?>(null) }

    val isLoading = state.loading.isLoading
    val isValid = selectedReason != null && messageState.text.isNotBlank()

    Column(
        verticalArrangement = spacedBy(12.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.dialog_title_report),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        ReasonField(
            selected = selectedReason,
            enabled = !isLoading,
            onSelect = { selectedReason = it },
            modifier = Modifier.fillMaxWidth(),
        )

        InputField(
            state = messageState,
            enabled = !isLoading,
            placeholder = stringResource(R.string.input_placeholder_report_message),
            containerColor = Color.Transparent,
            lineLimits = TextFieldLineLimits.MultiLine(
                minHeightInLines = 4,
                maxHeightInLines = 8,
            ),
            imeAction = ImeAction.Default,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.error != null) {
            Text(
                text = stringResource(state.error),
                color = Red500,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 5,
                overflow = Ellipsis,
                modifier = Modifier.onClick(onClick = onErrorClick),
            )
        }

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            PrimaryButton(
                text = when {
                    isLoading -> ""
                    else -> stringResource(R.string.button_text_submit_report)
                },
                enabled = !isLoading && isValid,
                loading = isLoading,
                onClick = {
                    val reason = selectedReason ?: return@PrimaryButton
                    onSubmitClick(reason, messageState.text.toString().trim())
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ReasonField(
    selected: ReportReason?,
    enabled: Boolean,
    onSelect: (ReportReason) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 2.dp,
                    color = TraktTheme.colors.chipContainer,
                    shape = RoundedCornerShape(16.dp),
                )
                .onClick(enabled = enabled) { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = when (selected) {
                    null -> stringResource(R.string.input_placeholder_report_reason)
                    else -> stringResource(selected.labelRes)
                },
                style = TraktTheme.typography.paragraph,
                color = when (selected) {
                    null -> TraktTheme.colors.textSecondary
                    else -> TraktTheme.colors.textPrimary
                },
                maxLines = 1,
                overflow = Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
        ) {
            ReportReason.entries.forEach { reason ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(reason.labelRes),
                            style = TraktTheme.typography.buttonTertiary,
                            color = TraktTheme.colors.textPrimary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    onClick = {
                        onSelect(reason)
                        expanded = false
                    },
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
        ViewContent(
            state = ReportCommentState(),
            modifier = Modifier.padding(24.dp),
        )
    }
}
