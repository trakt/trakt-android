package tv.trakt.trakt.common.helpers

import android.content.Context
import androidx.annotation.StringRes

sealed interface StringResource {
    fun get(context: Context): String
}

data class DynamicStringResource(
    @param:StringRes val resId: Int,
    val args: List<Any> = emptyList(),
) : StringResource {
    override fun get(context: Context): String {
        return if (args.isEmpty()) {
            context.getString(resId)
        } else {
            context.getString(resId, *args.toTypedArray())
        }
    }
}

data class StaticStringResource(
    val value: String,
) : StringResource {
    override fun get(context: Context): String {
        return value
    }
}
