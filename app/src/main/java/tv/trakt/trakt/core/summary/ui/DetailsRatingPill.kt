package tv.trakt.trakt.core.summary.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.core.ratings.ui.RatingDelight
import tv.trakt.trakt.core.ratings.ui.RatingDelightOverlay
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.ratings.ui.ratingDelight
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.milliseconds

private val CollapseDelay = 600.milliseconds
private val CollapseDelightDelay = 2100.milliseconds

private val PillShape = RoundedCornerShape(18.dp)

private val PillIconSize = 23.dp
private val PillHorizontalPadding = 12.dp
private val PillVerticalPadding = 12.dp
private val PillHeight = PillIconSize + PillVerticalPadding * 2
private val PillTouchPadding = 6.dp
private val PillDragLabelSpacing = 40.dp
private val SeparatorHeight = 16.dp
private val SeparatorInnerPadding = 3.dp

private data class RatingCommit(
    val id: Int,
    val rating: Int?,
)

@Composable
internal fun DetailsRatingPill(
    rating: UserRating?,
    favoriteLoading: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onRatingDrag: (Boolean) -> Unit = {},
    onRatingClick: (Int) -> Unit = {},
    onRatingRemoveClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
) {
    var commit by remember { mutableStateOf<RatingCommit?>(null) }
    var ratingDragging by remember { mutableStateOf(false) }

    LaunchedEffect(commit) {
        val current = commit ?: return@LaunchedEffect
        val hasDelight = current.rating?.let(::ratingDelight) != null
        delay(if (hasDelight) CollapseDelightDelay else CollapseDelay)
        onExpandedChange(false)
        commit = null
    }

    BackHandler(enabled = expanded) {
        onExpandedChange(false)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .dropShadow(
                shape = PillShape,
                shadow = Shadow(
                    radius = 6.dp,
                    color = Color.Black,
                    spread = 1.dp,
                    alpha = 0.12F,
                ),
            )
            .background(TraktTheme.colors.navigationContainer, PillShape)
            .height(PillHeight)
            .padding(horizontal = PillHorizontalPadding - PillTouchPadding),
    ) {
        // Only the rating side swaps; the favorite icon stays mounted across both states.
        AnimatedContent(
            targetState = expanded,
            contentAlignment = Alignment.CenterEnd,
            transitionSpec = {
                fadeIn(tween(150, delayMillis = 75)) togetherWith
                    fadeOut(tween(75)) using
                    SizeTransform(clip = false) { _, _ -> tween(200) }
            },
            label = "ratingPill",
        ) { isExpanded ->
            when {
                isExpanded -> UserRatingBar(
                    rating = rating?.rating,
                    favoriteVisible = false,
                    size = PillIconSize,
                    textSpacing = PillDragLabelSpacing,
                    topPadding = 0.dp,
                    onRatingDrag = {
                        ratingDragging = it
                        onRatingDrag(it)
                    },
                    onRatingClick = {
                        onRatingClick(it)
                        commit = RatingCommit(id = (commit?.id ?: 0) + 1, rating = it)
                    },
                    onRatingRemoveClick = {
                        onRatingRemoveClick()
                        commit = RatingCommit(id = (commit?.id ?: 0) + 1, rating = null)
                    },
                    modifier = Modifier.padding(horizontal = PillTouchPadding),
                )

                else -> CollapsedRating(
                    rating = rating,
                    onClick = { onExpandedChange(true) },
                )
            }
        }

        Box(
            modifier = Modifier
                .padding(horizontal = SeparatorInnerPadding)
                .size(width = 1.dp, height = SeparatorHeight)
                .background(TraktTheme.colors.separator),
        )

        PillFavorite(
            favorite = rating?.favorite == true,
            loading = favoriteLoading,
            dimmed = ratingDragging,
            onClick = onFavoriteClick,
        )
    }
}

@Composable
private fun CollapsedRating(
    rating: UserRating?,
    onClick: () -> Unit,
) {
    val rated = (rating?.rating ?: 0) > 0

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = Modifier
            .onClick(onClick = onClick)
            .padding(PillTouchPadding),
    ) {
        Icon(
            painter = painterResource(
                if (rated) R.drawable.ic_star_trakt_on else R.drawable.ic_star_trakt_off,
            ),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(PillIconSize),
        )
        if (rated && rating != null) {
            Text(
                text = rating.rating5Scale,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.meta.copy(fontSize = 14.sp),
            )
        }
    }
}

@Composable
private fun PillFavorite(
    favorite: Boolean,
    loading: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
) {
    var favoriteDelightId by remember { mutableIntStateOf(0) }
    var favoriteDelightPending by remember { mutableStateOf(false) }
    LaunchedEffect(favorite, favoriteDelightPending) {
        if (!favorite || !favoriteDelightPending) return@LaunchedEffect
        favoriteDelightPending = false
        favoriteDelightId++
    }

    val favoriteAlpha by animateFloatAsState(
        targetValue = when {
            dimmed -> 0.05F
            loading -> 0.5F
            else -> 1F
        },
        animationSpec = tween(150),
        label = "favoriteAlpha",
    )

    Box(
        modifier = Modifier
            .onClick(enabled = !loading) {
                favoriteDelightPending = !favorite
                onClick()
            }
            .padding(PillTouchPadding),
    ) {
        Icon(
            painter = painterResource(
                if (favorite) R.drawable.ic_heart_on else R.drawable.ic_heart_off,
            ),
            contentDescription = null,
            tint = if (favorite) Red500 else TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(PillIconSize)
                .alpha(favoriteAlpha),
        )

        if (favoriteDelightId > 0) {
            val center = with(LocalDensity.current) { PillIconSize.toPx() / 2 }
            RatingDelightOverlay(
                delight = RatingDelight.FavoriteGlow,
                origin = Offset(center, center),
                key = favoriteDelightId,
                modifier = Modifier.size(PillIconSize),
            )
        }
    }
}

@Preview
@Composable
private fun DetailsRatingPillPreview() {
    TraktThemeLightDark {
        Column(
            verticalArrangement = spacedBy(16.dp),
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(16.dp),
        ) {
            DetailsRatingPill(
                rating = null,
                favoriteLoading = false,
                expanded = false,
                onExpandedChange = {},
            )
            DetailsRatingPill(
                rating = UserRating(
                    mediaId = TraktId(1),
                    mediaType = MediaType.Movie,
                    rating = 7,
                    favorite = true,
                ),
                favoriteLoading = false,
                expanded = false,
                onExpandedChange = {},
            )
            DetailsRatingPill(
                rating = UserRating(
                    mediaId = TraktId(1),
                    mediaType = MediaType.Movie,
                    rating = 8,
                ),
                favoriteLoading = false,
                expanded = true,
                onExpandedChange = {},
            )
        }
    }
}
