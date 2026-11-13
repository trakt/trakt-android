package tv.trakt.trakt.core.settings.usecases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.util.Base64
import androidx.core.graphics.createBitmap
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.core.settings.features.avatar.AvatarCrop
import java.io.ByteArrayOutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private const val AVATAR_SIZE = 440
private const val AVATAR_QUALITY = 90
private const val AVATAR_MAX_DECODE_SIZE = 4096

internal class UpdateUserAvatarUseCase(
    private val context: Context,
    private val remoteSource: UserRemoteSource,
    private val sessionManager: SessionManager,
    private val imageLoader: ImageLoader,
) {
    suspend fun uploadAvatar(
        image: Uri,
        crop: AvatarCrop,
    ) {
        val source = decodeImage(image, crop)
        val avatar = withContext(Dispatchers.Default) {
            encodeAvatar(source, crop)
        }

        remoteSource.updateAvatar(avatar)
        refreshAvatar()
    }

    suspend fun removeAvatar() {
        remoteSource.removeAvatar()
        refreshAvatar()
    }

    private suspend fun decodeImage(
        image: Uri,
        crop: AvatarCrop,
    ): Bitmap {
        val request = ImageRequest.Builder(context)
            .data(image)
            .size(decodeSize(image, crop))
            .precision(Precision.EXACT)
            .allowHardware(false)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .build()

        return when (val result = imageLoader.execute(request)) {
            is SuccessResult -> result.image.toBitmap()
            is ErrorResult -> throw result.throwable
        }
    }

    // Sized so the cropped square still holds AVATAR_SIZE source pixels after downsampling.
    private suspend fun decodeSize(
        image: Uri,
        crop: AvatarCrop,
    ): Int {
        val bounds = withContext(Dispatchers.IO) {
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
                context.contentResolver.openInputStream(image)?.use {
                    BitmapFactory.decodeStream(it, null, this)
                }
            }
        }
        val shortSide = min(bounds.outWidth, bounds.outHeight)
        val aspectRatio = when {
            shortSide > 0 -> max(bounds.outWidth, bounds.outHeight).toFloat() / shortSide
            else -> 1F
        }

        return ceil(AVATAR_SIZE / crop.size * aspectRatio)
            .toInt()
            .coerceIn(AVATAR_SIZE, AVATAR_MAX_DECODE_SIZE)
    }

    private fun encodeAvatar(
        source: Bitmap,
        crop: AvatarCrop,
    ): String {
        val side = (min(source.width, source.height) * crop.size)
            .roundToInt()
            .coerceIn(1, min(source.width, source.height))
        val left = (crop.left * source.width).roundToInt().coerceIn(0, source.width - side)
        val top = (crop.top * source.height).roundToInt().coerceIn(0, source.height - side)

        val avatar = createBitmap(AVATAR_SIZE, AVATAR_SIZE)
        Canvas(avatar).drawBitmap(
            source,
            Rect(left, top, left + side, top + side),
            Rect(0, 0, AVATAR_SIZE, AVATAR_SIZE),
            Paint(Paint.FILTER_BITMAP_FLAG),
        )

        val bytes = ByteArrayOutputStream().use { stream ->
            avatar.compress(Bitmap.CompressFormat.JPEG, AVATAR_QUALITY, stream)
            stream.toByteArray()
        }
        avatar.recycle()

        return "data:image/jpeg;base64,${Base64.encodeToString(bytes, Base64.NO_WRAP)}"
    }

    private suspend fun refreshAvatar() {
        sessionManager.saveAvatarVersion(nowUtcInstant().toEpochMilli())
    }
}
