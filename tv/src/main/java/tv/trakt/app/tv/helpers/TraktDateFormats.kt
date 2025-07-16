package tv.trakt.app.tv.helpers

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal val longDateTimeFormat = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
    .withLocale(Locale.US)

internal val longDateFormat = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.LONG)
    .withLocale(Locale.US)
