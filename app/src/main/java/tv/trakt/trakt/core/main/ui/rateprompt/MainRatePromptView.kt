package tv.trakt.trakt.core.main.ui.rateprompt

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.LocalRatePromptVisibility
import tv.trakt.trakt.core.main.MainState
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptMedia
import tv.trakt.trakt.core.ratings.rateprompt.model.RatePromptState.UnratedMovies
import tv.trakt.trakt.core.ratings.rateprompt.ui.RatePromptView
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ColumnScope.MainRatePromptView(
    state: MainState,
    onMediaClick: (RatePromptMedia) -> Unit = {},
) {
    val localVisibility = LocalRatePromptVisibility.current

    val ratePrompt = state.ratePrompt
    val ratePromptMovies = (ratePrompt as? UnratedMovies)?.movies.orEmpty()
    val ratePromptFavorites = (ratePrompt as? UnratedMovies)?.favorites.orEmpty()

    AnimatedVisibility(
        visible = remember(ratePrompt, ratePromptMovies, localVisibility.value) {
            ratePrompt is UnratedMovies && localVisibility.value
        },
        enter = fadeIn(tween(250)) + slideInVertically(initialOffsetY = { it / 10 }),
        exit = fadeOut(tween(200)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = TraktTheme.spacing.mainPageHorizontalSpace - 8.dp),
    ) {
        ratePromptMovies
            .firstOrNull()
            ?.let { movie ->
                val media = RatePromptMedia(
                    movie = movie,
                    favorite = ratePromptFavorites.contains(movie.ids.trakt),
                )

                val moreMedia = ratePromptMovies
                    .filter { it.ids.trakt != movie.ids.trakt }
                    .map { movie ->
                        RatePromptMedia(
                            movie = movie,
                            favorite = ratePromptFavorites.contains(movie.ids.trakt),
                        )
                    }

                RatePromptView(
                    viewModel = koinViewModel(
                        key = media.movie.ids.trakt.value.toString(),
                    ) {
                        parametersOf(media, moreMedia)
                    },
                    media = media,
                    onMediaClick = {
                        onMediaClick(media)
                    },
                )
            }
    }
}
