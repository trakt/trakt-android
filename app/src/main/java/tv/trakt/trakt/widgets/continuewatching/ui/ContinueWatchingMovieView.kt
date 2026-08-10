package tv.trakt.trakt.widgets.continuewatching.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import tv.trakt.trakt.widgets.continuewatching.ContinueWatchingWidgetItem
import tv.trakt.trakt.widgets.ui.WidgetDimensions
import tv.trakt.trakt.widgets.ui.WidgetMediaCard
import tv.trakt.trakt.widgets.ui.WidgetProgressChip
import tv.trakt.trakt.widgets.widgetTargetIntent

@Composable
internal fun ContinueWatchingMovieView(
    item: ContinueWatchingWidgetItem.Movie,
    width: Dp,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current

    WidgetMediaCard(
        title = item.title,
        subtitle = item.runtimeText,
        image = item.image,
        width = width,
        onImageClick = actionStartActivity(context.widgetTargetIntent(item.imageTarget)),
        onTitleClick = actionStartActivity(context.widgetTargetIntent(item.titleTarget)),
        modifier = modifier,
        chipContent = {
            WidgetProgressChip(
                startText = item.remainingTimeText,
                width = width - WidgetDimensions.spacingMedium,
                height = WidgetDimensions.chipHeight,
                progress = item.progress,
            )
        },
    )
}
