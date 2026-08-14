package tv.trakt.trakt.widgets.ui

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.action
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio

/** Below this the fill reads as a stray nub rather than progress. Matches `EpisodeProgressBar`. */
private const val MIN_VISIBLE_PROGRESS = 0.1F

private val TITLE_ICON_SIZE = 11.5.dp

@Composable
internal fun WidgetMediaCard(
    title: String,
    subtitle: String,
    image: Bitmap?,
    width: Dp,
    onImageClick: Action,
    onTitleClick: Action,
    modifier: GlanceModifier = GlanceModifier,
    @DrawableRes titleIconRes: Int? = null,
    footerAction: (@Composable () -> Unit)? = null,
    chipContent: @Composable () -> Unit,
) {
    Column(modifier = modifier.width(width)) {
        Box(
            contentAlignment = Alignment.BottomStart,
            modifier = GlanceModifier
                .width(width)
                .height(width / HorizontalImageAspectRatio)
                .background(WidgetColors.placeholderContainer)
                .cornerRadius(WidgetDimensions.cardCornerRadius)
                .clickable(onImageClick),
        ) {
            if (image != null) {
                Image(
                    provider = ImageProvider(image),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = GlanceModifier.fillMaxSize(),
                )
            }

            Box(modifier = GlanceModifier.padding(WidgetDimensions.spacingSmall)) {
                chipContent()
            }
        }

        Spacer(modifier = GlanceModifier.height(WidgetDimensions.spacingSmall))

        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            modifier = GlanceModifier.fillMaxWidth(),
        ) {
            Column(
                modifier = GlanceModifier
                    .defaultWeight()
                    .clickable(onTitleClick),
            ) {
                Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                    if (titleIconRes != null) {
                        Image(
                            provider = ImageProvider(titleIconRes),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(WidgetColors.chipContent),
                            modifier = GlanceModifier.size(TITLE_ICON_SIZE),
                        )

                        Spacer(modifier = GlanceModifier.width(WidgetDimensions.spacingSmall))
                    }

                    Text(
                        text = title,
                        style = WidgetTextStyles.cardTitle,
                        maxLines = 1,
                    )
                }

                Text(
                    text = subtitle,
                    style = WidgetTextStyles.cardSubtitle,
                    maxLines = 1,
                )
            }

            footerAction?.let { action ->
                Box(
                    modifier = GlanceModifier.padding(
                        start = WidgetDimensions.spacingRegular,
                        end = 2.dp,
                    ),
                ) {
                    action()
                }
            }
        }
    }
}

@Composable
internal fun WidgetProgressChip(
    startText: String,
    width: Dp,
    height: Dp,
    progress: Float,
    modifier: GlanceModifier = GlanceModifier,
    endText: String? = null,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .width(width)
            .height(height)
            .background(WidgetColors.chipContainer)
            .cornerRadius(WidgetDimensions.chipCornerRadius)
            .padding(horizontal = 3.dp),
    ) {
        if (progress > MIN_VISIBLE_PROGRESS) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = GlanceModifier
                    .width((width * progress.coerceAtMost(1F)))
                    .height(WidgetDimensions.chipHeight - 5.dp)
                    .background(WidgetColors.chipProgressTrack)
                    .cornerRadius(WidgetDimensions.chipCornerRadius),
            ) {}
        }

        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
        ) {
            Text(
                text = startText,
                style = WidgetTextStyles.meta,
                maxLines = 1,
            )

            if (endText != null) {
                Spacer(modifier = GlanceModifier.defaultWeight())

                Text(
                    text = endText,
                    style = WidgetTextStyles.meta,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Wrap-content pill for short static labels (release time, tags). */
@Composable
internal fun WidgetInfoChip(
    text: String,
    modifier: GlanceModifier = GlanceModifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(WidgetDimensions.chipHeight)
            .background(WidgetColors.chipContainer)
            .cornerRadius(WidgetDimensions.chipCornerRadius)
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = text,
            style = WidgetTextStyles.meta,
            maxLines = 1,
        )
    }
}

private val STATUS_DOT_SIZE = 7.5.dp

/** Wrap-content pill with a coloured status dot; mirrors the app's `StatusChip`. */
@Composable
internal fun WidgetStatusChip(
    text: String,
    dotColor: ColorProvider,
    modifier: GlanceModifier = GlanceModifier,
) {
    Row(
        verticalAlignment = Alignment.Vertical.CenterVertically,
        modifier = modifier
            .height(WidgetDimensions.chipHeight)
            .background(WidgetColors.chipContainer)
            .cornerRadius(WidgetDimensions.chipCornerRadius)
            .padding(horizontal = 6.dp),
    ) {
        Box(
            modifier = GlanceModifier
                .size(STATUS_DOT_SIZE)
                .background(dotColor)
                .cornerRadius(WidgetDimensions.chipCornerRadius),
        ) {}

        Spacer(modifier = GlanceModifier.width(WidgetDimensions.spacingSmall))

        Text(
            text = text,
            style = WidgetTextStyles.meta,
            maxLines = 1,
        )
    }
}

private val PREVIEW_CARD_WIDTH = 160.dp

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 132)
@Composable
private fun InfoChipCardPreview() {
    WidgetMediaCard(
        title = "The Pitt",
        subtitle = "S1 • E4 - 10:00 A.M.",
        image = null,
        width = PREVIEW_CARD_WIDTH,
        onImageClick = action {},
        onTitleClick = action {},
        titleIconRes = R.drawable.ic_shows_off,
        footerAction = {
            Image(
                provider = ImageProvider(R.drawable.ic_check_double),
                contentDescription = null,
                colorFilter = ColorFilter.tint(WidgetColors.textPrimary),
                modifier = GlanceModifier.size(WidgetDimensions.checkIconSize),
            )
        },
        chipContent = {
            WidgetInfoChip(text = "9:00 AM")
        },
    )
}

@OptIn(ExperimentalGlancePreviewApi::class)
@Preview(widthDp = 180, heightDp = 132)
@Composable
private fun ProgressChipCardPreview() {
    WidgetMediaCard(
        title = "Silo",
        subtitle = "S3 • E7 - Radio",
        image = null,
        width = PREVIEW_CARD_WIDTH,
        onImageClick = action {},
        onTitleClick = action {},
        chipContent = {
            WidgetProgressChip(
                startText = "45m",
                width = PREVIEW_CARD_WIDTH - WidgetDimensions.spacingMedium,
                height = WidgetDimensions.chipHeight,
                progress = 0.4F,
                endText = "3 episodes",
            )
        },
    )
}
