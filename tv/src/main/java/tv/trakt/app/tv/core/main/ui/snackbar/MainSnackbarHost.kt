package tv.trakt.app.tv.core.main.ui.snackbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import tv.trakt.app.tv.ui.theme.TraktTheme

@Composable
internal fun MainSnackbarHost(snackbarHostState: SnackbarHostState) {
    SnackbarHost(snackbarHostState) {
        Box(modifier = Modifier.fillMaxSize()) {
            Snackbar(
                shape = RoundedCornerShape(12.dp),
                containerColor = TraktTheme.colors.snackbarContainer,
                contentColor = TraktTheme.colors.snackbarContent,
                modifier = Modifier
                    .widthIn(max = 380.dp)
                    .padding(bottom = TraktTheme.spacing.mainContentVerticalSpace + 8.dp)
                    .align(Alignment.BottomCenter),
            ) {
                Text(
                    text = it.visuals.message,
                    color = TraktTheme.colors.snackbarContent,
                    style = TraktTheme.typography.heading6.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W500,
                    ),
                )
            }
        }
    }
}
