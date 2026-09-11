package tv.trakt.trakt.core.summary.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.core.ratings.ui.UserRatingBar
import tv.trakt.trakt.core.ratings.ui.userRatingDragText
import tv.trakt.trakt.ui.theme.TraktTheme

// Scroll distance after which the floating rating bar fades out.
internal val RatingBarFadeDistance = 16.dp

@Composable
internal fun DetailsRating(
    modifier: Modifier = Modifier,
    rating: UserRating?,
    loading: Boolean = false,
    favoriteVisible: Boolean = true,
    onRatingDrag: (Boolean) -> Unit,
    onRatingClick: (Int) -> Unit,
    onRatingRemoveClick: () -> Unit,
    onFavoriteClick: () -> Unit = {},
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = TraktTheme.colors.shadowDynamicSmall,
            pressedElevation = TraktTheme.colors.shadowDynamicSmall,
            disabledElevation = TraktTheme.colors.shadowDynamicSmall,
        ),
        colors = cardColors(
            containerColor = TraktTheme.colors.navigationContainer,
        ),
        content = {
            var isDragging by remember { mutableStateOf(false) }
            var dragStars by remember { mutableFloatStateOf(0f) }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 12.dp),
            ) {
                if (isDragging) {
                    Text(
                        text = userRatingDragText(dragStars),
                        textAlign = TextAlign.Center,
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.meta.copy(
                            fontSize = 13.sp,
                        ),
                    )
                }

                UserRatingBar(
                    rating = rating?.rating,
                    favoriteLoading = loading,
                    favoriteVisible = favoriteVisible,
                    favorite = rating?.favorite == true,
                    dragLabelVisible = false,
                    onRatingDrag = {
                        isDragging = it
                        onRatingDrag(it)
                    },
                    onRatingDragValue = { dragStars = it },
                    onRatingClick = onRatingClick,
                    onRatingRemoveClick = onRatingRemoveClick,
                    onFavoriteClick = onFavoriteClick,
                )
            }
        },
    )
}
