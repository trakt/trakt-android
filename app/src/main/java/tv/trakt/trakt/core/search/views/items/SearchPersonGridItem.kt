package tv.trakt.trakt.core.search.views.items

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.capitalize
import tv.trakt.trakt.common.helpers.extensions.mediumDateFormat
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchPersonGridItem(
    item: SearchItem.Person,
    onPersonClick: (Person) -> Unit,
    modifier: Modifier,
) {
    VerticalMediaCard(
        title = item.person.name,
        more = false,
        imageUrl = item.person.images?.getHeadshotUrl(),
        chipContent = {
            Column(
                verticalArrangement = spacedBy(1.dp),
            ) {
                Text(
                    text = item.person.name,
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (item.showBirthday) {
                    item.person.birthday?.let { date ->
                        val (isToday, age) = remember(date) {
                            val today = nowLocalDay()
                            Pair(
                                date.monthValue == today.monthValue &&
                                    date.dayOfMonth == today.dayOfMonth,
                                (today.year - date.year) - when {
                                    date.dayOfYear <= today.dayOfYear -> 0
                                    else -> 1
                                },
                            )
                        }

                        Row(
                            horizontalArrangement = spacedBy(4.dp),
                            verticalAlignment = CenterVertically,
                        ) {
                            if (isToday) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_celebration),
                                    contentDescription = null,
                                    tint = TraktTheme.colors.textSecondary,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                            Text(
                                text = "${date.format(mediumDateFormat()).capitalize()} ($age)",
                                style = TraktTheme.typography.cardSubtitle,
                                color = TraktTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                } else {
                    item.person.knownForDepartment?.let { string ->
                        Text(
                            text = string.replaceFirstChar { it.uppercaseChar() },
                            style = TraktTheme.typography.cardSubtitle,
                            color = TraktTheme.colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
        onClick = { onPersonClick(item.person) },
        modifier = modifier,
    )
}
