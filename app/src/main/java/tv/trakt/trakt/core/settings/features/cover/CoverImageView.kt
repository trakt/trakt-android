package tv.trakt.trakt.core.settings.features.cover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight.Companion.W400
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.HorizontalImageAspectRatio
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun CoverImageView(
    viewModel: CoverImageViewModel,
    modifier: Modifier = Modifier,
    onImageSet: () -> Unit,
    onDismiss: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.loading) {
        if (state.loading.isDone) {
            onImageSet()
        }
    }

    CoverImageContent(
        state = state,
        modifier = modifier,
        onConfirm = viewModel::setCoverImage,
        onDismiss = onDismiss,
    )
}

@Composable
private fun CoverImageContent(
    state: CoverImageState,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.button_text_set_cover_image),
            subtitle = stringResource(R.string.warning_prompt_set_cover_image, state.mediaTitle),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = DefaultCardShape,
                    clip = false,
                )
                .clip(DefaultCardShape)
                .background(TraktTheme.colors.skeletonContainer)
                .aspectRatio(HorizontalImageAspectRatio),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(state.mediaImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                onError = {
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(HorizontalImageAspectRatio),
            )
        }

        state.error?.let {
            Text(
                text = state.error.message ?: state.error.toString(),
                color = Red500,
                style = TraktTheme.typography.meta.copy(fontWeight = W400),
                maxLines = 5,
                overflow = Ellipsis,
            )
        }

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.button_text_yes),
                enabled = state.loading.isIdle,
                loading = !state.loading.isIdle,
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton(
                text = stringResource(R.string.button_text_cancel),
                enabled = state.loading.isIdle,
                containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        CoverImageContent(
            state = CoverImageState(
                mediaId = 1.toTraktId(),
                mediaTitle = "The Matrix",
                mediaType = MediaType.Show,
                mediaImage = "",
            ),
        )
    }
}
