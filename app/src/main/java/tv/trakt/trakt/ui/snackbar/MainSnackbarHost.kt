package tv.trakt.trakt.ui.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.LocalCheckInVisibility
import tv.trakt.trakt.LocalRatePromptVisibility
import tv.trakt.trakt.ui.theme.TraktTheme

internal const val SNACK_DURATION_SHORT = 2500L
internal const val SNACK_DURATION_LONG = 4500L

@Composable
internal fun MainSnackbarHost(
    snackbarHostState: SnackbarHostState,
    checkInVisible: Boolean,
    ratePromptVisible: Boolean,
) {
    val localBottomBarVisibility = LocalBottomBarVisibility.current
    val localCheckInVisibility = LocalCheckInVisibility.current
    val localRatePromptVisibility = LocalRatePromptVisibility.current

    val padding = WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding()
        .plus(12.dp)
        .plus(
            when {
                localBottomBarVisibility.value -> TraktTheme.size.navigationBarHeight
                else -> 0.dp
            },
        )
        .plus(
            when {
                checkInVisible && localCheckInVisibility.value -> TraktTheme.size.navigationPromptHeight
                else -> 0.dp
            },
        )
        .plus(
            when {
                ratePromptVisible && localRatePromptVisibility.value -> TraktTheme.size.navigationPromptHeight
                else -> 0.dp
            },
        )

    val currentSnackbarData = snackbarHostState.currentSnackbarData
    var snackbarDataToShow by remember { mutableStateOf<SnackbarData?>(null) }
    val initialOffset = LocalDensity.current.run { 12.dp.roundToPx() }

    LaunchedEffect(currentSnackbarData) {
        if (currentSnackbarData != null) {
            snackbarDataToShow = currentSnackbarData
            delay(
                when (currentSnackbarData.visuals.duration) {
                    SnackbarDuration.Long -> SNACK_DURATION_LONG
                    else -> SNACK_DURATION_SHORT
                },
            )
            currentSnackbarData.dismiss()
        }
    }

    AnimatedVisibility(
        visible = currentSnackbarData != null,
        enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { initialOffset }),
        exit = fadeOut(tween(100)),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            snackbarDataToShow?.let { data ->
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
                        text = data.visuals.message,
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
}
