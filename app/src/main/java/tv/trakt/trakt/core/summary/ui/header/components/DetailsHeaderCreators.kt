package tv.trakt.trakt.core.summary.ui.header.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

/**
 * "[label] name, name" line under the title. [creators] is null while loading, which keeps the
 * line invisible; an empty list shows "-".
 */
@Composable
internal fun DetailsHeaderCreators(
    label: String,
    creators: ImmutableList<Person>?,
    onCreatorClick: (Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (creators == null) 0F else 1F,
        animationSpec = tween(delayMillis = 50),
        label = "alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(3.dp),
        modifier = modifier
            .alpha(animatedAlpha)
            .padding(top = 2.dp),
    ) {
        Text(
            text = label,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
        )

        if (creators.isNullOrEmpty()) {
            Text(
                text = "-",
                fontWeight = FontWeight.SemiBold,
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmaller,
            )
        }

        creators?.forEachIndexed { index, creator ->
            Text(
                text = when {
                    index < creators.lastIndex -> "${creator.name},"
                    else -> creator.name
                },
                fontWeight = FontWeight.SemiBold,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraphSmaller,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1F, fill = false)
                    .onClick { onCreatorClick(creator) },
            )
        }
    }
}

/** A single creator as a list, dropping the placeholder person used when none is known. */
internal fun Person?.toCreators(): ImmutableList<Person>? {
    if (this == null) return null
    if (name.isBlank()) return persistentListOf()

    return persistentListOf(this)
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        Column(verticalArrangement = spacedBy(8.dp)) {
            DetailsHeaderCreators(
                label = "Created by",
                creators = persistentListOf(),
                onCreatorClick = {},
            )
            DetailsHeaderCreators(
                label = "Created by",
                creators = persistentListOf(PreviewData.person1),
                onCreatorClick = {},
            )
            DetailsHeaderCreators(
                label = "Created by",
                creators = persistentListOf(
                    PreviewData.person1.copy(name = "Alexandra Montgomery-Fitzgerald"),
                    PreviewData.person2.copy(name = "Christopher Worthington"),
                ),
                onCreatorClick = {},
            )
        }
    }
}
