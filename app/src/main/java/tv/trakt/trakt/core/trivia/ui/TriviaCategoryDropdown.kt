package tv.trakt.trakt.core.trivia.ui

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.core.trivia.model.TriviaCategory
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.chips.FilterChip
import tv.trakt.trakt.ui.theme.TraktTheme

private val ChevronSize = 12.dp
private val CheckSize = 14.dp

@Composable
internal fun TriviaCategoryDropdown(
    categories: ImmutableList<TriviaCategory>,
    selectedCategory: TriviaCategory?,
    onCategoryClick: (TriviaCategory?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }

    val labeledCategories = categories
        .map { it to stringResource(it.displayRes) }
        .sortedBy { it.second }

    Box(modifier = modifier) {
        FilterChip(
            selected = selectedCategory != null,
            text = stringResource(
                selectedCategory?.displayRes ?: R.string.option_text_trivia_category_all,
            ),
            height = 32.dp,
            animated = false,
            endAlwaysVisible = true,
            paddingHorizontal = PaddingValues(start = 9.dp, end = 11.dp),
            endContent = {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_down_small),
                    contentDescription = null,
                    tint = when {
                        selectedCategory != null -> TraktTheme.colors.textPrimaryOnAccent
                        else -> TraktTheme.colors.textPrimary
                    },
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(ChevronSize)
                        .rotate(if (showMenu) 180F else 0F),
                )
            },
            onClick = { showMenu = true },
        )

        DropdownMenu(
            expanded = showMenu,
            containerColor = TraktTheme.colors.dropdownMenuContainer,
            shape = RoundedCornerShape(16.dp),
            offset = DpOffset(0.dp, 4.dp),
            onDismissRequest = { showMenu = false },
        ) {
            CategoryItem(
                text = stringResource(R.string.option_text_trivia_category_all),
                selected = selectedCategory == null,
                onClick = {
                    showMenu = false
                    onCategoryClick(null)
                },
            )

            for ((category, label) in labeledCategories) {
                CategoryItem(
                    text = label,
                    selected = selectedCategory == category,
                    onClick = {
                        showMenu = false
                        onCategoryClick(category)
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = {
            Row(
                horizontalArrangement = spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (selected) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(CheckSize),
                    )
                }

                Text(
                    text = text,
                    style = TraktTheme.typography.buttonTertiary,
                    color = TraktTheme.colors.textPrimary,
                )
            }
        },
        onClick = onClick,
    )
}

@Preview
@Composable
private fun TriviaCategoryDropdownPreview() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TriviaCategoryDropdown(
                categories = persistentListOf(
                    TriviaCategory.BehindTheScenes,
                    TriviaCategory.CastAndCrew,
                ),
                selectedCategory = null,
                onCategoryClick = {},
            )
            TriviaCategoryDropdown(
                categories = persistentListOf(
                    TriviaCategory.BehindTheScenes,
                    TriviaCategory.CastAndCrew,
                ),
                selectedCategory = TriviaCategory.CastAndCrew,
                onCategoryClick = {},
            )
        }
    }
}
