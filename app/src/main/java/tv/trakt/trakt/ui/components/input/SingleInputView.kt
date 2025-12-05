package tv.trakt.trakt.ui.components.input

import InputField
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SingleInputView(
    title: String,
    description: String? = null,
    initialInput: String? = null,
    nullable: Boolean = false,
    multiline: Boolean = false,
    onApply: (String?) -> Unit,
) {
    SingleInputContent(
        title = title,
        description = description,
        initialInput = initialInput,
        nullable = nullable,
        multiline = multiline,
        onApplyClick = onApply,
    )
}

@Composable
private fun SingleInputContent(
    title: String,
    description: String? = null,
    initialInput: String? = null,
    nullable: Boolean = false,
    multiline: Boolean = false,
    onApplyClick: (String?) -> Unit = {},
) {
    val nameInputState = rememberTextFieldState(
        initialText = initialInput ?: "",
    )

    val inputValid by remember(nullable) {
        derivedStateOf {
            nullable || nameInputState.text.isNotBlank()
        }
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        TraktHeader(
            title = title,
            subtitle = description,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        InputField(
            state = nameInputState,
            containerColor = Color.Transparent,
            lineLimits = when {
                multiline -> TextFieldLineLimits.MultiLine(
                    minHeightInLines = 5,
                    maxHeightInLines = 10,
                )

                else -> TextFieldLineLimits.SingleLine
            },
            modifier = Modifier.fillMaxWidth(),
        )

        PrimaryButton(
            text = "OK",
            enabled = inputValid,
            onClick = {
                val inputText = nameInputState.text.ifBlank { "" }
                onApplyClick(inputText.toString())
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        SingleInputContent(
            title = "Single Input Title",
            description = "This is a description for the single input view.",
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        SingleInputContent(
            title = "Single Input Title",
            nullable = true,
            multiline = true,
            initialInput = "Initial value",
        )
    }
}
