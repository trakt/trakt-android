package tv.trakt.trakt.core.home.sections.monthreview.usecases

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth

private val KEY_MONTH_REVIEW_BANNER_DISMISSED = stringPreferencesKey("key_month_review_banner_dismissed")

private const val BANNER_DAYS = 7

internal class MonthReviewBannerUseCase(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun getBannerMonth(today: LocalDate): YearMonth? {
        if (today.dayOfMonth > BANNER_DAYS) return null

        val dismissed = dataStore.data.first()[KEY_MONTH_REVIEW_BANNER_DISMISSED]
        if (dismissed == monthKey(today)) return null

        return YearMonth.from(today).minusMonths(1)
    }

    suspend fun dismissBanner(today: LocalDate): String {
        val key = monthKey(today)
        dataStore.edit { prefs ->
            prefs[KEY_MONTH_REVIEW_BANNER_DISMISSED] = key
        }
        return key
    }

    private fun monthKey(date: LocalDate): String {
        return "${date.year}-${date.monthValue}"
    }
}
