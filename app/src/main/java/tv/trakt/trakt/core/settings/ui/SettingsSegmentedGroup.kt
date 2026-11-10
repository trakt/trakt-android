package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.segmentShape

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
                title = it,
                titleColor = TraktTheme.colors.settingsHeader,
                titleStyle = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Column(
            verticalArrangement = spacedBy(3.dp),
        ) {
            items.forEachIndexed { index, item ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = TraktTheme.colors.shadowDynamicSmall,
                            shape = segmentShape(index = index, count = items.size),
                            clip = true,
                        )
                        .background(TraktTheme.colors.panelCardContainer)
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                ) {
                    item()
                }
            }
        }
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
