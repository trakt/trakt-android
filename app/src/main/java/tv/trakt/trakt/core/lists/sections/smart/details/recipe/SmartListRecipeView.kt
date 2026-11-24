package tv.trakt.trakt.core.lists.sections.smart.details.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W600
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.lists.SmartListRecipe
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private val CardShape = DefaultCardShape
private val ChipShape = RoundedCornerShape(100)

@Composable
internal fun SmartListRecipeView(
    recipe: SmartListRecipe,
    listName: String,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        contentPadding = PaddingValues(
            bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        overscrollEffect = null,
        modifier = modifier,
    ) {
        item(key = "header") {
            TraktHeader(
                title = stringResource(R.string.header_smart_list_recipe),
                subtitle = listName,
            )
        }

        item(key = "source") {
            RecipeSection(
                title = stringResource(R.string.text_source),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                ) {
                    Text(
                        text = recipe.source,
                        color = TraktTheme.colors.textPrimary,
                        style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W600),
                        maxLines = 1,
                        overflow = Ellipsis,
                        modifier = Modifier.weight(1F),
                    )
                    Text(
                        text = recipe.mediaType,
                        color = TraktTheme.colors.textSecondary,
                        style = TraktTheme.typography.meta.copy(
                            fontSize = 12.sp,
                        ),
                    )
                }
            }
        }

        if (recipe.includes.isNotEmpty()) {
            item(key = "includes") {
                RecipeSection(title = stringResource(R.string.header_smart_list_includes)) {
                    recipe.includes.forEachIndexed { index, row ->
                        if (index > 0) {
                            HorizontalDivider(
                                color = TraktTheme.colors.chipContainer,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                        IncludeRow(row = row)
                    }
                }
            }
        }

        if (recipe.limits.isNotEmpty()) {
            item(key = "limits") {
                RecipeSection(title = stringResource(R.string.header_smart_list_limits)) {
                    RecipeChips(labels = recipe.limits)
                }
            }
        }

        if (recipe.hidden.isNotEmpty()) {
            item(key = "hidden") {
                RecipeSection(title = stringResource(R.string.header_smart_list_hide)) {
                    RecipeChips(labels = recipe.hidden)
                }
            }
        }

        if (recipe.isEmpty) {
            item(key = "everything") {
                Text(
                    text = stringResource(R.string.text_smart_list_recipe_everything),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraphSmaller,
                )
            }
        }
    }
}

@Composable
private fun RecipeSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier.padding(top = 8.dp),
    ) {
        Text(
            text = title,
            style = TraktTheme.typography.paragraphSmaller.copy(fontWeight = W600),
            color = TraktTheme.colors.textSecondary,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = TraktTheme.colors.shadowDynamicSmall, shape = CardShape)
                .background(TraktTheme.colors.dialogOnContainer, CardShape)
                .padding(16.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun IncludeRow(
    row: SmartListRecipe.Include,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = row.label,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.meta.copy(
                fontSize = 12.sp,
            ),
        )

        FlowRow(
            horizontalArrangement = spacedBy(4.dp),
            verticalArrangement = spacedBy(4.dp),
        ) {
            row.values.forEach { value ->
                RecipeChip(
                    label = value.label,
                    excluded = value.isExcluded,
                )
            }
        }

        if (row.details.isNotEmpty()) {
            Text(
                text = row.details.joinToString(" · "),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta.copy(
                    fontSize = 12.sp,
                ),
            )
        }
    }
}

@Composable
private fun RecipeChips(
    labels: ImmutableList<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        horizontalArrangement = spacedBy(4.dp),
        verticalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        labels.forEach { label ->
            RecipeChip(label = label)
        }
    }
}

@Composable
private fun RecipeChip(
    label: String,
    modifier: Modifier = Modifier,
    excluded: Boolean = false,
) {
    val chipModifier = when {
        excluded -> modifier.border(1.dp, TraktTheme.colors.chipContainer, ChipShape)
        else -> modifier.background(TraktTheme.colors.chipContainer, ChipShape)
    }

    Text(
        text = label,
        color = when {
            excluded -> TraktTheme.colors.textSecondary
            else -> TraktTheme.colors.textPrimary
        },
        style = TraktTheme.typography.meta.copy(
            fontSize = 12.sp,
        ),
        textDecoration = TextDecoration.LineThrough.takeIf { excluded },
        maxLines = 1,
        overflow = Ellipsis,
        modifier = chipModifier.padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SmartListRecipeView(
            listName = "Unwatched sci-fi from the 2010s",
            recipe = SmartListRecipe(
                source = "Most Popular",
                mediaType = "Movies",
                includes = persistentListOf(
                    SmartListRecipe.Include(
                        key = "genres",
                        label = "Genre",
                        values = persistentListOf(
                            SmartListRecipe.Value(label = "Science Fiction", isExcluded = false),
                            SmartListRecipe.Value(label = "Thriller", isExcluded = false),
                            SmartListRecipe.Value(label = "Horror", isExcluded = true),
                        ),
                        details = persistentListOf("Match: All"),
                    ),
                    SmartListRecipe.Include(
                        key = "countries",
                        label = "Country",
                        values = persistentListOf(
                            SmartListRecipe.Value(label = "United Kingdom", isExcluded = false),
                        ),
                        details = EmptyImmutableList,
                    ),
                ),
                limits = persistentListOf("2010 - 2019", "Trakt 70% to 100%"),
                hidden = persistentListOf("Watched"),
            ),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewEmpty() {
    TraktThemePreview {
        SmartListRecipeView(
            listName = "Everything trending",
            recipe = SmartListRecipe(
                source = "Trending",
                mediaType = "Media",
                includes = EmptyImmutableList,
                limits = EmptyImmutableList,
                hidden = EmptyImmutableList,
            ),
        )
    }
}
