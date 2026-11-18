package tv.trakt.trakt.app.core.details.ratings.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenu
import tv.trakt.trakt.app.common.ui.menus.TvDropdownMenuItem
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.resources.R

private val RatingOptions = (10 downTo 1).toList()

@Composable
internal fun RatingMenu(
    expanded: Boolean,
    rating: Int?,
    onDismissRequest: () -> Unit,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    TvDropdownMenu(
        visible = expanded,
        onDismiss = onDismissRequest,
        modifier = modifier,
    ) {
        val resources = LocalResources.current
        val initialIndex = RatingOptions.indexOf(rating).coerceAtLeast(0)
        val initialFocus = remember { FocusRequester() }
        var focusedIndex by remember { mutableIntStateOf(initialIndex) }

        LaunchedEffect(Unit) {
            initialFocus.requestFocus()
        }

        RatingOptions.forEachIndexed { index, option ->
            val stars = option / 2f
            TvDropdownMenuItem(
                text = "${stars.toString().removeSuffix(".0")} • ${UserRating.getSlug(stars, resources)}",
                focused = focusedIndex == index,
                onClick = {
                    onDismissRequest()
                    if (option != rating) {
                        onSelect(option)
                    }
                },
                modifier = Modifier
                    .ifOrElse(
                        condition = index == initialIndex,
                        isTrue = Modifier.focusRequester(initialFocus),
                    )
                    .onFocusChanged {
                        if (it.isFocused) focusedIndex = index
                    },
            )
        }

        if (rating != null) {
            val removeIndex = RatingOptions.size
            TvDropdownMenuItem(
                text = stringResource(R.string.text_no_rating),
                focused = focusedIndex == removeIndex,
                onClick = {
                    onDismissRequest()
                    onSelect(null)
                },
                modifier = Modifier.onFocusChanged {
                    if (it.isFocused) focusedIndex = removeIndex
                },
            )
        }
    }
}
