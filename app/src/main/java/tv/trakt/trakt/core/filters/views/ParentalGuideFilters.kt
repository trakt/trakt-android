package tv.trakt.trakt.core.filters.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.lists.ParentalGuideCategory
import tv.trakt.trakt.common.model.lists.ParentalGuideSeverity
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ParentalGuideFilters(
    rangeOf: (ParentalGuideCategory) -> Pair<Int, Int>?,
    onRangeChange: (ParentalGuideCategory, Pair<Int, Int>?) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable {
        mutableStateOf(ParentalGuideCategory.entries.any { rangeOf(it) != null })
    }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180F else 0F,
        label = "chevronRotation",
    )

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .onClick { expanded = !expanded },
        ) {
            Text(
                text = stringResource(R.string.option_text_certification_parental_guidance),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.buttonTertiary,
            )

            Icon(
                painter = painterResource(R.drawable.ic_arrow_dropdown),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .size(15.dp)
                    .graphicsLayer {
                        rotationZ = chevronRotation
                    },
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(100)),
            exit = shrinkVertically(animationSpec = tween(100)),
        ) {
            Column(
                verticalArrangement = spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
            ) {
                ParentalGuideCategory.entries.forEach { category ->
                    ParentalGuideFilter(
                        category = category,
                        range = rangeOf(category),
                        onRangeChange = { onRangeChange(category, it) },
                        enabled = enabled,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentalGuideFilter(
    category: ParentalGuideCategory,
    range: Pair<Int, Int>?,
    onRangeChange: (Pair<Int, Int>?) -> Unit,
    enabled: Boolean,
) {
    val severityMin = ParentalGuideSeverity.Min
    val severityMax = ParentalGuideSeverity.Max

    val rangeValue = remember(range) {
        mutableStateOf(
            (range?.first ?: severityMin).toFloat()..(range?.second ?: severityMax).toFloat(),
        )
    }

    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(category.displayRes),
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.buttonTertiary,
            maxLines = 1,
            overflow = Ellipsis,
        )

        RangeSlider(
            value = rangeValue.value,
            enabled = enabled,
            valueRange = severityMin.toFloat()..severityMax.toFloat(),
            steps = severityMax - severityMin - 1,
            onValueChange = { value ->
                rangeValue.value = value
            },
            onValueChangeFinished = {
                val start = rangeValue.value.start.toInt()
                val end = rangeValue.value.endInclusive.toInt()
                onRangeChange(
                    when {
                        start == severityMin && end == severityMax -> null
                        else -> start to end
                    },
                )
            },
            colors = SliderDefaults.colors(
                activeTrackColor = TraktTheme.colors.accent,
                inactiveTrackColor = Color.White,
                thumbColor = TraktTheme.colors.accent,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
                disabledActiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledActiveTickColor = TraktTheme.colors.dialogOnContainer,
                disabledThumbColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTrackColor = TraktTheme.colors.dialogOnContainer,
                disabledInactiveTickColor = TraktTheme.colors.dialogOnContainer,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(25.dp),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            ParentalGuideSeverity.entries.forEach { severity ->
                Text(
                    text = stringResource(severity.displayRes),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        ParentalGuideFilters(
            rangeOf = { category ->
                when (category) {
                    ParentalGuideCategory.Violence -> 0 to 1
                    ParentalGuideCategory.Frightening -> 2 to 3
                    else -> null
                }
            },
            onRangeChange = { _, _ -> },
        )
    }
}
