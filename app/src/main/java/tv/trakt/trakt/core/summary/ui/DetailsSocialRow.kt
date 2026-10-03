package tv.trakt.trakt.core.summary.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.reactions.MediaReactionsTarget
import tv.trakt.trakt.core.reactions.media.MediaReactionsView
import tv.trakt.trakt.core.reactions.media.MediaReactionsViewModel
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.ui.header.social.DetailsHeaderSocialHorizontalChip
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark

private val RowTopSpace = 20.dp
private val RowItemsSpace = 24.dp

private const val ENTER_DELAY_MS = 300

@Composable
internal fun DetailsSocialRow(
    target: MediaReactionsTarget,
    socials: ImmutableList<MediaSocialActivity>?,
    onActivityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reactionsViewModel: MediaReactionsViewModel? = when {
        LocalInspectionMode.current -> null
        else -> koinViewModel(parameters = { parametersOf(target) })
    }
    val reactionsState = reactionsViewModel?.state?.collectAsStateWithLifecycle()

    val isLoaded = socials != null && reactionsState?.value?.loading?.isDone != false
    val users = remember(socials?.size) {
        socials?.map { it.user }?.toImmutableList()
    }

    Box(
        contentAlignment = Center,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(200, delayMillis = 250),
            ),
    ) {
        Row(
            horizontalArrangement = spacedBy(RowItemsSpace),
            verticalAlignment = CenterVertically,
        ) {
            reactionsViewModel?.let {
                MediaReactionsView(
                    viewModel = it,
                    visible = isLoaded,
                    modifier = Modifier.padding(top = RowTopSpace),
                )
            }

            AnimatedVisibility(
                visible = isLoaded && !socials.isEmpty(),
                enter = fadeIn(tween(200, delayMillis = ENTER_DELAY_MS)),
                exit = fadeOut(tween(200, delayMillis = ENTER_DELAY_MS)),
                modifier = Modifier
                    .padding(top = RowTopSpace)
                    .onClick(onClick = onActivityClick),
            ) {
                DetailsHeaderSocialHorizontalChip(
                    users = users ?: EmptyImmutableList,
                )
            }
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun Preview() {
    TraktThemeLightDark {
        DetailsSocialRow(
            target = MediaReactionsTarget(
                type = MediaType.Movie,
                id = TraktId(1),
            ),
            socials = persistentListOf(
                MediaSocialActivity(
                    type = MediaType.Movie,
                    user = PreviewData.user1,
                    watched = null,
                    watchlist = null,
                ),
            ),
            onActivityClick = {},
        )
    }
}
