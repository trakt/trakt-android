package tv.trakt.trakt.widgets.calendar

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.WidgetIntentTarget
import tv.trakt.trakt.widgets.calendar.data.CalendarWidgetDataSource
import tv.trakt.trakt.widgets.calendar.ui.CalendarItemView
import tv.trakt.trakt.widgets.data.widgetAppearance
import tv.trakt.trakt.widgets.model.WidgetAppearance
import tv.trakt.trakt.widgets.model.WidgetBackground
import tv.trakt.trakt.widgets.ui.WidgetColors
import tv.trakt.trakt.widgets.ui.WidgetColors.backgroundNone
import tv.trakt.trakt.widgets.ui.WidgetColors.backgroundPrimary
import tv.trakt.trakt.widgets.ui.WidgetColors.backgroundTranslucent
import tv.trakt.trakt.widgets.ui.WidgetDimensions
import tv.trakt.trakt.widgets.ui.WidgetTextStyles
import tv.trakt.trakt.widgets.widgetTargetIntent
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityIntent

private const val ITEMS_PER_ROW = 2

/** The whole week rides in one RemoteViews payload, so its bitmap count is capped. */
internal const val MAX_ITEM_COUNT = 70

private val TODAY_PILL_WIDTH = 3.dp
private val TODAY_PILL_HEIGHT = 16.dp
private val TODAY_PILL_SPACE = 6.dp

internal class CalendarWidget(
    private val dataSource: CalendarWidgetDataSource,
) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        withContext(Dispatchers.IO) {
            dataSource.refresh(
                context = context,
                limit = MAX_ITEM_COUNT,
            )
        }

        provideContent {
            WidgetContent(
                state = dataSource.state,
                appearance = currentState<Preferences>().widgetAppearance(),
            )
        }
    }
}

@Composable
private fun WidgetContent(
    state: CalendarWidgetState,
    appearance: WidgetAppearance,
) {
    val context = LocalContext.current
    val cardWidth = getCardWidth(widgetWidth = LocalSize.current.width)

    Column(
        modifier = GlanceModifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(
                    when (appearance.background) {
                        WidgetBackground.Solid -> backgroundPrimary
                        WidgetBackground.SemiTransparent -> backgroundTranslucent
                        WidgetBackground.None -> backgroundNone
                    },
                )
                .cornerRadius(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        android.R.dimen.system_app_widget_background_radius
                    } else {
                        R.dimen.default_widget_corner_radius
                    },
                )
                .padding(horizontal = WidgetDimensions.spacingLarge),
        ) {
            if (appearance.titleVisible) {
                HeaderView(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(top = WidgetDimensions.spacingRegular),
                )

                Spacer(
                    modifier = GlanceModifier
                        .height(WidgetDimensions.spacingMicro),
                )
            }

            Spacer(
                modifier = GlanceModifier
                    .height(2.dp),
            )

            when {
                state.error -> Text(
                    text = context.getString(R.string.error_text_unexpected_error_short),
                    style = WidgetTextStyles.message,
                    maxLines = 3,
                )

                state.days.isEmpty() -> Text(
                    text = context.getString(R.string.about_feature_description_calendar),
                    style = WidgetTextStyles.message,
                    maxLines = 3,
                )

                else -> DaysListView(
                    days = state.days,
                    cardWidth = cardWidth,
                )
            }
        }
    }
}

@Composable
private fun HeaderView(modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.Vertical.CenterVertically,
        modifier = modifier,
    ) {
        Column(
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(
                    actionStartActivityIntent(
                        context.widgetTargetIntent(WidgetIntentTarget.Calendar),
                    ),
                ),
        ) {
            Text(
                text = context.getString(R.string.page_title_calendar),
                style = WidgetTextStyles.heading,
                maxLines = 1,
            )
            Text(
                text = context.getString(R.string.text_stats_this_week),
                style = WidgetTextStyles.cardSubtitle,
                maxLines = 1,
            )
        }

        Image(
            provider = ImageProvider(R.drawable.ic_trakt_icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(WidgetColors.textPrimary),
            modifier = GlanceModifier
                .size(WidgetDimensions.headerIconSize)
                .clickable(actionStartActivity(MainActivity::class.java)),
        )
    }
}

@Composable
private fun DaysListView(
    days: List<CalendarWidgetDay>,
    cardWidth: Dp,
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = GlanceModifier.fillMaxSize(),
    ) {
        days.forEach { day ->
            item {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Spacer(modifier = GlanceModifier.height(WidgetDimensions.spacingMedium))

                    DayHeaderView(day = day)

                    Spacer(modifier = GlanceModifier.height(WidgetDimensions.spacingMedium - 2.dp))
                }
            }

            if (day.items.isEmpty()) {
                item {
                    Text(
                        text = context.getString(R.string.text_calendar_placeholder_2),
                        style = WidgetTextStyles.message,
                        maxLines = 2,
                        modifier = GlanceModifier
                            .padding(bottom = WidgetDimensions.spacingRegular),
                    )
                }
            }

            day.items.chunked(ITEMS_PER_ROW).forEach { rowItems ->
                item {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        ItemsRowView(
                            items = rowItems,
                            cardWidth = cardWidth,
                        )

                        Spacer(modifier = GlanceModifier.height(WidgetDimensions.spacingRegular))
                    }
                }
            }
        }

        if (days.isNotEmpty()) {
            item {
                Spacer(
                    modifier = GlanceModifier.height(WidgetDimensions.spacingRegular),
                )
            }
        }
    }
}

@Composable
private fun DayHeaderView(
    day: CalendarWidgetDay,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        verticalAlignment = Alignment.Vertical.CenterVertically,
        modifier = modifier,
    ) {
        if (day.isToday) {
            Box(
                modifier = GlanceModifier
                    .size(width = TODAY_PILL_WIDTH, height = TODAY_PILL_HEIGHT)
                    .background(WidgetColors.todayMarker)
                    .cornerRadius(WidgetDimensions.chipCornerRadius),
            ) {}

            Spacer(modifier = GlanceModifier.width(TODAY_PILL_SPACE))
        }

        Text(
            text = day.label,
            style = WidgetTextStyles.dayHeading,
            maxLines = 1,
            modifier = GlanceModifier.padding(bottom = 2.dp),
        )
    }
}

@Composable
private fun ItemsRowView(
    items: List<CalendarWidgetItem>,
    cardWidth: Dp,
) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = GlanceModifier.width(WidgetDimensions.rowSpace))
            }

            CalendarItemView(
                item = item,
                width = cardWidth,
            )
        }
    }
}

private fun getCardWidth(widgetWidth: Dp): Dp {
    val gaps = WidgetDimensions.rowSpace * (ITEMS_PER_ROW - 1)
    val available = widgetWidth - WidgetDimensions.spacingLarge * 2 - gaps

    return (available / ITEMS_PER_ROW).coerceAtLeast(0.dp)
}
