package tv.trakt.trakt.core.today.stories.ui

import android.os.SystemClock
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.today.model.FollowingActivity
import tv.trakt.trakt.core.today.model.TodayFriendAction
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.model.TodayMilestone
import tv.trakt.trakt.core.today.model.TodayStoryFrame
import tv.trakt.trakt.core.today.model.TodayTitleStory
import tv.trakt.trakt.core.today.stories.details.TodayPosterDetails
import tv.trakt.trakt.core.today.stories.details.TodayPosterDetailsViewModel
import tv.trakt.trakt.core.today.stories.watched.TodayWatchedButton
import tv.trakt.trakt.core.today.stories.watchlist.TodayWatchlistButton
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

private const val POSTER_RATIO = 2F / 3F
private const val FLIP_DURATION_MS = 200

private const val TAP_EDGE = 0.3F
private const val HOLD_THRESHOLD_MS = 300L

private val PosterShape = RoundedCornerShape(16.dp)
private val FooterMinGap = 16.dp
private val PosterElevation = 2.dp
private val MilestoneBorder = 2.dp

@Composable
internal fun TodayStoryFrameContent(
    frame: TodayStoryFrame,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onHeldChange: (Boolean) -> Unit,
    onConfirmVisibleChange: (Boolean) -> Unit,
) {
    Layout(
        content = {
            TodayStoryPoster(
                frame = frame,
                isFlipped = isFlipped,
                onFlip = onFlip,
                onNext = onNext,
                onPrevious = onPrevious,
                onHeldChange = onHeldChange,
            )

            Column(
                verticalArrangement = spacedBy(16.dp),
            ) {
                if (frame is TodayStoryFrame.Friend) {
                    frame.action.comment?.let {
                        TodayCommentBubble(comment = it)
                    }
                }

                TodayStoryFooter(
                    frame = frame,
                    onConfirmVisibleChange = onConfirmVisibleChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp)
                        .heightIn(min = 64.dp),
                )
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val minGap = FooterMinGap.roundToPx()

        val footer = measurables[1].measure(
            constraints.copy(minWidth = width, maxWidth = width, minHeight = 0),
        )

        // The poster takes what the footer leaves, then the footer centres in the space below it.
        val posterSpace = (height - footer.height - minGap * 2).coerceAtLeast(0)
        val posterWidth = minOf(width, (posterSpace * POSTER_RATIO).toInt())
        val posterHeight = (posterWidth / POSTER_RATIO).toInt()
        val poster = measurables[0].measure(Constraints.fixed(width, posterHeight))

        layout(width, height) {
            poster.place(0, 0)
            footer.place(0, posterHeight + (height - posterHeight - footer.height) / 2)
        }
    }
}

@Composable
private fun TodayStoryPoster(
    frame: TodayStoryFrame,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    onFlip: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onHeldChange: (Boolean) -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val flip by rememberUpdatedState(onFlip)
    val next by rememberUpdatedState(onNext)
    val previous by rememberUpdatedState(onPrevious)
    val heldChange by rememberUpdatedState(onHeldChange)

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        val posterWidth = minOf(maxWidth, maxHeight * POSTER_RATIO)
        val edge = (maxWidth - posterWidth) / 2 + posterWidth * TAP_EDGE

        // The poster is a child so its scrollable back face and these taps both get events.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(edge, isRtl) {
                    var pressedAt = 0L

                    detectTapGestures(
                        onPress = {
                            pressedAt = SystemClock.uptimeMillis()
                            heldChange(true)
                            tryAwaitRelease()
                            heldChange(false)
                        },
                        onTap = { offset ->
                            // A long hold only pauses the story; it never pages.
                            if (SystemClock.uptimeMillis() - pressedAt > HOLD_THRESHOLD_MS) {
                                return@detectTapGestures
                            }

                            val edgePx = edge.toPx()
                            val isStart = offset.x < edgePx
                            val isEnd = offset.x > size.width - edgePx

                            when {
                                isStart -> if (isRtl) next() else previous()
                                isEnd -> if (isRtl) previous() else next()
                                else -> flip()
                            }
                        },
                    )
                },
        ) {
            TodayFlipPoster(
                frame = frame,
                isFlipped = isFlipped,
                modifier = Modifier.width(posterWidth),
            )
        }
    }
}

@Composable
private fun TodayFlipPoster(
    frame: TodayStoryFrame,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val media = frame.media
    val rating = frame.rating10

    // Created with the front face so ratings and sentiment load before the card flips.
    val detailsViewModel: TodayPosterDetailsViewModel = koinViewModel(
        key = "today-details-${media.key}",
        parameters = { parametersOf(media) },
    )

    // Warm the back face image so it is ready by the time the card flips.
    val fanartUrl = media.images?.getFanartUrl(Images.Size.MEDIUM)
    LaunchedEffect(fanartUrl) {
        if (fanartUrl == null) return@LaunchedEffect
        context.imageLoader.execute(
            ImageRequest.Builder(context)
                .data(fanartUrl)
                .build(),
        )
    }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180F else 0F,
        animationSpec = tween(durationMillis = FLIP_DURATION_MS),
        label = "todayPosterFlip",
    )
    val isBackVisible = rotation > 90F

    Box(
        modifier = modifier
            .aspectRatio(POSTER_RATIO)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12F * density
            }
            .shadow(
                elevation = PosterElevation,
                shape = PosterShape,
                ambientColor = Color.Black,
                spotColor = Color.Black,
            )
            .then(
                if (frame.milestone != null) {
                    Modifier.border(MilestoneBorder, TraktTheme.colors.accent, PosterShape)
                } else {
                    Modifier
                },
            ),
    ) {
        TodayPosterDetails(
            media = media,
            viewModel = detailsViewModel,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = 180F
                    alpha = if (isBackVisible) 1F else 0F
                }
                .clip(PosterShape),
        )

        AsyncImage(
            model = media.images?.getPosterUrl(Images.Size.MEDIUM),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (isBackVisible) 0F else 1F)
                .clip(PosterShape)
                .background(TraktTheme.colors.placeholderContainer),
        )

//        if (rating != null && !isFlipped) {
//            TodayRatingTag(
//                rating10 = rating,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(8.dp),
//            )
//        }
    }
}

@Composable
private fun TodayStoryFooter(
    frame: TodayStoryFrame,
    modifier: Modifier = Modifier,
    onConfirmVisibleChange: (Boolean) -> Unit,
) {
    val media = frame.media

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(10.dp),
        modifier = modifier,
    ) {
        when (frame) {
            is TodayStoryFrame.Summary -> TodaySummaryFooter(
                story = frame.story,
                modifier = Modifier.weight(1F),
            )
            is TodayStoryFrame.Friend -> TodayFriendFooter(
                action = frame.action,
                modifier = Modifier.weight(1F),
            )
            is TodayStoryFrame.ForYou -> TodayForYouFooter(
                item = frame.item,
                modifier = Modifier.weight(1F),
            )
        }

        when (frame) {
            is TodayStoryFrame.Title -> TodayWatchlistButton(
                media = media,
                viewModel = koinViewModel(
                    key = "today-watchlist-${media.key}",
                    parameters = { parametersOf(media) },
                ),
                onConfirmVisibleChange = onConfirmVisibleChange,
            )
            is TodayStoryFrame.ForYou -> TodayWatchedButton(
                item = frame.item,
                viewModel = koinViewModel(
                    key = "today-watched-${frame.item.key}",
                    parameters = { parametersOf(frame.item) },
                ),
                onConfirmVisibleChange = onConfirmVisibleChange,
            )
        }
    }
}

@Composable
private fun TodaySummaryFooter(
    story: TodayTitleStory,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(
                R.string.text_today_friends_watched,
                story.users.size,
            ),
            style = TraktTheme.typography.heading6,
            color = TraktTheme.colors.textPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(8.dp),
        ) {
            TodayFaces(users = story.users)
            story.milestone?.let {
                TodayMilestoneChip(milestone = it)
            }
        }
    }
}

private val TodayStoryFrame.milestone: TodayMilestone?
    get() = when (this) {
        is TodayStoryFrame.Friend -> action.milestone
        is TodayStoryFrame.Summary -> story.milestone
        is TodayStoryFrame.ForYou -> null
    }

private val TodayStoryFrame.rating10: Float?
    get() = when (this) {
        is TodayStoryFrame.Friend -> action.rating?.toFloat()
        is TodayStoryFrame.Summary -> story.averageRating
        is TodayStoryFrame.ForYou -> null
    }

@Preview(heightDp = 720)
@Composable
private fun Preview() {
    val action = TodayFriendAction(
        key = "1",
        user = PreviewData.user1,
        activityAt = Instant.now(),
        target = FollowingActivity.Target.EpisodeTarget(
            show = PreviewData.show1,
            episode = PreviewData.episode1,
        ),
        detail = FollowingActivity.Detail.Watch,
        episodeCount = 3,
        milestone = TodayMilestone(TodayMilestone.Type.SeasonEnd, season = 1),
    )
    val story = TodayTitleStory(
        media = TodayMedia.ShowMedia(PreviewData.show1),
        actions = persistentListOf(action),
        users = persistentListOf(PreviewData.user1),
        averageRating = null,
        milestone = action.milestone,
        latestAt = Instant.now(),
    )

    TraktTheme {
        TodayStoryFrameContent(
            frame = TodayStoryFrame.Friend(story, action),
            isFlipped = false,
            onFlip = {},
            onNext = {},
            onPrevious = {},
            onHeldChange = {},
            onConfirmVisibleChange = {},
            modifier = Modifier
                .background(TraktTheme.colors.backgroundPrimary),
        )
    }
}
