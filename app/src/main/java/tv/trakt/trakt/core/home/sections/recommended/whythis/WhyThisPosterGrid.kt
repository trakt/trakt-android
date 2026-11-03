package tv.trakt.trakt.core.home.sections.recommended.whythis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.VerticalImageAspectRatio

private const val GRID_COLUMNS = 4
private val CARD_CORNER = 13.dp

internal sealed interface WhyThisGridCell {
    data class Poster(
        val title: String,
        val imageUrl: String?,
    ) : WhyThisGridCell

    data class More(
        val count: Int,
    ) : WhyThisGridCell
}

@Composable
internal fun WhyThisPosterGrid(
    cells: List<WhyThisGridCell>,
    modifier: Modifier = Modifier,
) {
    val gridSpacing = TraktTheme.spacing.mainGridHorizontalSpace
    Column(
        verticalArrangement = spacedBy(gridSpacing),
        modifier = modifier.fillMaxWidth(),
    ) {
        cells.chunked(GRID_COLUMNS).forEach { row ->
            Row(
                horizontalArrangement = spacedBy(gridSpacing),
                modifier = Modifier.fillMaxWidth(),
            ) {
                row.forEach { cell ->
                    when (cell) {
                        is WhyThisGridCell.Poster -> VerticalMediaCard(
                            title = cell.title,
                            imageUrl = cell.imageUrl,
                            more = false,
                            corner = CARD_CORNER,
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = TraktTheme.colors.shadowSmall,
                                pressedElevation = TraktTheme.colors.shadowSmall,
                                disabledElevation = TraktTheme.colors.shadowSmall,
                            ),
                            modifier = Modifier.weight(1F),
                        )

                        is WhyThisGridCell.More -> MoreCell(
                            count = cell.count,
                            modifier = Modifier.weight(1F),
                        )
                    }
                }

                repeat(GRID_COLUMNS - row.size) {
                    Spacer(modifier = Modifier.weight(1F))
                }
            }
        }
    }
}

@Composable
private fun MoreCell(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(VerticalImageAspectRatio)
            .clip(RoundedCornerShape(CARD_CORNER))
            .background(TraktTheme.colors.placeholderContainer),
    ) {
        Text(
            text = stringResource(R.string.text_recommendation_person_more_titles, count),
            style = TraktTheme.typography.paragraphSmaller,
            color = TraktTheme.colors.textPrimary,
        )
    }
}
