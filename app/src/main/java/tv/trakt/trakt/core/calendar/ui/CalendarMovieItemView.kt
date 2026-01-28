package tv.trakt.trakt.core.calendar.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.onClickCombined
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.HorizontalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CalendarMovieItemView(
    item: CalendarItem.MovieItem,
    modifier: Modifier = Modifier,
    itemLoading: Boolean = false,
    onClick: (TraktId) -> Unit = {},
    onCheckClick: () -> Unit = {},
    onCheckLongClick: () -> Unit = {},
    onRemoveClick: () -> Unit = {},
) {
    val isReleased = remember(item.releasedAt) {
        val releasedAt = item.releasedAt
        releasedAt != null && releasedAt.isBefore(nowUtcInstant())
    }

    HorizontalMediaCard(
        title = "",
        more = false,
        containerImageUrl = item.movie.images?.getFanartUrl(),
        onClick = { onClick(item.id) },
        footerContent = {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    verticalArrangement = spacedBy(1.dp),
                    modifier = Modifier
                        .onClick {
                            onClick(item.movie.ids.trakt)
                        }
                        .weight(1F, false),
                ) {
                    Row(
                        horizontalArrangement = spacedBy(3.dp),
                        verticalAlignment = CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_movies_off),
                            contentDescription = null,
                            tint = TraktTheme.colors.chipContent,
                            modifier = Modifier
                                .size(13.dp)
                                .graphicsLayer {
                                    translationY = -(0.25).dp.toPx()
                                },
                        )

                        Text(
                            text = item.movie.title,
                            style = TraktTheme.typography.cardTitle,
                            color = TraktTheme.colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Text(
                        text = stringResource(R.string.translated_value_type_movie),
                        style = TraktTheme.typography.cardSubtitle,
                        color = TraktTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isReleased) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(start = 12.dp, end = 4.dp)
                            .size(23.dp),
                    ) {
                        when {
                            itemLoading -> {
                                FilmProgressIndicator(
                                    size = 18.dp,
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationX = 2.dp.toPx()
                                        },
                                )
                            }
                            item.watched -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check_double),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textPrimary,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .onClick(onClick = onRemoveClick),
                                )
                            }
                            !item.watched -> {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.accent,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .onClickCombined(
                                            onClick = onCheckClick,
                                            onLongClick = onCheckLongClick,
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        CalendarMovieItemView(
            item = CalendarItem.MovieItem(
                watched = true,
                movie = PreviewData.movie1,
            ),
        )
    }
}
