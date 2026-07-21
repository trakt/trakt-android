package tv.trakt.trakt.core.share

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults.toggleButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.share.model.ShareImageVariant
import tv.trakt.trakt.core.share.model.ShareImageVariant.Default
import tv.trakt.trakt.core.share.model.ShareImageVariant.Feed
import tv.trakt.trakt.core.share.model.ShareImageVariant.Story
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme
import java.io.File
import java.net.URL

@Composable
internal fun ShareView(
    viewModel: ShareViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ShareContent(
        state = state,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShareContent(
    state: ShareState,
    modifier: Modifier = Modifier,
) {
    var selectedVariant by remember { mutableStateOf(Default) }
    var loadingVariant by remember { mutableStateOf(false) }
    var isError by remember(selectedVariant) { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        TraktHeader(
            title = stringResource(R.string.button_text_share),
            subtitle = stringResource(R.string.dialog_prompt_media_share_image_format),
        )

        ShareButtons(
            selectedVariant = selectedVariant,
            onVariantSelected = {
                selectedVariant = it
            },
            modifier = Modifier
                .fillMaxWidth(),
        )

        state.media?.let { media ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            selectedVariant.getImageUri(
                                mediaType = media.type.value,
                                mediaSlug = media.slug.value,
                            ),
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = "",
                    contentScale = when (selectedVariant) {
                        Default, Feed -> ContentScale.Fit
                        Story -> ContentScale.FillHeight
                    },
                    onLoading = {
                        loadingVariant = true
                    },
                    onSuccess = {
                        loadingVariant = false
                    },
                    onError = { error ->
                        isError = error.result.throwable.toString()
                        loadingVariant = false
                    },
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                )

                if (loadingVariant) {
                    FilmProgressIndicator()
                }

                if (isError != null) {
                    Text(
                        text = "$isError",
                        style = TraktTheme.typography.paragraphSmaller,
                        color = TraktTheme.colors.textSecondary,
                        textAlign = Center,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        Row(
            horizontalArrangement = spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            val isInstagramAvailable = remember {
                context.packageManager.getLaunchIntentForPackage("com.instagram.android") != null
            }

            if (selectedVariant == Story && isInstagramAvailable) {
                val isLoading = state.media == null || loadingVariant
                PrimaryButton(
                    text = stringResource(R.string.button_text_media_share_format_story),
                    border = BorderStroke(1.dp, TraktTheme.colors.chipContainer),
                    icon = painterResource(R.drawable.ic_instagram),
                    iconSize = 24.dp,
                    iconSpace = 4.dp,
                    containerColor = Color.Transparent,
                    onClick = {
                        if (isLoading) {
                            return@PrimaryButton
                        }
                        scope.launch {
                            shareToInstagramStories(
                                context = context,
                                imageUri = selectedVariant.getImageUri(
                                    mediaType = state.media.type.value,
                                    mediaSlug = state.media.slug.value,
                                ),
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .alpha(if (isLoading) 0.2f else 1f),
                )
            }

            PrimaryButton(
                text = stringResource(R.string.button_text_share),
                enabled = state.media != null && !loadingVariant && isError == null,
                onClick = {
                    val media = state.media ?: return@PrimaryButton
                    val imageUri = selectedVariant.getImageUri(
                        mediaType = media.type.value,
                        mediaSlug = media.slug.value,
                    )
                    scope.launch {
                        shareImage(
                            context = context,
                            imageUri = imageUri,
                        )
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ShareButtons(
    modifier: Modifier = Modifier,
    selectedVariant: ShareImageVariant,
    onVariantSelected: (ShareImageVariant) -> Unit = {},
) {
    Row(
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier,
    ) {
        ShareImageVariant.entries.forEach { variant ->
            ToggleButton(
                checked = selectedVariant == variant,
                onCheckedChange = {
                    if (selectedVariant != variant) {
                        onVariantSelected(variant)
                    }
                },
                colors = toggleButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = TraktTheme.colors.textPrimary,
                    checkedContainerColor = TraktTheme.colors.accent,
                    checkedContentColor = TraktTheme.colors.textPrimary,
                ),
                border = when {
                    selectedVariant == variant -> BorderStroke(
                        width = 0.dp,
                        color = Color.Transparent,
                    )

                    else -> BorderStroke(
                        width = 1.dp,
                        color = TraktTheme.colors.chipContainer,
                    )
                },
                shapes = when (variant) {
                    Default -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    Feed -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    Story -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp),
            ) {
                Text(
                    text = stringResource(variant.displayRes),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.buttonTertiary,
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
        }
    }
}

private suspend fun prepareShareFile(
    context: Context,
    imageUri: Uri,
): Uri {
    val bytes = withContext(Dispatchers.IO) {
        URL(imageUri.toString()).openStream().use { it.readBytes() }
    }

    val shareDir = File(context.cacheDir, "share")
    shareDir.deleteRecursively()
    shareDir.mkdirs()

    val file = File(shareDir, "share_image.png")
    withContext(Dispatchers.IO) {
        file.writeBytes(bytes)
    }

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

private suspend fun shareImage(
    context: Context,
    imageUri: Uri,
) {
    try {
        val contentUri = prepareShareFile(context, imageUri)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            clipData = ClipData.newRawUri("", contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, null))
    } catch (error: Exception) {
        error.rethrowCancellation {
            Timber.recordError(error)
        }
    }
}

private suspend fun shareToInstagramStories(
    context: Context,
    imageUri: Uri,
) {
    try {
        val contentUri = prepareShareFile(context, imageUri)
        val intent = Intent("com.instagram.share.ADD_TO_STORY").apply {
            setDataAndType(contentUri, "image/png")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.instagram.android")
        }
        context.startActivity(intent)
    } catch (error: Exception) {
        error.rethrowCancellation {
            Timber.recordError(error)
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    widthDp = 400,
)
@Composable
private fun PreviewShareContent() {
    TraktTheme {
        ShareContent(
            state = ShareState(
                media = ShareState.Media(
                    type = MediaType.Movie,
                    slug = "the-matrix".toSlugId(),
                ),
            ),
        )
    }
}
