package tv.trakt.trakt.core.lists.sections.smart.templates.views

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.rememberThousandsFormat
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.lists.SmartListFilters
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import tv.trakt.trakt.core.lists.sections.smart.templates.model.SmartListTemplateCopy
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio
import java.util.Locale

private val PosterWidth = 52.dp

private const val POSTER_SLOTS = 3
private const val CHIP_ROWS = 2
private const val CHIPS_SLOT = "chips"
private const val MORE_SLOT = "more_"
private val ChipSpacing = 4.dp

@Composable
internal fun SmartListTemplateCard(
    template: SmartListTemplate,
    onClick: (name: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val copy = remember(template.id) { SmartListTemplateCopy.fromId(template.id) } ?: return
    val name = stringResource(copy.nameRes)

    Card(
        onClick = { onClick(name) },
        modifier = modifier,
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
            horizontalArrangement = spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 18.dp),
        ) {
            val posters = remember(template.images) {
                template.images.getPostersUrl()
            }
            SmartListTemplatePosters(
                posters = List(POSTER_SLOTS) { posters.getOrNull(it) },
                posterWidth = PosterWidth,
                modifier = Modifier.size(
                    width = PosterWidth * (POSTER_SLOTS + 1) / 2,
                    height = PosterWidth / VerticalImageAspectRatio,
                ),
            )

            SmartListTemplateText(
                template = template,
                copy = copy,
                name = name,
                modifier = Modifier.weight(1F),
            )
        }
    }
}

@Composable
private fun SmartListTemplateText(
    template: SmartListTemplate,
    copy: SmartListTemplateCopy,
    name: String,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val configuration = LocalConfiguration.current
    val chips = remember(template.filters, copy, configuration) {
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        copy.toChipLabels(template.filters, resources, locale)
    }

    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        if (template.personal) {
            InfoChip(
                text = stringResource(R.string.tag_text_smart_list_template_personal),
                containerColor = TraktTheme.colors.accent,
                contentColor = TraktTheme.colors.textPrimaryOnAccent,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        Text(
            text = name,
            style = TraktTheme.typography.cardTitle.copy(fontSize = 16.sp, letterSpacing = 0.1.sp),
            color = TraktTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(bottom = 1.dp),
        )

        Text(
            text = stringResource(copy.pitchRes),
            style = TraktTheme.typography.cardSubtitle.copy(fontSize = 12.sp),
            color = TraktTheme.colors.textSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        SmartListTemplateChips(
            chips = chips,
            modifier = Modifier.padding(vertical = 10.dp),
        )

        SmartListTemplateFooter(template = template)
    }
}

@Composable
private fun SmartListTemplateChips(
    chips: List<String>,
    modifier: Modifier = Modifier,
) {
    if (chips.isEmpty()) return

    SubcomposeLayout(modifier = modifier) { constraints ->
        val spacing = ChipSpacing.roundToPx()
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        val chipPlaceables = subcompose(CHIPS_SLOT) {
            chips.forEach { SmartListTemplateChip(text = it) }
        }.map { it.measure(looseConstraints) }

        fun fits(items: List<Placeable>): Boolean {
            return chipRows(items, constraints.maxWidth, spacing).size <= CHIP_ROWS
        }

        val visible = chipPlaceables.takeIf(::fits)
            ?: (chipPlaceables.lastIndex downTo 0).firstNotNullOf { count ->
                val hidden = chipPlaceables.size - count
                val more = subcompose("$MORE_SLOT$hidden") {
                    SmartListTemplateChip(text = "+$hidden")
                }.first().measure(looseConstraints)

                (chipPlaceables.take(count) + more).takeIf { count == 0 || fits(it) }
            }

        val rows = chipRows(visible, constraints.maxWidth, spacing)
        val rowHeight = visible.maxOf { it.height }
        val width = rows.maxOf { row -> row.sumOf { it.width } + spacing * (row.size - 1) }
        val height = rowHeight * rows.size + spacing * (rows.size - 1)

        layout(width, height) {
            rows.forEachIndexed { rowIndex, row ->
                var x = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x, rowIndex * (rowHeight + spacing))
                    x += placeable.width + spacing
                }
            }
        }
    }
}

@Composable
private fun SmartListTemplateChip(text: String) {
    InfoChip(
        text = text,
        contentColor = TraktTheme.colors.textPrimary,
    )
}

private fun chipRows(
    items: List<Placeable>,
    maxWidth: Int,
    spacing: Int,
): List<List<Placeable>> {
    return items.fold(mutableListOf<MutableList<Placeable>>()) { rows, item ->
        val row = rows.lastOrNull()
        val rowWidth = row?.sumOf { it.width + spacing } ?: 0
        if (row == null || rowWidth + item.width > maxWidth) {
            rows.add(mutableListOf(item))
        } else {
            row.add(item)
        }
        rows
    }
}

@Composable
private fun SmartListTemplateFooter(template: SmartListTemplate) {
    val source = stringResource(template.filters.source.displayRes)
    val media = stringResource(template.filters.media.displayRes)

    Row(
        horizontalArrangement = spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "$source • $media",
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1F),
        )
        template.total?.let { total ->
            Text(
                text = stringResource(R.string.text_smart_list_template_total, rememberThousandsFormat(total)),
                style = TraktTheme.typography.meta,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
            )
        }
    }
}

@Composable
internal fun SmartListTemplatePosters(
    posters: List<String?>,
    posterWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier,
    ) {
        val offset = posterWidth / 2
        posters.forEachIndexed { index, url ->
            VerticalMediaCard(
                title = "",
                imageUrl = url,
                width = posterWidth,
                corner = 8.dp,
                enabled = false,
                more = false,
                modifier = Modifier
                    .padding(start = offset * index)
                    .width(posterWidth)
                    .height(posterWidth / VerticalImageAspectRatio)
                    .graphicsLayer {
                        rotationZ = (index - 1) * 6F
                    },
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Column(verticalArrangement = spacedBy(16.dp)) {
                SmartListTemplateCard(
                    template = SmartListTemplate(
                        id = "hidden-gems",
                        filters = SmartListFilters(
                            media = MediaMode.Movies,
                            source = SmartListSource.Discover,
                            ratings = persistentListOf(78),
                            votes = persistentListOf(300, 3000),
                            runtimes = persistentListOf(80),
                        ),
                        personal = false,
                        total = 941,
                        images = Images(
                            posters = persistentListOf(
                                "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                                "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                                "https://trakt.tv/images/posters/000/000/001/thumb/1.jpg",
                            ),
                        ),
                    ),
                    onClick = {},
                    modifier = Modifier.width(320.dp),
                )
                SmartListTemplateCard(
                    template = SmartListTemplate(
                        id = "next-obsession",
                        filters = SmartListFilters(
                            media = MediaMode.Movies,
                            source = SmartListSource.Recommendations,
                            ignoreWatchlisted = true,
                        ),
                        personal = true,
                        total = null,
                        images = Images(),
                    ),
                    onClick = {},
                    modifier = Modifier.width(320.dp),
                )
            }
        }
    }
}
