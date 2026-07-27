package tv.trakt.trakt.core.comments.features.report.model

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
    Other("other", R.string.label_report_reason_other),
}
