package tv.trakt.trakt.widgets.calendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.width
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.calendar.CalendarWidgetItem
import tv.trakt.trakt.widgets.calendar.CalendarWidgetTag
import tv.trakt.trakt.widgets.ui.WidgetColors
import tv.trakt.trakt.widgets.ui.WidgetInfoChip
import tv.trakt.trakt.widgets.ui.WidgetMediaCard
import tv.trakt.trakt.widgets.ui.WidgetStatusChip
import tv.trakt.trakt.widgets.widgetTargetIntent

// Matches the 3dp gap between the status and time chips on the app's calendar cards.
private val CHIP_SPACE = 3.dp

@Composable
internal fun CalendarItemView(
    item: CalendarWidgetItem,
    width: Dp,
    modifier: GlanceModifier = GlanceModifier,
) {
    val context = LocalContext.current

    WidgetMediaCard(
        title = item.title,
        subtitle = item.subtitle,
        image = item.image,
        width = width,
        onImageClick = actionStartActivity(context.widgetTargetIntent(item.imageTarget)),
        onTitleClick = actionStartActivity(context.widgetTargetIntent(item.titleTarget)),
        modifier = modifier,
        titleIconRes = when {
            item.isMovie -> R.drawable.ic_movies_off
            else -> R.drawable.ic_shows_off
        },
        chipContent = {
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                when (item.tag) {
                    CalendarWidgetTag.Premiere -> {
                        WidgetStatusChip(
                            text = context.getString(R.string.tag_text_premiere),
                            dotColor = WidgetColors.premiereDot,
                        )

                        Spacer(modifier = GlanceModifier.width(CHIP_SPACE))
                    }

                    CalendarWidgetTag.Finale -> {
                        WidgetStatusChip(
                            text = context.getString(R.string.tag_text_finale),
                            dotColor = WidgetColors.finaleDot,
                        )

                        Spacer(modifier = GlanceModifier.width(CHIP_SPACE))
                    }

                    null -> {}
                }

                item.timeText?.let { timeText ->
                    WidgetInfoChip(text = timeText)
                }
            }
        },
    )
}
