package tv.trakt.trakt.common.helpers

import android.content.Context
import androidx.annotation.StringRes

sealed interface StringResource {
    fun get(context: Context): String
}

data class DynamicStringResource(
    @param:StringRes val resId: Int,
) : StringResource {
    override fun get(context: Context): String {
        return context.getString(resId)
    }
}

data class StaticStringResource(
    val value: String,
) : StringResource {
    override fun get(context: Context): String {
        return value
    }
}
