package tv.trakt.trakt.app.core.home

internal object HomeConfig {
    const val HOME_SECTION_LIMIT = 10
    const val HOME_PAGE_LIMIT = 25
    const val HOME_NEXT_PAGE_OFFSET = 5
    const val HOME_SOCIAL_PAGE_LIMIT = 100 // Social API is not paginated, so we fetch a larger amount of items
}
