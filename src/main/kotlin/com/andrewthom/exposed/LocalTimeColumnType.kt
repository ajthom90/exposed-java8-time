package com.andrewthom.exposed

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

private val DEFAULT_TIME_STRING_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.ROOT).withZone(ZoneId.systemDefault())

class LocalTimeColumnType: ColumnType() {
	override fun sqlType() = "TIME"

	override fun nonNullValueToString(value: Any): String {
		if (value is String) return value

		val instant = when (value) {
			is java.time.LocalTime -> value
			is org.joda.time.LocalTime -> LocalTime.of(value.hourOfDay, value.minuteOfHour, value.secondOfMinute)
			is java.sql.Time -> value.toLocalTime()
			is java.sql.Timestamp -> value.toLocalDateTime().toLocalTime()
			else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
		}

		return DEFAULT_TIME_STRING_FORMATTER.format(instant)
	}

	override fun valueFromDB(value: Any): Any = when(value) {
		is LocalTime -> value
		is java.sql.Time -> value.toLocalTime()
		is java.sql.Timestamp -> value.toLocalDateTime().toLocalTime()
		is Int -> Instant.ofEpochMilli(value.toLong()).atOffset(ZoneOffset.UTC).toLocalTime()
		is Long -> Instant.ofEpochMilli(value).atOffset(ZoneOffset.UTC).toLocalTime()
		is String -> DEFAULT_TIME_STRING_FORMATTER.parse(value, LocalTime::from)
		else -> DEFAULT_TIME_STRING_FORMATTER.parse(value.toString(), LocalTime::from)
	}

	override fun notNullValueToDB(value: Any): Any {
		if (value is LocalTime) {
			return java.sql.Time(value.toSecondOfDay() * 1000L)
		}
		return value
	}
}

fun Table.javaTime(name: String) = registerColumn<LocalTime>(name, LocalTimeColumnType())