package tv.trakt.trakt.widgets.streaks

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
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
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Alignment.Horizontal.Companion.CenterHorizontally
import androidx.glance.layout.Alignment.Vertical.Companion.CenterVertically
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.widgets.data.widgetAppearance
import tv.trakt.trakt.widgets.model.WidgetAppearance
import tv.trakt.trakt.widgets.model.WidgetBackground
import tv.trakt.trakt.widgets.streaks.data.StreaksWidgetDataSource
import tv.trakt.trakt.widgets.ui.WidgetColors
import tv.trakt.trakt.widgets.ui.WidgetColors.backgroundNone
import tv.trakt.trakt.widgets.ui.WidgetColors.backgroundPrimary
import tv.trakt.trakt.widgets.ui.WidgetColors.backgroundTranslucent
import tv.trakt.trakt.widgets.ui.WidgetDimensions
import tv.trakt.trakt.widgets.ui.WidgetTextStyles

private val FULL_MIN_WIDTH = 260.dp
private val SHORT_MAX_HEIGHT = 56.dp

private val FLAME_SIZE = 40.dp
private val FLAME_SIZE_SHORT = 24.dp

private val PILL_WIDTH = 8.dp
private val PILL_HEIGHT = 22.dp
private val PILL_HEIGHT_SHORT = 14.dp
private val PILL_RING_STROKE = 1.2.dp

internal class StreaksWidget(
    private val dataSource: StreaksWidgetDataSource,
) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        withContext(Dispatchers.IO) {
            dataSource.refresh()
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
    state: StreaksWidgetState,
    appearance: WidgetAppearance,
) {
    val context = LocalContext.current
    val isFull = LocalSize.current.width >= FULL_MIN_WIDTH
    val isShort = LocalSize.current.height < SHORT_MAX_HEIGHT

    Row(
        verticalAlignment = CenterVertically,
        horizontalAlignment = CenterHorizontally,
        modifier = GlanceModifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(
                horizontal = when {
                    isFull -> 14.dp
                    else -> WidgetDimensions.spacingSmall
                },
                vertical = when {
                    isShort -> WidgetDimensions.spacingSmall
                    else -> WidgetDimensions.spacingRegular
                },
            )
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
            .clickable(actionStartActivity(MainActivity::class.java)),
    ) {
        when {
            state.error && !state.loaded -> {
                Text(
                    text = context.getString(R.string.error_text_unexpected_error_short),
                    style = WidgetTextStyles.message,
                    maxLines = 2,
                )
            }

            else -> {
                FlameView(
                    streakDays = state.streakDays,
                    isShort = isShort,
                )

                Spacer(modifier = GlanceModifier.width(WidgetDimensions.spacingSmall))

                StreakLabelView(
                    streakDays = state.streakDays,
                    isShort = isShort,
                )

                if (isFull) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    WeekPillsView(
                        week = state.week,
                        isShort = isShort,
                        modifier = GlanceModifier.padding(end = 9.dp),
                    )
                } else {
                    Spacer(modifier = GlanceModifier.width(10.dp))
                }
            }
        }
    }
}

@Composable
private fun FlameView(
    streakDays: Int,
    isShort: Boolean,
) {
    Image(
        provider = ImageProvider(
            when {
                streakDays <= 1 -> R.drawable.ic_flame_1
                streakDays <= 7 -> R.drawable.ic_flame_2
                else -> R.drawable.ic_flame_3
            },
        ),
        contentDescription = null,
        modifier = GlanceModifier.size(
            when {
                isShort -> FLAME_SIZE_SHORT
                else -> FLAME_SIZE
            },
        ),
    )
}

@Composable
private fun StreakLabelView(
    streakDays: Int,
    isShort: Boolean,
) {
    val context = LocalContext.current

    val fullText = "${
        context.getString(
            when {
                streakDays > 1 -> R.string.text_stats_days_count
                else -> R.string.text_stats_day_count
            },
            streakDays,
        )
    } ${context.getString(R.string.text_stats_watching_streak)}"

    val minText = "${
        context.getString(
            when {
                streakDays > 1 -> R.string.text_stats_days_count
                else -> R.string.text_stats_day_count
            },
            streakDays,
        )
    }\n${context.getString(R.string.text_stats_watching_streak)}"

    Text(
        text = when {
            LocalSize.current.width >= FULL_MIN_WIDTH -> fullText
            else -> minText
        },
        style = when {
            isShort -> WidgetTextStyles.headingCompact
            else -> WidgetTextStyles.heading
        },
        maxLines = 2,
    )
}

@Composable
private fun WeekPillsView(
    week: ImmutableList<StreaksWidgetDay>,
    isShort: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalAlignment = CenterHorizontally,
        modifier = modifier,
    ) {
        week.forEach { day ->
            PillView(
                day = day,
                isShort = isShort,
            )
        }
    }
}

@Composable
private fun PillView(
    day: StreaksWidgetDay,
    isShort: Boolean,
) {
    val fill = when {
        day.today && !day.active -> backgroundPrimary
        day.future -> backgroundPrimary
        day.active -> WidgetColors.streakPillActive
        else -> WidgetColors.streakPillMissed
    }
    val pillHeight = when {
        isShort -> PILL_HEIGHT_SHORT
        else -> PILL_HEIGHT
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .width(PILL_WIDTH * 1.45F),
    ) {
        if (day.today || day.future) {
            val stroke = when {
                day.today -> PILL_RING_STROKE * 0.75F
                else -> 0.dp
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .size(
                        width = PILL_WIDTH + stroke,
                        height = pillHeight + stroke,
                    )
                    .background(
                        when {
                            day.today -> WidgetColors.streakTodayRing
                            else -> WidgetColors.streakPillMissed
                        },
                    )
                    .cornerRadius(WidgetDimensions.chipCornerRadius)
                    .padding(PILL_RING_STROKE),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(fill)
                        .cornerRadius(WidgetDimensions.chipCornerRadius),
                ) {}
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = GlanceModifier
                    .width(PILL_WIDTH)
                    .height(pillHeight)
                    .background(fill)
                    .cornerRadius(WidgetDimensions.chipCornerRadius),
            ) {}
        }
    }
}

private val previewState = StreaksWidgetState(
    streakDays = 333,
    week = persistentListOf(
        StreaksWidgetDay(active = true, today = false, future = false),
        StreaksWidgetDay(active = true, today = false, future = false),
        StreaksWidgetDay(active = false, today = false, future = false),
        StreaksWidgetDay(active = false, today = true, future = false),
        StreaksWidgetDay(active = false, today = false, future = true),
        StreaksWidgetDay(active = false, today = false, future = true),
        StreaksWidgetDay(active = false, today = false, future = true),
    ),
    loaded = true,
)

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(
    widthDp = 150,
    heightDp = 40,
)
@Composable
private fun ShortPreview() {
    WidgetContent(
        state = previewState,
        appearance = WidgetAppearance(),
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(
    widthDp = 150,
    heightDp = 70,
)
@Composable
private fun MinimalPreview() {
    WidgetContent(
        state = previewState,
        appearance = WidgetAppearance(),
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(
    widthDp = 350,
    heightDp = 80,
)
@Composable
private fun FullPreview() {
    WidgetContent(
        state = previewState,
        appearance = WidgetAppearance(),
    )
}
