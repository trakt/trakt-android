package tv.trakt.trakt.common.model.globalfilter

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class GlobalFilterDecade(
    val years: Pair<Int, Int>,
) {
    CurrentYear(0 to 0),
    Years2020(2020 to 2029),
    Years2010(2010 to 2019),
    Years2000(2000 to 2009),
    Years1990(1990 to 1999),
    Years1980(1980 to 1989),
    Years1970(1970 to 1979),
    Years1960(1960 to 1969),
    Years1950(1950 to 1959),
    Years1940(1940 to 1949),
    Years1930(1930 to 1939),
}
