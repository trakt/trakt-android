package tv.trakt.trakt.tv.helpers

import android.content.Context
import androidx.annotation.StringRes

sealed interface StringResource {
    fun get(context: Context): String
}

internal data class DynamicStringResource(
    @param:StringRes val resId: Int,
) : StringResource {
    override fun get(context: Context): String {
        return context.getString(resId)
    }
}

internal data class StaticStringResource(
    val value: String,
) : StringResource {
    override fun get(context: Context): String {
        return value
    }
}
