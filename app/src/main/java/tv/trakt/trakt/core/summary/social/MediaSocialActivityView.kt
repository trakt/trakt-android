@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.social

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Ids
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TmdbId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.social.ui.MediaSocialItemCard
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration.Companion.days

@Composable
internal fun MediaSocialActivityView(
    viewModel: MediaSocialActivityViewModel,
    mediaTitle: String,
    onUserClick: (user: User) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    MediaSocialActivityContent(
        state = state,
        mediaTitle = mediaTitle,
        onUserClick = onUserClick,
        modifier = modifier,
    )
}

@Composable
private fun MediaSocialActivityContent(
    state: MediaSocialActivityState,
    mediaTitle: String,
    modifier: Modifier = Modifier,
    onUserClick: (user: User) -> Unit = {},
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        item(key = "header") {
            TraktHeader(
                title = stringResource(R.string.list_title_social_activity),
                subtitle = mediaTitle,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }

        items(
            items = state.activity.orEmpty(),
            key = { it.user.ids.trakt.value },
        ) { item ->
            MediaSocialItemCard(
                item = item,
                containerColor = TraktTheme.colors.dialogOnContainer,
                shadow = 1.dp,
                onUserClick = { onUserClick(item.user) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@DevicePreview
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            val item = MediaSocialActivity(
                type = MediaType.MOVIE,
                user = PreviewData.user1,
                watched = MediaSocialActivity.Watched(
                    lastWatchedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                    lastUpdatedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                    plays = 3,
                    rated = MediaSocialActivity.Watched.Rated(
                        rating = 8,
                        ratedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                    ),
                    commented = MediaSocialActivity.Watched.Commented(
                        id = 1.toTraktId(),
                        comment = "Sample comment by someone",
                        spoiler = false,
                        review = false,
                        createdAt = nowUtcInstant(),
                        updatedAt = nowUtcInstant(),
                    ),
                ),
                watchlisted = MediaSocialActivity.Watchlisted(
                    listedAt = nowUtcInstant().minusSeconds(3.days.inWholeSeconds),
                ),
            )

            MediaSocialActivityContent(
                state = MediaSocialActivityState(
                    activity = persistentListOf(
                        item,
                        item.copy(
                            user = PreviewData.user1.copy(
                                ids = Ids(
                                    trakt = TraktId(2),
                                    slug = SlugId("john-doe"),
                                    imdb = ImdbId("tt1234567"),
                                    tmdb = TmdbId(67890),
                                ),
                            ),
                        ),
                    ),
                ),
                mediaTitle = "The Movie Title",
            )
        }
    }
}
