package tv.trakt.trakt.common

object Config {
    const val DEFAULT_COUNTRY_CODE: String = "us"

    const val API_BASE_URL: String = "https://apiz.trakt.tv/"

    const val WEB_BASE_URL: String = "https://trakt.tv/"
    const val WEB_V3_BASE_URL: String = "https://app.trakt.tv/"
    const val WEB_VIP_URL: String = "https://trakt.tv/vip?native_app_mode=true"
    const val WEB_ABOUT_US_URL: String = "https://trakt.tv/about?native_app_mode=true"

    fun webUserUrl(userId: String): String = "https://trakt.tv/users/$userId?native_app_mode=true"

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

    const val PLEX_BASE_URL: String = "https://watch.plex.tv/"
}
