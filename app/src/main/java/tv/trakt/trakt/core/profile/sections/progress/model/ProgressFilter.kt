package tv.trakt.trakt.core.profile.sections.progress.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Serializable
internal enum class ProgressFilter(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val iconRes: Int,
) {
    UpToDate(R.string.button_text_progress_completed, R.drawable.ic_check_double),
    Ended(R.string.translated_value_status_ended, R.drawable.ic_flag_checker),
    InProgress(R.string.button_text_progress_in_progress, R.drawable.ic_hourglass),
    Dropped(R.string.button_text_progress_dropped, R.drawable.ic_drop),
}
