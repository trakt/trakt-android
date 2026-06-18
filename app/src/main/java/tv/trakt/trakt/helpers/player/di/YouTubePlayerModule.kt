package tv.trakt.trakt.helpers.player.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.helpers.player.YouTubePlayerViewModel

internal val youTubePlayerModule = module {
    viewModelOf(::YouTubePlayerViewModel)
}
