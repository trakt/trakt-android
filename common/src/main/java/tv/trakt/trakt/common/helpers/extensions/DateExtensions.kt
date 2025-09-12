package tv.trakt.trakt.common.helpers.extensions

import android.icu.text.RelativeDateTimeFormatter
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

val mediumDateFormat: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.MEDIUM)
    .withLocale(Locale.US)

val longDateFormat: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDate(FormatStyle.LONG)
    .withLocale(Locale.US)

val longDateTimeFormat: DateTimeFormatter = DateTimeFormatter
    .ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT)
    .withLocale(Locale.US)

// UTC time functions

fun nowUtc(): ZonedDateTime = ZonedDateTime.now(UTC)

fun nowUtcInstant(): Instant = Instant.now()

// Local time functions

fun nowLocal(): ZonedDateTime = ZonedDateTime.now()

fun nowLocalDay(): LocalDate = LocalDate.now()

fun ZonedDateTime.toLocal(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())

fun Instant.toLocal(): ZonedDateTime = this.atZone(ZoneId.systemDefault())

fun LocalDate.isTodayOrBefore(): Boolean {
    val today = LocalDate.now()
    return this.isEqual(today) || this.isBefore(today)
}

// Strings functions

fun String.toInstant(): Instant {
    return Instant.parse(this)
}

fun String.toZonedDateTime(): ZonedDateTime {
    // Current Nitro endpoints pattern is "2025-06-23 03:24:23.000000"
    val nitroFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(UTC)
    return when {
        else -> runCatching {
            ZonedDateTime.parse(this)
        }.getOrElse {
            ZonedDateTime.parse(this, nitroFormatter).withFixedOffsetZone()
        }
    }
}

fun LocalDate.relativeDateString(locale: Locale = Locale.US): String {
    val today = nowLocal().toLocalDate()
    val formatter = RelativeDateTimeFormatter.getInstance(locale)

    val monthsBetween = ChronoUnit.MONTHS.between(
        today.withDayOfMonth(1),
        this.withDayOfMonth(1),
    )

    val weeksBetween = ChronoUnit.WEEKS.between(
        today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    )

    val daysBetween = ChronoUnit.DAYS.between(today, this)

    return when {
        daysBetween == 0L -> formatter.format(
            RelativeDateTimeFormatter.Direction.THIS,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY,
        )
        daysBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY,
        )
        daysBetween <= 6L -> formatter.format(
            daysBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.DAYS,
        )
        weeksBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.WEEK,
        )
        weeksBetween <= 4L -> formatter.format(
            weeksBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.WEEKS,
        )
        monthsBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.MONTH,
        )
        monthsBetween <= 6L -> formatter.format(
            monthsBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.MONTHS,
        )
        else -> this.year.toString()
    }.replaceFirstChar {
        it.titlecase(locale)
    }
}

fun ZonedDateTime.relativeDateTimeString(locale: Locale = Locale.US): String {
    val formatter = RelativeDateTimeFormatter.getInstance(locale)
    val now = ZonedDateTime.now()

    val minutesBetween = ChronoUnit.MINUTES.between(now, this)
    val hoursBetween = ChronoUnit.HOURS.between(now, this)
    val daysBetween = ChronoUnit.DAYS.between(now.toLocalDate(), this.toLocalDate())

    val weeksBetween = ChronoUnit.WEEKS.between(
        now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
        this.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    )

    val monthsBetween = ChronoUnit.MONTHS.between(
        now.toLocalDate().withDayOfMonth(1),
        this.toLocalDate().withDayOfMonth(1),
    )

    return when {
        minutesBetween <= 1 -> formatter.format(
            RelativeDateTimeFormatter.Direction.PLAIN,
            RelativeDateTimeFormatter.AbsoluteUnit.NOW,
        )
        minutesBetween <= 59 -> formatter.format(
            minutesBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.MINUTES,
        )
        hoursBetween <= 11 -> formatter.format(
            hoursBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.HOURS,
        )
        daysBetween == 0L -> formatter.format(
            RelativeDateTimeFormatter.Direction.THIS,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY,
        )
        daysBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY,
        )
        daysBetween <= 6L -> formatter.format(
            daysBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.DAYS,
        )
        weeksBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.WEEK,
        )
        weeksBetween <= 4L -> formatter.format(
            weeksBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.WEEKS,
        )
        monthsBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.AbsoluteUnit.MONTH,
        )
        monthsBetween <= 6 -> formatter.format(
            monthsBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.NEXT,
            RelativeDateTimeFormatter.RelativeUnit.MONTHS,
        )
        else -> this.year.toString()
    }.replaceFirstChar {
        it.titlecase(locale)
    }
}

fun ZonedDateTime.relativePastDateString(locale: Locale = Locale.US): String {
    val formatter = RelativeDateTimeFormatter.getInstance(locale)
    val now = ZonedDateTime.now()

    val daysBetween = ChronoUnit.DAYS.between(
        this.toLocalDate(),
        now.toLocalDate(),
    )

    return when {
        daysBetween == 0L -> formatter.format(
            RelativeDateTimeFormatter.Direction.THIS,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY,
        )
        daysBetween == 1L -> formatter.format(
            RelativeDateTimeFormatter.Direction.LAST,
            RelativeDateTimeFormatter.AbsoluteUnit.DAY,
        )
        daysBetween <= 3L -> formatter.format(
            daysBetween.toDouble(),
            RelativeDateTimeFormatter.Direction.LAST,
            RelativeDateTimeFormatter.RelativeUnit.DAYS,
        )
        else -> this.format(longDateFormat)
    }.replaceFirstChar {
        it.titlecase(locale)
    }
}
