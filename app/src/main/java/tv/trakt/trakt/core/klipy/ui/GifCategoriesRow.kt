package tv.trakt.trakt.core.klipy.ui

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.core.klipy.model.GifCategory
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.ui.theme.TraktTheme

private val tileShape = RoundedCornerShape(8.dp)

private val tileWidth = 96.dp
private val tileHeight = 48.dp

private const val SKELETON_TILES = 6

private val labelShadow = Shadow(
    color = Color.Black,
    offset = Offset(0F, 1F),
    blurRadius = 3F,
)

@Composable
internal fun GifCategoriesRow(
    categories: ImmutableList<GifCategory>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onCategoryClick: (GifCategory) -> Unit = {},
) {
    if (categories.isEmpty()) {
        GifCategoriesSkeletonRow(
            contentPadding = contentPadding,
            modifier = modifier,
        )
        return
    }

    LazyRow(
        horizontalArrangement = spacedBy(8.dp),
        contentPadding = contentPadding,
        overscrollEffect = null,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(
            items = categories,
            key = { it.query },
        ) { category ->
            GifCategoryTile(
                category = category,
                onClick = { onCategoryClick(category) },
            )
        }
    }
}

@Composable
private fun GifCategoriesSkeletonRow(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")
    val shimmerTransition by infiniteTransition.animateColor(
        initialValue = TraktTheme.colors.skeletonContainer,
        targetValue = TraktTheme.colors.skeletonShimmer,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerTransition",
    )

    LazyRow(
        horizontalArrangement = spacedBy(8.dp),
        contentPadding = contentPadding,
        userScrollEnabled = false,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(count = SKELETON_TILES) {
            Box(
                modifier = Modifier
                    .size(width = tileWidth, height = tileHeight)
                    .clip(tileShape)
                    .background(shimmerTransition),
            )
        }
    }
}

@Composable
private fun GifCategoryTile(
    category: GifCategory,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .size(width = tileWidth, height = tileHeight)
            .clip(tileShape)
            .background(TraktTheme.colors.skeletonShimmer)
            .onClick(onClick = onClick),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(category.previewUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Text(
            text = category.title.replaceFirstChar { it.titlecase() },
            style = TraktTheme.typography.meta.copy(
                fontWeight = FontWeight.Bold,
                shadow = labelShadow,
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        GifCategoriesRow(
            categories = persistentListOf(
                GifCategory(title = "happy", query = "happy", previewUrl = ""),
                GifCategory(title = "sad", query = "sad", previewUrl = ""),
                GifCategory(title = "excited", query = "excited", previewUrl = ""),
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        GifCategoriesRow(
            categories = persistentListOf(),
        )
    }
}
