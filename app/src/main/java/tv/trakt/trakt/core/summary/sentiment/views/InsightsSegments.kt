package tv.trakt.trakt.core.summary.sentiment.views

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.segmentShape

private val MarkdownHeading = Regex("""^(#{1,6}\s+.+|\*\*[^*]+\*\*:?)\s*$""")

@Composable
internal fun InsightsSegments(
    count: Int,
    modifier: Modifier = Modifier,
    header: String? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    collapsed: Boolean? = null,
    onCollapseClick: () -> Unit = {},
    segment: @Composable ColumnScope.(index: Int) -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        header?.let {
            InsightsSegmentsHeader(
                title = it,
                collapsed = collapsed,
                onCollapseClick = onCollapseClick,
            )
        }

        if (collapsed != true) {
            Column(
                verticalArrangement = spacedBy(3.dp),
            ) {
                repeat(count) { index ->
                    Column(
                        verticalArrangement = spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = TraktTheme.colors.shadowDynamicSmall,
                                shape = segmentShape(index = index, count = count),
                                clip = true,
                            )
                            .background(TraktTheme.colors.commentContainer)
                            .padding(contentPadding),
                    ) {
                        segment(index)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsSegmentsHeader(
    title: String,
    collapsed: Boolean?,
    onCollapseClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (collapsed == true) -180F else 0F,
        animationSpec = tween(200),
        label = "rotation",
    )

    Row(
        horizontalArrangement = spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .onClick(enabled = collapsed != null, onClick = onCollapseClick),
    ) {
        Text(
            text = title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading5,
            modifier = Modifier.weight(1F),
        )
        if (collapsed != null) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_dropdown),
                contentDescription = null,
                tint = TraktTheme.colors.textSecondary,
                modifier = Modifier
                    .rotate(rotation)
                    .size(16.dp),
            )
        }
    }
}

internal data class MarkdownSection(
    val title: String?,
    val body: String,
)

internal fun String.markdownSections(): ImmutableList<MarkdownSection> {
    return lines()
        .fold(listOf<List<String>>()) { sections, line ->
            when {
                sections.isEmpty() || MarkdownHeading.matches(line) -> sections + listOf(listOf(line))
                else -> sections.dropLast(1) + listOf(sections.last() + line)
            }
        }
        .map { lines ->
            val title = lines.first().takeIf { MarkdownHeading.matches(it) }
            val body = lines.drop(if (title != null) 1 else 0).joinToString("\n").trim()
            MarkdownSection(title = title?.trim(), body = body)
        }
        .filter { it.title != null || it.body.isNotEmpty() }
        .toImmutableList()
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    val sections = persistentListOf("First", "Second", "Third")
    TraktThemePreview {
        InsightsSegments(
            header = "Header",
            count = sections.size,
            modifier = Modifier.padding(16.dp),
        ) { index ->
            Text(
                text = sections[index],
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.paragraphSmall,
            )
        }
    }
}
