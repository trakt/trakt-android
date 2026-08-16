package tv.trakt.trakt.common.helpers.coil

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.gif.AnimatedImageDecoder
import coil3.svg.SvgDecoder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.mp.KoinPlatform
import kotlin.time.Duration.Companion.seconds

val coilModule = module {
    single<ImageLoader> {
        ImageLoader.Builder(androidContext())
            .timeout(15.seconds)
            .components {
                add(TimeoutInterceptor())
                add(SvgDecoder.Factory())
                // Animated GIF / WebP playback (KLIPY GIFs); without it only the first frame renders.
                add(AnimatedImageDecoder.Factory())
            }
            .build()
    }
}

fun registerCoilImageLoader() {
    SingletonImageLoader.setSafe { _ -> KoinPlatform.getKoin().get<ImageLoader>() }
}
