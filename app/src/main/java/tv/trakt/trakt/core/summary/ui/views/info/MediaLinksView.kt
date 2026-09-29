package tv.trakt.trakt.core.summary.ui.views.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MediaLinksView(
    links: ImmutableList<MediaLink>,
    modifier: Modifier = Modifier,
    onLinkClick: (MediaLink) -> Unit = {},
) {
    if (links.isEmpty()) return

    val (officialLinks, otherLinks) = remember(links) {
        links.partition { it.type.official }
    }

    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier,
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = TraktTheme.colors.separator,
        )

        Column(
            verticalArrangement = spacedBy(16.dp),
        ) {
            if (officialLinks.isNotEmpty()) {
                MediaLinksGroup(
                    title = stringResource(R.string.header_official_links),
                    links = officialLinks,
                    onLinkClick = onLinkClick,
                )
            }

            if (otherLinks.isNotEmpty()) {
                MediaLinksGroup(
                    title = stringResource(R.string.header_other_links),
                    links = otherLinks,
                    onLinkClick = onLinkClick,
                )
            }
        }
    }
}

@Composable
private fun MediaLinksGroup(
    title: String,
    links: List<MediaLink>,
    onLinkClick: (MediaLink) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.textSecondary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        Row(
            horizontalArrangement = spacedBy(8.dp),
        ) {
            for (link in links) {
                MediaLinkButton(
                    link = link,
                    onClick = { onLinkClick(link) },
                )
            }
        }
    }
}

@Composable
private fun MediaLinkButton(
    link: MediaLink,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .shadow(
                elevation = TraktTheme.colors.shadowDynamicDefault,
                shape = shape,
            )
            .background(
                color = TraktTheme.colors.dialogOnContainer,
                shape = shape,
            )
            .onClick(onClick = onClick),
    ) {
        if (link.type.brandColored) {
            Image(
                painter = painterResource(link.type.iconRes),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        } else {
            Icon(
                painter = painterResource(link.type.iconRes),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        MediaLinksView(
            links = MediaLinkType.entries
                .map { MediaLink(type = it, url = "https://trakt.tv") }
                .toImmutableList(),
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewOtherOnly() {
    TraktTheme {
        MediaLinksView(
            links = persistentListOf(
                MediaLink(type = MediaLinkType.Imdb, url = "https://imdb.com"),
                MediaLink(type = MediaLinkType.Tmdb, url = "https://themoviedb.org"),
            ),
            modifier = Modifier.padding(24.dp),
        )
    }
}
