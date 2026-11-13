package tv.trakt.trakt.core.settings.features.avatar

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

private const val PREVIEW_DECODE_SIZE = 1024

@Composable
internal fun AvatarCropView(
    image: Uri,
    modifier: Modifier = Modifier,
    onConfirm: (AvatarCrop) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image)
            .size(PREVIEW_DECODE_SIZE)
            .build(),
    )
    val painterState by painter.state.collectAsStateWithLifecycle()
    val imageSize = (painterState as? AsyncImagePainter.State.Success)
        ?.painter
        ?.intrinsicSize

    AvatarCropContent(
        imageSize = imageSize,
        modifier = modifier,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    ) { layoutModifier ->
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = layoutModifier,
        )
    }
}

@Composable
private fun AvatarCropContent(
    imageSize: Size?,
    modifier: Modifier = Modifier,
    onConfirm: (AvatarCrop) -> Unit = {},
    onDismiss: () -> Unit = {},
    image: @Composable (Modifier) -> Unit = {},
) {
    val density = LocalDensity.current

    var viewport by remember { mutableFloatStateOf(0F) }
    var zoom by remember { mutableFloatStateOf(AVATAR_MIN_ZOOM) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val isReady = imageSize != null && imageSize.minDimension > 0 && viewport > 0

    Column(
        verticalArrangement = spacedBy(24.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.dialog_title_adjust_photo),
            subtitle = stringResource(R.string.label_drag_image_to_adjust),
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(1F, fill = false)
                .aspectRatio(1F, matchHeightConstraintsFirst = true)
                .clip(DefaultCardShape)
                .background(TraktTheme.colors.skeletonContainer)
                .onSizeChanged { viewport = it.width.toFloat() }
                .pointerInput(imageSize, viewport) {
                    if (imageSize == null || !isReady) return@pointerInput
                    detectTransformGestures { _, pan, zoomChange, _ ->
                        zoom = (zoom * zoomChange).coerceIn(AVATAR_MIN_ZOOM, AVATAR_MAX_ZOOM)
                        offset = AvatarCrop.clampOffset(
                            viewport = viewport,
                            image = imageSize,
                            zoom = zoom,
                            offset = offset + pan,
                        )
                    }
                },
        ) {
            if (imageSize != null && isReady) {
                val fitScale = viewport / imageSize.minDimension
                val width = with(density) { (imageSize.width * fitScale).toDp() }
                val height = with(density) { (imageSize.height * fitScale).toDp() }

                image(
                    Modifier
                        .requiredSize(width, height)
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = offset.x
                            translationY = offset.y
                        },
                )
            }
        }

        Column(
            verticalArrangement = spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
        ) {
            PrimaryButton(
                text = stringResource(R.string.button_text_upload_photo),
                enabled = isReady,
                onClick = {
                    if (imageSize == null) return@PrimaryButton
                    onConfirm(
                        AvatarCrop.fromViewport(
                            viewport = viewport,
                            image = imageSize,
                            zoom = zoom,
                            offset = offset,
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryButton(
                text = stringResource(R.string.button_text_cancel),
                containerColor = TraktTheme.colors.primaryButtonContainerDisabled,
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        AvatarCropContent(
            imageSize = null,
            modifier = Modifier.padding(24.dp),
        )
    }
}
