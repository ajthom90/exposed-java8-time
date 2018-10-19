package com.andrewthom.exposed

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

private val DEFAULT_DATE_STRING_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd").withLocale(Locale.ROOT).withZone(ZoneId.systemDefault())

class LocalDateColumnType: ColumnType() {
	override fun sqlType() = "DATE"

	override fun nonNullValueToString(value: Any): String {
		if (value is String) return value

		val instant = when (value) {
			is org.joda.time.LocalDate -> value.toDate().toInstant().atOffset(ZoneOffset.UTC).toLocalDate()
			is java.sql.Date -> value.toLocalDate()
			is java.sql.Timestamp -> value.toLocalDateTime().toLocalDate()
			else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
		}

		return DEFAULT_DATE_STRING_FORMATTER.format(instant)
	}

	override fun valueFromDB(value: Any): Any = when(value) {
		is LocalDate -> value
		is java.sql.Date -> value.toLocalDate()
		is java.sql.Timestamp -> value.toLocalDateTime().toLocalDate()
		is Int -> Instant.ofEpochMilli(value.toLong()).atOffset(ZoneOffset.UTC).toLocalDate()
		is Long -> Instant.ofEpochMilli(value).atOffset(ZoneOffset.UTC).toLocalDate()
		is String -> DEFAULT_DATE_STRING_FORMATTER.parse(value, Instant::from)
		else -> DEFAULT_DATE_STRING_FORMATTER.parse(value.toString(), LocalDate::from)
	}

	override fun notNullValueToDB(value: Any): Any {
		if (value is LocalDate) {
			return java.sql.Date(value.toEpochDay())
		}
		return value
	}
}

fun Table.javaDate(name: String) = registerColumn<LocalDate>(name, LocalDateColumnType())