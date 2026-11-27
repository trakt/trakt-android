package tv.trakt.trakt.core.mixtape.results.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp

/**
 * Sizes the card from the height left by the other slots, then insets the header end and the
 * actions to the card edges in the same pass, so nothing shifts after the first frame.
 */
@Composable
internal fun MixtapeDeckLayout(
    cardRatio: Float,
    spacing: Dp,
    header: @Composable () -> Unit,
    card: @Composable () -> Unit,
    text: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        contents = listOf(header, card, text, actions),
        modifier = modifier,
    ) { (headerMeasurables, cardMeasurables, textMeasurables, actionMeasurables), constraints ->
        val width = constraints.maxWidth
        val gap = spacing.roundToPx()

        val fixedHeight = (headerMeasurables + textMeasurables + actionMeasurables)
            .sumOf { it.maxIntrinsicHeight(width) }
        val cardHeight = (constraints.maxHeight - fixedHeight - gap * 3).coerceAtLeast(0)
        val cardWidth = minOf(width, (cardHeight * cardRatio).toInt())
        val inset = (width - cardWidth) / 2

        val headers = headerMeasurables.map { it.measure(Constraints.fixedWidth(width - inset)) }
        val cards = cardMeasurables.map { it.measure(Constraints.fixed(width, cardHeight)) }
        val texts = textMeasurables.map { it.measure(Constraints.fixedWidth(width)) }
        val actions = actionMeasurables.map { it.measure(Constraints.fixedWidth(width - inset * 2)) }

        layout(width, constraints.maxHeight) {
            var y = 0
            listOf(headers to 0, cards to 0, texts to 0, actions to inset).forEach { (placeables, x) ->
                placeables.forEach {
                    it.place(x, y)
                    y += it.height
                }
                y += gap
            }
        }
    }
}
