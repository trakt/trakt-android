package tv.trakt.trakt.widgets.configuration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.ui.theme.TraktTheme

/** Matches the app's bottom-sheet idiom while the launcher shows through above it. */
private val SHEET_CORNER_RADIUS = 28.dp
private const val SCRIM_ALPHA = 0.5F

@Composable
internal fun WidgetConfigurationScreen(
    appWidgetId: Int,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WidgetConfigurationViewModel = koinViewModel {
        parametersOf(appWidgetId)
    },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = SCRIM_ALPHA)),
    ) {
        WidgetConfigurationView(
            state = state,
            onBackgroundClick = viewModel::setBackground,
            onTitleVisibleClick = viewModel::setTitleVisible,
            onDoneClick = onDone,
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .background(
                    color = TraktTheme.colors.backgroundPrimary,
                    shape = RoundedCornerShape(
                        topStart = SHEET_CORNER_RADIUS,
                        topEnd = SHEET_CORNER_RADIUS,
                    ),
                )
                .padding(24.dp),
        )
    }
}
