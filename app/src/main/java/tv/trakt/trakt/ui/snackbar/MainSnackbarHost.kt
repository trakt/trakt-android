package tv.trakt.trakt.ui.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.ui.theme.TraktTheme

internal const val SNACK_DURATION_SHORT = 2250L
internal const val SNACK_DURATION_LONG = 4500L

@Composable
internal fun MainSnackbarHost(snackbarHostState: SnackbarHostState) {
    val localBottomBarVisibility = LocalBottomBarVisibility.current

    val padding = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()
        .plus(
            when {
                localBottomBarVisibility.value -> TraktTheme.size.navigationBarHeight
                else -> 0.dp
            },
        )
        .plus(16.dp)

    SnackbarHost(snackbarHostState) {
        Box(modifier = Modifier.fillMaxSize()) {
            Snackbar(
                shape = RoundedCornerShape(12.dp),
                containerColor = TraktTheme.colors.snackbarContainer,
                contentColor = TraktTheme.colors.snackbarContent,
                modifier = Modifier
                    .padding(bottom = padding)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.BottomCenter),
            ) {
                Text(
                    text = it.visuals.message,
                    color = TraktTheme.colors.snackbarContent,
                    maxLines = 10,
                    style = TraktTheme.typography.paragraph.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                    ),
                )
            }
        }
    }
}
