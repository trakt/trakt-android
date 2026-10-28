package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.rememberPercentFormat
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.UserMatch
import tv.trakt.trakt.common.ui.theme.colors.Purple400
import tv.trakt.trakt.core.userprofile.sections.match.model.MatchChipTier
import tv.trakt.trakt.core.userprofile.sections.match.model.matchLabelRes
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktSectionHeader
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.components.mediacards.skeletons.VerticalMediaSkeletonCard
import tv.trakt.trakt.ui.theme.TraktTheme

private const val SHARED_MEDIA_COLUMNS = 3
private const val SHARED_MEDIA_LIMIT = 6

private val ChipShape = RoundedCornerShape(100)
private val BarShape = RoundedCornerShape(100)
private val UnicornColor = Purple400
private val AvatarSize = 80.dp
private val AvatarLabelWidth = 112.dp

@Composable
internal fun UserMatchView(
    match: UserMatch,
    viewer: User?,
    user: User,
    sharedMovies: ImmutableList<Movie>?,
    sharedShows: ImmutableList<Show>?,
    modifier: Modifier = Modifier,
    onMovieClick: (Movie) -> Unit = {},
    onShowClick: (Show) -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier
            .verticalScroll(
                state = rememberScrollState(),
                overscrollEffect = null,
            )
            .padding(top = 16.dp),
    ) {
        MatchHero(
            score = match.score,
            viewer = viewer,
            user = user,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            verticalArrangement = spacedBy(32.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            MatchSection(title = stringResource(R.string.match_drawer_breakdown_header)) {
                BreakdownRow(
                    label = stringResource(R.string.match_drawer_breakdown_topics),
                    value = match.topicsScore,
                )
                BreakdownRow(
                    label = stringResource(R.string.match_drawer_breakdown_favorites),
                    value = match.favoritesScore,
                )
            }

            if (match.sharedTopics.isNotEmpty()) {
                MatchSection(
                    title = stringResource(R.string.match_drawer_shared_topics_header),
                    count = match.sharedTopics.size,
                ) {
                    FlowRow(
                        horizontalArrangement = spacedBy(5.dp),
                        verticalArrangement = spacedBy(6.dp),
                    ) {
                        match.sharedTopics.forEachIndexed { index, topic ->
                            TopicChip(
                                name = topic.name,
                                tier = MatchChipTier.of(index, topic.rarity),
                            )
                        }
                    }
                }
            }

            if (match.sharedShows.isNotEmpty()) {
                MatchSection(
                    title = stringResource(R.string.tag_text_shows),
                    count = match.sharedShows.size,
                ) {
                    SharedMediaGrid(
                        placeholders = match.sharedShows.size.coerceAtMost(SHARED_MEDIA_LIMIT),
                        items = sharedShows?.map {
                            SharedMedia(
                                it.title,
                                it.images?.getPosterUrl(),
                            ) { onShowClick(it) }
                        },
                    )
                }
            }

            if (match.sharedMovies.isNotEmpty()) {
                MatchSection(
                    title = stringResource(R.string.tag_text_movies),
                    count = match.sharedMovies.size,
                ) {
                    SharedMediaGrid(
                        placeholders = match.sharedMovies.size.coerceAtMost(SHARED_MEDIA_LIMIT),
                        items = sharedMovies?.map {
                            SharedMedia(
                                it.title,
                                it.images?.getPosterUrl(),
                            ) { onMovieClick(it) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MatchHero(
    score: Int,
    viewer: User?,
    user: User,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
        ) {
            MatchAvatar(
                user = viewer,
                label = stringResource(R.string.text_leaderboard_you),
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(AvatarSize),
            ) {
                Text(
                    text = stringResource(R.string.text_game_versus),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading5,
                )
            }
            MatchAvatar(
                user = user,
                label = user.displayName,
            )
        }

        Text(
            text = stringResource(R.string.match_drawer_overlap_caption),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 12.dp),
        )

        MatchGauge(score = score)

        Text(
            text = stringResource(matchLabelRes(score)),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading3,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MatchAvatar(
    user: User?,
    label: String,
) {
    Column(
        verticalArrangement = spacedBy(6.dp),
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier.width(AvatarLabelWidth),
    ) {
        MatchAvatarImage(user = user)
        Text(
            text = label,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmaller,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MatchAvatarImage(user: User?) {
    val modifier = Modifier
        .size(AvatarSize)
        .border(
            2.dp,
            when (user?.isAnyVip == true) {
                true -> TraktTheme.colors.vipAccent
                else -> Color.Transparent
            },
            CircleShape,
        )
        .clip(CircleShape)

    if (user?.hasAvatar == true) {
        AsyncImage(
            model = user.images?.avatar?.full,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.ic_person_placeholder),
            modifier = modifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_person_placeholder),
            contentDescription = null,
            modifier = modifier,
        )
    }
}

@Composable
private fun MatchGauge(
    score: Int,
    size: Dp = 148.dp,
    strokeWidth: Dp = 12.dp,
) {
    val trackColor = TraktTheme.colors.textPrimary.copy(alpha = 0.1F)
    val fillColor = TraktTheme.colors.accent

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2
            val arcSize = this.size.copy(
                width = this.size.width - stroke,
                height = this.size.height - stroke,
            )
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = 0F,
                sweepAngle = 360F,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke),
            )
            if (score > 0) {
                drawArc(
                    color = fillColor,
                    startAngle = -90F,
                    sweepAngle = 360F * score / 100F,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }

        Text(
            text = score.rememberPercentFormat(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading2.copy(fontSize = 36.sp),
            maxLines = 1,
        )
    }
}

@Composable
private fun MatchSection(
    title: String,
    modifier: Modifier = Modifier,
    count: Int? = null,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(10.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = spacedBy(10.dp),
        ) {
            TraktSectionHeader(
                title = title,
                chevron = false,
                collapsable = false,
                modifier = Modifier
                    .weight(1F),
            )
        }
        content()
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: Int,
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(110.dp),
        )

        Box(
            modifier = Modifier
                .weight(1F)
                .height(6.dp)
                .background(TraktTheme.colors.textPrimary.copy(alpha = 0.15F), BarShape),
        ) {
            if (value > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(value / 100F)
                        .fillMaxSize()
                        .background(TraktTheme.colors.accent, BarShape),
                )
            }
        }

        Text(
            text = value.rememberPercentFormat(),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.paragraphSmaller,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.width(44.dp),
        )
    }
}

@Composable
private fun TopicChip(
    name: String,
    tier: MatchChipTier,
) {
    val foreground = TraktTheme.colors.textPrimary
    val chipModifier = when (tier) {
        MatchChipTier.Unicorn -> {
            Modifier
                .background(
                    UnicornColor.copy(alpha = 0.33F),
                    ChipShape,
                )
        }
        MatchChipTier.Rare -> {
            Modifier
                .background(
                    foreground.copy(alpha = 0.2F),
                    ChipShape,
                )
        }
        MatchChipTier.Notable -> {
            Modifier
                .background(foreground.copy(alpha = 0.12F), ChipShape)
        }
        MatchChipTier.Common -> {
            Modifier
                .alpha(0.75F)
                .background(foreground.copy(alpha = 0.06F), ChipShape)
        }
    }

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(3.dp),
        modifier = chipModifier
            .padding(
                start = if (tier.hasSparkle) 8.dp else 10.dp,
                end = 11.dp,
                top = 6.dp,
                bottom = 6.dp,
            ),
    ) {
        if (tier.hasSparkle) {
            Icon(
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
                tint = when (tier) {
                    MatchChipTier.Unicorn -> UnicornColor
                    else -> foreground.copy(alpha = 0.85F)
                },
                modifier = Modifier.size(16.dp),
            )
        }
        val font = TraktTheme.typography.meta.copy(
            fontSize = 12.sp,
        )
        Text(
            text = name.capitalize(),
            color = foreground,
            style = when {
                tier.hasSparkle -> font
                else -> font
            },
            maxLines = 1,
        )
    }
}

private data class SharedMedia(
    val title: String,
    val posterUrl: String?,
    val onClick: () -> Unit,
)

@Composable
private fun SharedMediaGrid(
    placeholders: Int,
    items: List<SharedMedia>?,
) {
    val rows = remember(items, placeholders) {
        (items ?: List(placeholders) { null }).chunked(SHARED_MEDIA_COLUMNS)
    }

    Column(verticalArrangement = spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { item ->
                    val itemModifier = Modifier.weight(1F)
                    if (item == null) {
                        VerticalMediaSkeletonCard(
                            width = Dp.Infinity,
                            chipRatio = 0.66F,
                            modifier = itemModifier,
                        )
                    } else {
                        VerticalMediaCard(
                            title = item.title,
                            imageUrl = item.posterUrl,
                            width = Dp.Infinity,
                            more = false,
                            onClick = item.onClick,
                            chipContent = { chipModifier ->
                                Text(
                                    text = item.title,
                                    color = TraktTheme.colors.textPrimary,
                                    style = TraktTheme.typography.cardTitle,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = chipModifier,
                                )
                            },
                            modifier = itemModifier,
                        )
                    }
                }
                repeat(SHARED_MEDIA_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1F))
                }
            }
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        UserMatchView(
            match = UserMatchPreviewData.match,
            viewer = PreviewData.user1,
            user = PreviewData.user1,
            sharedMovies = listOf(PreviewData.movie1, PreviewData.movie2).toImmutableList(),
            sharedShows = null,
            modifier = Modifier.padding(16.dp),
        )
    }
}
