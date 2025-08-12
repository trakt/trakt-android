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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.common.ui.FilmProgressIndicator
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.R as RCommon

@Composable
internal fun TraktTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState = rememberTextFieldState(""),
    icon: Painter? = null,
    placeholder: String? = null,
    loading: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }

    BasicTextField(
        state = state,
        textStyle = TraktTheme.typography.paragraph.copy(
            color = when {
                isFocused -> TraktTheme.colors.textPrimary
                else -> TraktTheme.colors.textSecondary
            },
        ),
        lineLimits = SingleLine,
        cursorBrush = when {
            isFocused -> SolidColor(TraktTheme.colors.textPrimary)
            else -> SolidColor(TraktTheme.colors.textSecondary)
        },
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            imeAction = ImeAction.Done,
        ),
        decorator = { innerField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = TraktTheme.colors.inputContainer.copy(alpha = 0.8F),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        width = 2.dp,
                        color = when {
                            isFocused -> TraktTheme.colors.accent
                            else -> TraktTheme.colors.textSecondary
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
                    )
                } else {
                    innerField()
                }
            }
        },
        modifier = modifier
            .height(48.dp)
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
        TraktTextField(
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
        TraktTextField(
            icon = painterResource(RCommon.drawable.ic_search),
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
        TraktTextField(
            icon = painterResource(RCommon.drawable.ic_search),
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
        TraktTextField(
            placeholder = "Search...",
            modifier = Modifier
                .padding(16.dp)
                .focusRequester(focus),
        )
    }
}
