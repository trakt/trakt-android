package tv.trakt.trakt.core.summary.social.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity
import tv.trakt.trakt.core.summary.ui.header.social.DetailsHeaderSocialHorizontalChip

@Composable
internal fun MediaSocialView(
    modifier: Modifier = Modifier,
    visible: Boolean,
    activity: ImmutableList<MediaSocialActivity>?,
    onActivityClick: () -> Unit,
) {
    Box(
        contentAlignment = Center,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(200, delayMillis = 250),
            ),
    ) {
        val users = remember(activity?.size) {
            activity?.map { it.user }?.toImmutableList()
        }
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200, delayMillis = 350)),
            exit = fadeOut(tween(200, delayMillis = 350)),
            modifier = Modifier
                .padding(top = 20.dp)
                .onClick(onClick = onActivityClick),
        ) {
            DetailsHeaderSocialHorizontalChip(
                users = users ?: EmptyImmutableList,
            )
        }
    }
}
