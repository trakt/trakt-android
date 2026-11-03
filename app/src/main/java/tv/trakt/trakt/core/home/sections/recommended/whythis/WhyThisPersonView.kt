package tv.trakt.trakt.core.home.sections.recommended.whythis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Images.Size.THUMB
import tv.trakt.trakt.common.model.recommended.RecommendedPerson
import tv.trakt.trakt.common.model.recommended.RecommendedPerson.Role.Acting
import tv.trakt.trakt.common.model.recommended.RecommendedPerson.Role.Directing
import tv.trakt.trakt.common.model.recommended.RecommendedPerson.Role.Making
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val HEADSHOT_SIZE = 56.dp

@Composable
internal fun WhyThisPersonView(
    reason: RecommendedPerson,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(15.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            AsyncImage(
                model = reason.person.images?.getHeadshotUrl(THUMB),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(HEADSHOT_SIZE)
                    .clip(CircleShape)
                    .background(TraktTheme.colors.placeholderContainer),
            )

            Column(verticalArrangement = spacedBy(1.dp)) {
                Text(
                    text = reason.person.name,
                    style = TraktTheme.typography.paragraph,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(reason.roleLineRes(), reason.watchedCount),
                    style = TraktTheme.typography.paragraphSmaller,
                    color = TraktTheme.colors.textSecondary,
                )
            }
        }

        if (reason.titles.isNotEmpty()) {
            val hiddenCount = reason.watchedCount - reason.titles.size
            WhyThisPosterGrid(
                cells = reason.titles.map {
                    WhyThisGridCell.Poster(
                        title = it.movie?.title ?: it.show?.title.orEmpty(),
                        imageUrl = (it.movie?.images ?: it.show?.images)?.getPosterUrl(),
                    )
                } + listOfNotNull(
                    hiddenCount.takeIf { it > 0 }?.let { WhyThisGridCell.More(it) },
                ),
            )
        }
    }
}

private fun RecommendedPerson.roleLineRes(): Int {
    val one = watchedCount == 1
    val acted = Acting in watchedRoles
    return when {
        Directing in watchedRoles && acted -> when {
            one -> R.string.text_recommendation_person_watched_directing_acting_one
            else -> R.string.text_recommendation_person_watched_directing_acting_other
        }
        Directing in watchedRoles -> when {
            one -> R.string.text_recommendation_person_watched_directing_one
            else -> R.string.text_recommendation_person_watched_directing_other
        }
        Making in watchedRoles && acted -> when {
            one -> R.string.text_recommendation_person_watched_making_acting_one
            else -> R.string.text_recommendation_person_watched_making_acting_other
        }
        Making in watchedRoles -> when {
            one -> R.string.text_recommendation_person_watched_making_one
            else -> R.string.text_recommendation_person_watched_making_other
        }
        else -> when {
            one -> R.string.text_recommendation_person_watched_acting_one
            else -> R.string.text_recommendation_person_watched_acting_other
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        WhyThisPersonView(
            reason = RecommendedPerson(
                person = PreviewData.person1,
                watchedRoles = persistentListOf(Directing, Acting),
                watchedCount = 7,
                titles = persistentListOf(
                    RecommendedPerson.Title(movie = PreviewData.movie1),
                    RecommendedPerson.Title(movie = PreviewData.movie2),
                    RecommendedPerson.Title(show = PreviewData.show1),
                ),
            ),
        )
    }
}
