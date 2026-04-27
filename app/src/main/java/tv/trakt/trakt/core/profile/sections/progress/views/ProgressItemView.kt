package tv.trakt.trakt.core.profile.sections.progress.views

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.InfoChip
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ProgressItemView(
    item: ProfileProgressItem,
    modifier: Modifier = Modifier,
    onShowClick: (Show) -> Unit = {},
) {
    when (item) {
        is ProfileProgressItem.ShowItem -> {
            VerticalMediaCard(
                title = item.show.title,
                imageUrl = item.show.images?.getPosterUrl(),
                more = false,
                onClick = { onShowClick(item.show) },
                cardContent = {
                    item.hiddenAt?.let {
                        InfoChip(
                            text = it.toLocal().format(mediumDateFormat),
                            iconPainter = painterResource(R.drawable.ic_drop),
                            iconPadding = 1.dp,
                            containerColor = TraktTheme.colors.chipContainerOnContent,
                        )
                    }
                    item.remainingEpisodes?.let {
                        if (it > 0) {
                            InfoChip(
                                text = stringResource(R.string.tag_text_remaining_episodes, it),
                                containerColor = TraktTheme.colors.chipContainerOnContent,
                            )
                        }
                    }
                },
                chipContent = { modifier ->
                    Column(
                        verticalArrangement = spacedBy(1.dp),
                        modifier = modifier
                            .onClick {
                                onShowClick.invoke(item.show)
                            },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = spacedBy(4.dp),
                            modifier = modifier,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_shows_off),
                                contentDescription = null,
                                tint = TraktTheme.colors.chipContent,
                                modifier = Modifier.size(13.dp),
                            )

                            Text(
                                text = item.show.title,
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

//                        Text(
//                            text = item.show.released?.year?.toString() ?: "TBA",
//                            style = TraktTheme.typography.cardSubtitle,
//                            color = TraktTheme.colors.textSecondary,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis,
//                        )
                    }
                },
                modifier = modifier,
            )
        }
    }
}

@Preview
@Composable
private fun Preview1() {
    TraktTheme {
        ProgressItemView(
            item = ProfileProgressItem.ShowItem(
                show = PreviewData.show1,
            ),
        )
    }
}
