package tv.trakt.trakt.core.lists.sections.smart.templates.views

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SmartListTemplatesStrip(
    templates: ImmutableList<SmartListTemplate>,
    contentPadding: PaddingValues,
    onTemplateClick: (SmartListTemplate, String) -> Unit,
    onHideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
        ) {
            Text(
                text = stringResource(R.string.smart_list_templates_intro),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1F),
            )
            Text(
                text = stringResource(R.string.button_text_hide),
                style = TraktTheme.typography.buttonTertiary,
                color = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .onClick(onClick = onHideClick)
                    .padding(vertical = 12.dp),
            )
        }

        LazyRow(
            horizontalArrangement = spacedBy(TraktTheme.spacing.mainRowSpace),
            contentPadding = contentPadding,
        ) {
            items(
                items = templates,
                key = { it.id },
            ) { template ->
                SmartListTemplateCard(
                    template = template,
                    onClick = { name -> onTemplateClick(template, name) },
                    modifier = Modifier.width(320.dp),
                )
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListTemplatesStrip(
            templates = persistentListOf(
                SmartListTemplate(
                    id = "hidden-gems",
                    filters = SmartListFilters(
                        media = MediaMode.Movies,
                        source = SmartListSource.Discover,
                        ratings = persistentListOf(78),
                        votes = persistentListOf(300, 3000),
                    ),
                    personal = false,
                    total = 941,
                    images = Images(),
                ),
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
            onTemplateClick = { _, _ -> },
            onHideClick = {},
        )
    }
}
