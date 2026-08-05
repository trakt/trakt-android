package tv.trakt.trakt.common.core.streamings.helpers

import android.icu.util.Currency
import tv.trakt.trakt.common.model.streamings.StreamingService
import tv.trakt.trakt.common.model.streamings.StreamingSource
import tv.trakt.trakt.common.networking.StreamingServiceDto

/**
 * Countries surfaced first within a source row, after the user's own streaming country.
 */
internal val PopularCountries = listOf(
    "us",
    "gb",
    "ca",
    "de",
    "fr",
    "jp",
    "au",
    "nl",
    "mx",
    "sg",
)

/**
 * Maps a watchnow offer onto a domain [StreamingService], taking branding from the
 * matching [StreamingSource].
 */
internal fun StreamingServiceDto.toStreamingService(
    country: String,
    source: StreamingSource?,
): StreamingService {
    return StreamingService(
        name = source?.name.orEmpty(),
        linkDirect = linkDirect,
        source = this.source,
        color = source?.color,
        logo = source?.images?.logo,
        channel = source?.images?.channel,
        uhd = uhd,
        country = country,
        purchasePrice = prices.purchase,
        rentPrice = prices.rent,
        currency = currency?.let { Currency.getInstance(it) },
    )
}

/**
 * Orders services so the widely used providers come first, then alphabetically.
 */
internal fun popularServicesComparator(): Comparator<StreamingService> {
    val priorityServices = PopularStreamingServices.reversed()

    return compareByDescending<StreamingService> {
        priorityServices.indexOf(it.source)
    }.thenBy {
        it.source
    }
}

/**
 * Orders countries so the user's own country comes first, then the popular ones, then
 * alphabetically.
 */
internal fun popularCountriesComparator(userCountry: String): Comparator<StreamingService> {
    val priorityCountries = (listOf(userCountry) + PopularCountries).reversed()

    return compareByDescending<StreamingService> {
        priorityCountries.indexOf(it.country.lowercase())
    }.thenBy {
        it.country
    }
}
