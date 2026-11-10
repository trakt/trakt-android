package tv.trakt.trakt.core.summary.ui.header

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.ui.theme.colors.Shade700
import tv.trakt.trakt.core.summary.people.model.PersonLink
import tv.trakt.trakt.core.summary.people.model.PersonLinkType
import tv.trakt.trakt.core.summary.people.model.toPersonLinks
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeader(
    person: Person,
    loading: Boolean,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onReportClick: (() -> Unit)? = null,
    onLinkClick: ((PersonLinkType) -> Unit)? = null,
    topRightContent: (@Composable () -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current
    val links = remember(person.socialIds) {
        person.socialIds.toPersonLinks()
    }

    val openLink: (PersonLink) -> Unit = { link ->
        uriHandler.openUri(link.url)
        onLinkClick?.invoke(link.type)
    }

    DetailsHeader(
        loading = loading,
        title = person.name,
        titleFooter = {
            Text(
                text = person.knownForDepartment
                    ?.replaceFirstChar { it.uppercaseChar() }
                    ?: "-",
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
                modifier = Modifier.padding(top = 1.dp),
            )
        },
        topRightContent = topRightContent,
        extraRightColumn = {
            Column(
                horizontalAlignment = CenterHorizontally,
                verticalArrangement = spacedBy(20.dp),
                modifier = Modifier
                    .padding(top = 17.dp)
                    .fillMaxWidth(),
            ) {
                links.inline.forEach { link ->
                    Icon(
                        painter = painterResource(link.type.iconRes),
                        tint = TraktTheme.colors.textPrimary,
                        contentDescription = null,
                        modifier = Modifier
                            .size(link.type.iconSize)
                            .onClick { openLink(link) },
                    )
                }

                if (links.overflow.isNotEmpty() || onReportClick != null) {
                    MoreMenuButton(
                        overflowLinks = links.overflow,
                        onLinkClick = openLink,
                        onReportClick = onReportClick,
                    )
                }
            }
        },
        genres = EmptyImmutableList,
        imageUrl = person.images?.getHeadshotUrl(),
        imagePlaceholderUrl = person.images?.getPosterUrl(Size.THUMB),
        imageHorizontal = false,
        onShareClick = onShareClick,
        onShareImageClick = null,
        onBackClick = onBackClick,
        date = null,
        status = null,
        runtime = null,
        externalRatings = null,
        externalRatingsVisible = false,
        externalRottenVisible = false,
        externalMalVisible = false,
        episodesCount = null,
        playsCount = null,
        certification = null,
        accentColor = Shade700,
        creditsCount = null,
        traktRatings = null,
        watched = false,
        watching = false,
        personImdb = person.ids.imdb,
        onImdbClick = {},
        onRottenClick = {},
        onMalClick = {},
        onLetterboxdClick = {},
        modifier = modifier,
    )
}

@Composable
private fun MoreMenuButton(
    overflowLinks: ImmutableList<PersonLink>,
    onLinkClick: (PersonLink) -> Unit,
    onReportClick: (() -> Unit)?,
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(24.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vertical),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(18.dp)
                .onClick { showMenu = true },
        )

        DropdownMenu(
            expanded = showMenu,
            containerColor = TraktTheme.colors.dialogContainer,
            shape = RoundedCornerShape(16.dp),
            onDismissRequest = { showMenu = false },
        ) {
            overflowLinks.forEach { link ->
                MenuItem(
                    text = stringResource(link.type.labelRes),
                    iconRes = link.type.iconRes,
                    onClick = {
                        showMenu = false
                        onLinkClick(link)
                    },
                )
            }

            if (onReportClick != null) {
                MenuItem(
                    text = stringResource(R.string.button_text_report),
                    iconRes = R.drawable.ic_flag,
                    onClick = {
                        showMenu = false
                        onReportClick()
                    },
                )
            }
        }
    }
}

@Composable
private fun MenuItem(
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = TraktTheme.typography.buttonTertiary,
                color = TraktTheme.colors.textPrimary,
            )
        },
        onClick = onClick,
        leadingIcon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(21.dp),
            )
        },
    )
}
