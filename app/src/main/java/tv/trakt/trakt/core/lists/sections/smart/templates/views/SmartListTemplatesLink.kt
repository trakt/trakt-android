package tv.trakt.trakt.core.lists.sections.smart.templates.views

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val POSTER_SLOTS = 3

@Composable
internal fun SmartListTemplatesLink(
    templates: ImmutableList<SmartListTemplate>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = rememberThousandsFormat(templates.size)
    val label = when (templates.size) {
        1 -> stringResource(R.string.button_text_browse_smart_list_templates_one, count)
        else -> stringResource(R.string.button_text_browse_smart_list_templates_other, count)
    }
    val posters = remember(templates) {
        templates
            .mapNotNull { it.images.getPostersUrl().firstOrNull() }
            .take(POSTER_SLOTS)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = DefaultCardShape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = TraktTheme.colors.shadowDynamicDefault,
            pressedElevation = TraktTheme.colors.shadowDynamicDefault,
            disabledElevation = TraktTheme.colors.shadowDynamicDefault,
        ),
        colors = cardColors(
            containerColor = TraktTheme.colors.customListContainer,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (posters.isNotEmpty()) {
                SmartListTemplatePosters(
                    posters = posters,
                    posterWidth = 28.dp,
                    modifier = Modifier
                        .width(56.dp)
                        .height(42.dp),
                )
            }

            Text(
                text = label,
                style = TraktTheme.typography.buttonPrimary,
                color = TraktTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1F),
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListTemplatesLink(
            templates = persistentListOf(
                SmartListTemplate(
                    id = "hidden-gems",
                    filters = SmartListFilters(
                        media = MediaMode.Movies,
                        source = SmartListSource.Discover,
                    ),
                    personal = false,
                    total = 941,
                    images = Images(),
                ),
            ),
            onClick = {},
        )
    }
}
