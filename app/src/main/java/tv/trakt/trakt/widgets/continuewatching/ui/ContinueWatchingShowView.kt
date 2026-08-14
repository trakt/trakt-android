package tv.trakt.trakt.widgets.continuewatching.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.size
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidgetItem
import tv.trakt.trakt.widgets.continuewatching.actions.EPISODE_ID_PARAM
import tv.trakt.trakt.widgets.continuewatching.actions.ITEM_KEY_PARAM
import tv.trakt.trakt.widgets.continuewatching.actions.MarkWatchedAction
import tv.trakt.trakt.widgets.ui.WidgetColors
import tv.trakt.trakt.widgets.ui.WidgetDimensions
import tv.trakt.trakt.widgets.ui.WidgetMediaCard
import tv.trakt.trakt.widgets.ui.WidgetProgressChip
import tv.trakt.trakt.widgets.widgetTargetIntent

@Composable
internal fun ContinueWatchingShowView(
    item: ContinueWatchingWidgetItem.Show,
    width: Dp,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current

    WidgetMediaCard(
        title = item.title,
        subtitle = item.episodeText,
        image = item.image,
        width = width,
        onImageClick = actionStartActivity(context.widgetTargetIntent(item.imageTarget)),
        onTitleClick = actionStartActivity(context.widgetTargetIntent(item.titleTarget)),
        modifier = modifier,
        footerAction = {
            when {
                item.loading -> CircularProgressIndicator(
                    color = WidgetColors.textSecondary,
                    modifier = GlanceModifier.size(WidgetDimensions.checkIconSize),
                )

                item.episodeId != null -> Image(
                    provider = ImageProvider(R.drawable.ic_check_2),
                    contentDescription = context.getString(R.string.button_text_mark_as_watched),
                    colorFilter = ColorFilter.tint(WidgetColors.accent),
                    modifier = GlanceModifier
                        .size(WidgetDimensions.checkIconSize)
                        .clickable(
                            actionRunCallback<MarkWatchedAction>(
                                parameters = actionParametersOf(
                                    ITEM_KEY_PARAM to item.key,
                                    EPISODE_ID_PARAM to item.episodeId,
                                ),
                            ),
                        ),
                )
            }
        },
        chipContent = {
            WidgetProgressChip(
                startText = item.runtimeText,
                width = width - WidgetDimensions.spacingMedium,
                height = WidgetDimensions.chipHeight,
                progress = item.progress,
                endText = item.remainingEpisodesText,
            )
        },
    )
}
