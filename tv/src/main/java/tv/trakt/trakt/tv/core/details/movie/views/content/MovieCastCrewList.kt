package tv.trakt.trakt.tv.core.details.movie.views.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.tv.common.model.CastPerson
import tv.trakt.trakt.tv.common.model.Person
import tv.trakt.trakt.tv.common.ui.PositionFocusLazyRow
import tv.trakt.trakt.tv.common.ui.mediacards.VerticalMediaCard
import tv.trakt.trakt.tv.helpers.extensions.emptyFocusListItems
import tv.trakt.trakt.tv.ui.theme.TraktTheme

@Composable
internal fun MovieCastCrewList(
    header: String,
    cast: () -> ImmutableList<CastPerson>,
    onFocused: () -> Unit,
    onClick: (Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = header,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading4,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainContentStartSpace,
            ),
        )

        PositionFocusLazyRow(
            contentPadding = PaddingValues(
                start = TraktTheme.spacing.mainContentStartSpace,
                end = TraktTheme.spacing.mainContentEndSpace,
            ),
        ) {
            items(
                items = cast(),
                key = { item -> item.person.ids.trakt.value },
            ) { person ->
                VerticalMediaCard(
                    title = person.person.name,
                    imageUrl = person.person.images?.getHeadshotUrl(THUMB),
                    onClick = { onClick(person.person) },
                    chipContent = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = person.person.name,
                                style = TraktTheme.typography.cardTitle,
                                color = TraktTheme.colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )

                            val characters = person.characters.joinToString(" / ") { word ->
                                word.replaceFirstChar { it.uppercaseChar() }
                            }
                            Text(
                                text = characters,
                                style = TraktTheme.typography.cardSubtitle,
                                color = TraktTheme.colors.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    modifier = Modifier
                        .onFocusChanged {
                            if (it.isFocused) {
                                onFocused()
                            }
                        },
                )
            }

            emptyFocusListItems()
        }
    }
}
