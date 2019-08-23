package com.andrewthom.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

private val DEFAULT_DATE_TIME_STRING_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.SSSSSS").withLocale(Locale.ROOT).withZone(ZoneId.systemDefault())

class InstantColumnType: ColumnType() {
	override fun sqlType() = "DATETIME"

	override fun nonNullValueToString(value: Any): String {
		if (value is String) return value

		val instant = when (value) {
			is Instant -> value
			is java.sql.Date -> value.toInstant()
			is java.sql.Timestamp -> value.toInstant()
			else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
		}

		return "'${DEFAULT_DATE_TIME_STRING_FORMATTER.format(instant)}'"
	}

	override fun valueFromDB(value: Any): Any = when(value) {
		is Instant -> value
		is java.sql.Date -> value.toInstant()
		is java.sql.Timestamp -> value.toInstant()
		is Int -> Instant.ofEpochMilli(value.toLong())
		is Long -> Instant.ofEpochMilli(value)
		is String -> DEFAULT_DATE_TIME_STRING_FORMATTER.parse(value, Instant::from)
		else -> DEFAULT_DATE_TIME_STRING_FORMATTER.parse(value.toString(), Instant::from)
	}

	override fun notNullValueToDB(value: Any): Any {
		if (value is Instant) {
			val millis = value.toEpochMilli()
			return java.sql.Timestamp(millis)
		}
		return value
	}
}

fun Table.instant(name: String): Column<Instant> = registerColumn(name, InstantColumnType())
