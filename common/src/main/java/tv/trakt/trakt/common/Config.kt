package tv.trakt.trakt.common

object Config {
    const val DEFAULT_COUNTRY_CODE: String = "us"

    const val API_BASE_URL: String = "https://apiz.trakt.tv/"
    const val PLEX_BASE_URL: String = "https://watch.plex.tv/"

    const val WEB_BASE_URL: String = "https://trakt.tv/"
    const val WEB_V3_BASE_URL: String = "https://app.trakt.tv/"
    const val WEB_ABOUT_US_URL: String = "https://trakt.tv/about?native_app_mode=true"
    const val WEB_TERMS_URL: String = "https://trakt.tv/terms?native_app_mode=true"
    const val WEB_PRIVACY_URL: String = "https://trakt.tv/privacy?native_app_mode=true"
    const val WEB_FORUMS_URL: String = "https://forums.trakt.tv/"
    const val WEB_SETTINGS_URL: String = "https://trakt.tv/settings?native_app_mode=true"
    const val WEB_SETTINGS_SCROBBLING_URL: String = "https://trakt.tv/settings/scrobblers?native_app_mode=true"

    const val WEB_SOCIAL_INSTAGRAM_URL: String = "https://www.instagram.com/trakt_app/"
    const val WEB_SOCIAL_X_URL: String = "https://x.com/trakt"

    const val WEB_SUPPORT_MAIL: String = "support@trakt.tv"
    const val WEB_GOOGLE_SUBSCRIPTIONS: String = "https://play.google.com/store/account/subscriptions"

    fun webUserUrl(userId: String): String = "https://trakt.tv/users/$userId?native_app_mode=true"

    fun webListUrl(
        userId: String,
        listId: String,
    ): String = "https://app.trakt.tv/users/$userId/lists/$listId"

    fun webYearReviewUrl(
        user: String,
        year: Int,
    ): String = "https://trakt.tv/users/$user/year/$year?native_app_mode=true"

    fun webMonthReviewUrl(
        user: String,
        month: Int,
        year: Int,
    ): String {
        return "https://trakt.tv/users/$user/mir/$year/$month?native_app_mode=true"
    }

    fun webImdbPersonUrl(imdbId: String): String {
        return "https://www.imdb.com/name/$imdbId/"
    }
}
