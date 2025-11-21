import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldLineLimits.MultiLine
import androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization.Companion.None
import androidx.compose.ui.text.input.KeyboardType.Companion.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun InputField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(""),
    icon: Painter? = null,
    placeholder: String? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    containerColor: Color = TraktTheme.colors.inputContainer.copy(alpha = 0.8F),
    lineLimits: TextFieldLineLimits = SingleLine,
    endSlot: @Composable (() -> Unit)? = null,
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        state = state,
        enabled = enabled,
        textStyle = TraktTheme.typography.paragraph.copy(
            color = when {
                isFocused -> TraktTheme.colors.textPrimary
                state.text.isNotBlank() -> TraktTheme.colors.textPrimary
                else -> TraktTheme.colors.textSecondary
            },
        ),
        lineLimits = lineLimits,
        cursorBrush = when {
            isFocused -> SolidColor(TraktTheme.colors.textPrimary)
            else -> SolidColor(TraktTheme.colors.textSecondary)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = Text,
            capitalization = None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        decorator = { innerField ->
            Row(
                verticalAlignment = when {
                    lineLimits is MultiLine -> Alignment.Top
                    else -> Alignment.CenterVertically
                },
                modifier = Modifier
                    .background(
                        color = containerColor,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            isFocused -> TraktTheme.colors.accent
                            else -> TraktTheme.colors.chipContainer
                        },
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(12.dp),
            ) {
                if (loading) {
                    FilmProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TraktTheme.colors.textPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                }

                if (!loading && icon != null) {
                    Icon(
                        painter = icon,
                        contentDescription = "Icon",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }

                if (!placeholder.isNullOrBlank() && state.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TraktTheme.typography.paragraph.copy(
                            color = TraktTheme.colors.textSecondary,
                        ),
                        modifier = Modifier
                            .padding(
                                start = when {
                                    lineLimits is MultiLine -> 4.dp
                                    else -> 0.dp
                                },
                                top = when {
                                    lineLimits is MultiLine -> 4.dp
                                    else -> 0.dp
                                },
                            ),
                    )
                } else {
                    innerField()
                }

                if (endSlot != null) {
                    Spacer(Modifier.weight(1F))
                    endSlot()
                }
            }
        },
        modifier = modifier
            .height(height)
            .onFocusChanged {
                isFocused = it.hasFocus
            },
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 300,
)
@Composable
private fun Preview() {
    TraktTheme {
        InputField(
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 300,
)
@Composable
private fun Preview2() {
    TraktTheme {
        InputField(
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = "Search...",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 300,
)
@Composable
private fun Preview3() {
    TraktTheme {
        InputField(
            icon = painterResource(R.drawable.ic_search_off),
            placeholder = "Search...",
            loading = true,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 300,
)
@Composable
private fun PreviewFocused() {
    TraktTheme {
        val focus = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focus.requestFocus()
        }
        InputField(
            placeholder = "Search...",
            endSlot = {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Clear",
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            },
            modifier = Modifier
                .padding(16.dp)
                .focusRequester(focus),
        )
    }
}
