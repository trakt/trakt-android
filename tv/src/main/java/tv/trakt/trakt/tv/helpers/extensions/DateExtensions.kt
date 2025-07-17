package tv.trakt.trakt.tv.helpers.extensions

import android.icu.text.RelativeDateTimeFormatter
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit.DAY
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit.MONTH
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit.NOW
import android.icu.text.RelativeDateTimeFormatter.AbsoluteUnit.WEEK
import android.icu.text.RelativeDateTimeFormatter.Direction.LAST
import android.icu.text.RelativeDateTimeFormatter.Direction.NEXT
import android.icu.text.RelativeDateTimeFormatter.Direction.PLAIN
import android.icu.text.RelativeDateTimeFormatter.Direction.THIS
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.DAYS
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.HOURS
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.MINUTES
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.MONTHS
import android.icu.text.RelativeDateTimeFormatter.RelativeUnit.WEEKS
import tv.trakt.trakt.tv.helpers.longDateTimeFormat
import java.time.DayOfWeek.MONDAY
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ISO_INSTANT
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// UTC time functions

fun nowUtc(): ZonedDateTime = ZonedDateTime.now(UTC)

fun nowUtcMillis(): Long = nowUtc().toInstant().toEpochMilli()

fun nowUtcString(): String = nowUtc().format(ISO_INSTANT)

// Local time functions

fun nowLocal(): ZonedDateTime = ZonedDateTime.now()

fun nowLocalMillis(): Long = nowLocal().toInstant().toEpochMilli()

fun nowLocalString(): String = nowLocal().format(ISO_INSTANT)

fun ZonedDateTime.toLocal(): ZonedDateTime = this.withZoneSameInstant(ZoneId.systemDefault())

// Misc

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
        today.with(TemporalAdjusters.previousOrSame(MONDAY)),
        this.with(TemporalAdjusters.previousOrSame(MONDAY)),
    )

    val daysBetween = ChronoUnit.DAYS.between(today, this)

    return when {
        daysBetween == 0L -> formatter.format(THIS, DAY)
        daysBetween == 1L -> formatter.format(NEXT, DAY)
        daysBetween <= 6L -> formatter.format(daysBetween.toDouble(), NEXT, DAYS)
        weeksBetween == 1L -> formatter.format(NEXT, WEEK)
        weeksBetween <= 4L -> formatter.format(weeksBetween.toDouble(), NEXT, WEEKS)
        monthsBetween == 1L -> formatter.format(NEXT, MONTH)
        monthsBetween <= 6L -> formatter.format(monthsBetween.toDouble(), NEXT, MONTHS)
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
        now.toLocalDate().with(TemporalAdjusters.previousOrSame(MONDAY)),
        this.toLocalDate().with(TemporalAdjusters.previousOrSame(MONDAY)),
    )

    val monthsBetween = ChronoUnit.MONTHS.between(
        now.toLocalDate().withDayOfMonth(1),
        this.toLocalDate().withDayOfMonth(1),
    )

    return when {
        minutesBetween <= 1 -> formatter.format(PLAIN, NOW)
        minutesBetween <= 59 -> formatter.format(minutesBetween.toDouble(), NEXT, MINUTES)
        hoursBetween <= 11 -> formatter.format(hoursBetween.toDouble(), NEXT, HOURS)
        daysBetween == 0L -> formatter.format(THIS, DAY)
        daysBetween == 1L -> formatter.format(NEXT, DAY)
        daysBetween <= 6L -> formatter.format(daysBetween.toDouble(), NEXT, DAYS)
        weeksBetween == 1L -> formatter.format(NEXT, WEEK)
        weeksBetween <= 4L -> formatter.format(weeksBetween.toDouble(), NEXT, WEEKS)
        monthsBetween == 1L -> formatter.format(NEXT, MONTH)
        monthsBetween <= 6 -> formatter.format(monthsBetween.toDouble(), NEXT, MONTHS)
        else -> this.year.toString()
    }.replaceFirstChar {
        it.titlecase(locale)
    }
}

fun ZonedDateTime.relativePastDateTimeString(locale: Locale = Locale.US): String {
    val formatter = RelativeDateTimeFormatter.getInstance(locale)
    val now = ZonedDateTime.now()

    val daysBetween = ChronoUnit.DAYS.between(
        this.toLocalDate(),
        now.toLocalDate(),
    )

    return when {
        daysBetween == 0L -> formatter.format(THIS, DAY)
        daysBetween == 1L -> formatter.format(LAST, DAY)
        daysBetween <= 3L -> formatter.format(daysBetween.toDouble(), LAST, DAYS)
        else -> this.format(longDateTimeFormat)
    }.replaceFirstChar {
        it.titlecase(locale)
    }
}
