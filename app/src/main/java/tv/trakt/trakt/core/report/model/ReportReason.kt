package tv.trakt.trakt.core.report.model

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

internal enum class ReportReason(
    val apiValue: String,
    @param:StringRes val labelRes: Int,
) {
    Abusive("abusive", R.string.label_report_reason_abusive),
    Spam("spam", R.string.label_report_reason_spam),
    Spoilers("spoilers", R.string.label_report_reason_spoilers),
    Bigotry("bigotry", R.string.label_report_reason_bigotry),
    Political("political", R.string.label_report_reason_political),
    Duplicate("duplicate", R.string.label_report_reason_duplicate),
    OffTopic("offtopic", R.string.label_report_reason_offtopic),
    Remove("remove", R.string.label_report_reason_remove),
    DataRefresh("data_refresh", R.string.label_report_reason_data_refresh),
    Metadata("metadata", R.string.label_report_reason_metadata),
    Adult("adult", R.string.label_report_reason_adult),
    Runtime("runtime", R.string.label_report_reason_runtime),
    Language("language", R.string.label_report_reason_language),
    Tmdb("tmdb", R.string.label_report_reason_tmdb),
    Other("other", R.string.label_report_reason_other),
}
