package tv.trakt.trakt.common

object Config {
    const val DEFAULT_COUNTRY_CODE: String = "us"

    const val API_BASE_URL: String = "https://apiz.trakt.tv/"
    const val API_V3_BASE_URL: String = "https://apiz.trakt.tv/v3/"
    const val PLEX_BASE_URL: String = "https://watch.plex.tv/"

    const val WEB_BASE_URL: String = "https://trakt.tv/"
    const val WEB_BASE_URL_NEW: String = "https://auth.trakt.tv/"
    const val WEB_V3_BASE_URL: String = "https://app.trakt.tv/"
    const val WEB_VIP_URL: String = "https://app.trakt.tv/vip?native_app_mode=true"
    const val WEB_ABOUT_US_URL: String = "https://app.trakt.tv/about?native_app_mode=true"
    const val WEB_TERMS_URL: String = "https://app.trakt.tv/terms?native_app_mode=true"
    const val WEB_PRIVACY_URL: String = "https://app.trakt.tv/privacy?native_app_mode=true"
    const val WEB_FORUMS_URL: String = "https://forums.trakt.tv/"
    const val WEB_ROADMAP_URL: String = "https://roadmap.trakt.tv/"
    const val WEB_SETTINGS_URL: String = "https://app.trakt.tv/settings?native_app_mode=true"
    const val WEB_SETTINGS_SCROBBLING_URL: String = "https://trakt.tv/settings/scrobblers?native_app_mode=true"
    const val WEB_TRANSLATE_URL: String = "https://crwd.in/trakt-poc/3857d5ea667dd425fbd0cb2e4e80dc192749600"
    const val WEB_DATA_IMPORT_URL: String = "https://app.trakt.tv/settings/data?native_app_mode=true"

    const val WEB_SOCIAL_GITHUB_URL: String = "https://github.com/trakt/trakt-android"
    const val WEB_SOCIAL_INSTAGRAM_URL: String = "https://www.instagram.com/trakt_app/"
    const val WEB_SOCIAL_X_URL: String = "https://x.com/trakt"

    const val WEB_SUPPORT_MAIL: String = "support@trakt.tv"
    const val WEB_GOOGLE_SUBSCRIPTIONS: String = "https://play.google.com/store/account/subscriptions"

    fun apiUserAgent(): String =
        "Trakt Android/${BuildConfig.VERSION_NAME} (tv.trakt.trakt; build:${BuildConfig.VERSION_CODE}; android:${android.os.Build.VERSION.SDK_INT})"

    fun webListUrl(
        userId: String,
        listId: String,
    ): String = "https://app.trakt.tv/users/$userId/lists/$listId"

    fun webYearReviewUrl(
        user: String,
        year: Int,
    ): String = "https://app.trakt.tv/users/$user/year/$year?native_app_mode=true"

    fun webMonthReviewUrl(
        user: String,
        month: Int,
        year: Int,
    ): String {
        return "https://app.trakt.tv/users/$user/mir/$year/$month?native_app_mode=true"
    }

    fun webImdbPersonUrl(imdbId: String): String {
        return "https://www.imdb.com/name/$imdbId/"
    }

    fun webImdbMediaUrl(imdbId: String): String {
        return "https://www.imdb.com/title/$imdbId/"
    }

    fun webInstagramPersonUrl(id: String): String {
        return "https://www.instagram.com/$id/"
    }

    fun webFacebookPersonUrl(id: String): String {
        return "https://www.facebook.com/$id/"
    }

    fun webTwitterPersonUrl(id: String): String {
        return "https://x.com/$id"
    }

    fun webWikipediaMediaUrl(title: String): String {
        return "https://en.wikipedia.org/wiki/$title"
    }

    fun webDataImportUrl(source: String?): String {
        return "${WEB_V3_BASE_URL}settings/data?native_app_mode=true&source=$source"
    }
}
