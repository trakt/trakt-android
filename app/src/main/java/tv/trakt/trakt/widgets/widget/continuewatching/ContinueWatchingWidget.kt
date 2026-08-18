package tv.trakt.trakt.widgets.widget.continuewatching

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
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
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
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.widgets.WidgetIntentTarget
import tv.trakt.trakt.widgets.data.widgetAppearance
import tv.trakt.trakt.widgets.model.WidgetAppearance
import tv.trakt.trakt.widgets.model.WidgetBackground
import tv.trakt.trakt.widgets.ui.WidgetDimensions
import tv.trakt.trakt.widgets.ui.WidgetTheme
import tv.trakt.trakt.widgets.widget.continuewatching.data.ContinueWatchingWidgetDataSource
import tv.trakt.trakt.widgets.widget.continuewatching.ui.ContinueWatchingMovieView
import tv.trakt.trakt.widgets.widget.continuewatching.ui.ContinueWatchingShowView
import tv.trakt.trakt.widgets.widgetTargetIntent
import androidx.glance.appwidget.action.actionStartActivity as actionStartActivityIntent

private const val ITEMS_PER_ROW = 2

private const val MIN_ROW_COUNT = 1
private const val MAX_ROW_COUNT = 3
internal const val MAX_ITEM_COUNT = MAX_ROW_COUNT * ITEMS_PER_ROW

private val HEADER_HEIGHT = 22.dp

private val CARD_FOOTER_HEIGHT = 35.dp

private val CONTENT_PADDING_TOP = WidgetDimensions.spacingRegular - 2.dp
private val CONTENT_PADDING_BOTTOM = WidgetDimensions.spacingRegular + 1.dp

internal class ContinueWatchingWidget(
    private val dataSource: ContinueWatchingWidgetDataSource,
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
    state: ContinueWatchingWidgetState,
    appearance: WidgetAppearance,
) {
    WidgetTheme(theme = appearance.theme) {
        WidgetBody(
            state = state,
            appearance = appearance,
        )
    }
}

@Composable
private fun WidgetBody(
    state: ContinueWatchingWidgetState,
    appearance: WidgetAppearance,
) {
    val context = LocalContext.current

    val size = LocalSize.current
    val cardWidth = getCardWidth(widgetWidth = size.width)
    val itemCount = getRowCount(
        widgetHeight = size.height,
        cardWidth = cardWidth,
        titleVisible = appearance.titleVisible,
    ) * ITEMS_PER_ROW

    Column(
        modifier = GlanceModifier
            .fillMaxSize(),
    ) {
        Column(
            modifier = GlanceModifier
                .background(
                    when (appearance.background) {
                        WidgetBackground.Solid -> WidgetTheme.colors.backgroundPrimary
                        WidgetBackground.SemiTransparent -> WidgetTheme.colors.backgroundTranslucent
                        WidgetBackground.None -> WidgetTheme.colors.backgroundNone
                    },
                )
                .cornerRadius(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        android.R.dimen.system_app_widget_background_radius
                    } else {
                        R.dimen.default_widget_corner_radius
                    },
                )
                .padding(horizontal = WidgetDimensions.spacingLarge)
                .padding(
                    top = CONTENT_PADDING_TOP,
                    bottom = CONTENT_PADDING_BOTTOM,
                ),
        ) {
            if (appearance.titleVisible) {
                HeaderView(
                    modifier = GlanceModifier.fillMaxWidth(),
                )
                Spacer(
                    modifier = GlanceModifier
                        .height(WidgetDimensions.spacingSmall),
                )
            }

            Spacer(
                modifier = GlanceModifier
                    .height(WidgetDimensions.spacingMedium),
            )

            when {
                state.error -> Text(
                    text = context.getString(R.string.error_text_unexpected_error_short),
                    style = WidgetTheme.textStyles.message,
                    maxLines = 3,
                )

                state.items.isEmpty() -> Text(
                    text = context.getString(R.string.text_cta_up_next),
                    style = WidgetTheme.textStyles.message,
                    maxLines = 3,
                )

                else -> ItemsGridView(
                    items = state.items.take(itemCount),
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
        Text(
            text = context.getString(R.string.list_title_up_next),
            style = WidgetTheme.textStyles.heading,
            maxLines = 1,
            modifier = GlanceModifier
                .defaultWeight()
                .clickable(
                    actionStartActivityIntent(
                        context.widgetTargetIntent(WidgetIntentTarget.UpNext),
                    ),
                ),
        )

        Image(
            provider = ImageProvider(R.drawable.ic_trakt_icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(WidgetTheme.colors.textPrimary),
            modifier = GlanceModifier
                .size(WidgetDimensions.headerIconSize)
                .clickable(actionStartActivity(MainActivity::class.java)),
        )
    }
}

/** Glance has no arrangement spacing, so the gaps between rows come from [Spacer]. */
@Composable
private fun ItemsGridView(
    items: List<ContinueWatchingWidgetItem>,
    cardWidth: Dp,
) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        items.chunked(ITEMS_PER_ROW).forEachIndexed { index, rowItems ->
            if (index > 0) {
                Spacer(modifier = GlanceModifier.height(WidgetDimensions.spacingRegular))
            }

            ItemsRowView(
                items = rowItems,
                cardWidth = cardWidth,
            )
        }
    }
}

@Composable
private fun ItemsRowView(
    items: List<ContinueWatchingWidgetItem>,
    cardWidth: Dp,
) {
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = GlanceModifier.width(WidgetDimensions.rowSpace))
            }

            when (item) {
                is ContinueWatchingWidgetItem.Show -> ContinueWatchingShowView(
                    item = item,
                    width = cardWidth,
                )
                is ContinueWatchingWidgetItem.Movie -> ContinueWatchingMovieView(
                    item = item,
                    width = cardWidth,
                )
            }
        }
    }
}

private fun getRowCount(
    widgetHeight: Dp,
    cardWidth: Dp,
    titleVisible: Boolean,
): Int {
    // A hidden title gives its row height back to the cards.
    val header = when {
        titleVisible -> HEADER_HEIGHT + WidgetDimensions.spacingRegular
        else -> 0.dp
    }
    val chrome = CONTENT_PADDING_TOP + CONTENT_PADDING_BOTTOM + header
    val rowHeight = cardWidth / HorizontalImageAspectRatio +
        WidgetDimensions.spacingSmall + CARD_FOOTER_HEIGHT

    if (rowHeight <= 0.dp) {
        return MIN_ROW_COUNT
    }

    // Every row past the first also costs a gap, so both sides gain one to cancel it out.
    val available = widgetHeight - chrome + WidgetDimensions.spacingRegular
    val fits = available / (rowHeight + WidgetDimensions.spacingRegular)

    return fits.toInt().coerceIn(MIN_ROW_COUNT, MAX_ROW_COUNT)
}

private fun getCardWidth(widgetWidth: Dp): Dp {
    val gaps = WidgetDimensions.rowSpace * (ITEMS_PER_ROW - 1)
    val available = widgetWidth - WidgetDimensions.spacingLarge * 2 - gaps

    return (available / ITEMS_PER_ROW).coerceAtLeast(0.dp)
}
