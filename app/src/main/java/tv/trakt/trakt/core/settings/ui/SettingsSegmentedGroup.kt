package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SEGMENT_INNER_CORNER_DP = 4

internal class SettingsSegmentScope {
    internal val items = mutableListOf<@Composable () -> Unit>()

    fun item(content: @Composable () -> Unit) {
        items += content
    }
}

@Composable
internal fun SettingsSegmentedGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: SettingsSegmentScope.() -> Unit,
) {
    val items = SettingsSegmentScope().apply(content).items

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        title?.let {
            TraktHeader(
                title = it.uppercase(),
                titleColor = TraktTheme.colors.settingsHeader,
                titleStyle = TraktTheme.typography.heading6,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(segmentShape(index = index, count = items.size))
                        .background(TraktTheme.colors.panelCardContainer)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    item()
                }
            }
        }
    }
}

private fun segmentShape(
    index: Int,
    count: Int,
): Shape {
    val first = index == 0
    val last = index == count - 1
    val inner = CornerSize(SEGMENT_INNER_CORNER_DP.dp)

    return when {
        first && last -> DefaultCardShape
        first -> DefaultCardShape.copy(bottomStart = inner, bottomEnd = inner)
        last -> DefaultCardShape.copy(topStart = inner, topEnd = inner)
        else -> RoundedCornerShape(inner)
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SettingsSegmentedGroup(
            title = "Behavior",
        ) {
            item { SettingsTextField(text = "First") }
            item { SettingsTextField(text = "Middle", description = "With a description") }
            item { SettingsTextField(text = "Last") }
        }
    }
}
