package tv.trakt.trakt.app.core.details.ratings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Surface
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator

private val SurfaceShape = RoundedCornerShape(12.dp)

internal val RatingIconSize = 24.dp

/**
 * Transparent focusable surface with a white focus outline. While [loading], the content keeps
 * its space but is hidden behind a progress indicator.
 */
@Composable
internal fun RatingSurface(
    loading: Boolean,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = {
            if (!loading) onClick()
        },
        shape = ClickableSurfaceDefaults.shape(shape = SurfaceShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            contentColor = TraktTheme.colors.textPrimary,
            focusedContentColor = TraktTheme.colors.textPrimary,
            pressedContentColor = TraktTheme.colors.textPrimary,
        ),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(width = 2.75.dp, color = TraktTheme.colors.primaryButtonContent),
                shape = SurfaceShape,
            ),
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1F),
        modifier = modifier,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .padding(contentPadding)
                    .alpha(if (loading) 0F else 1F),
            ) {
                content()
            }

            if (loading) {
                FilmProgressIndicator(
                    size = 18.dp,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        }
    }
}
