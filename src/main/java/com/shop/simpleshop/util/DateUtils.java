package com.shop.simpleshop.util;

import com.shop.simpleshop.exceptions.InvalidQueryParameterException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tolerant date-time parsing helpers.
 *
 * The frontend sends ISO-8601 strings with a trailing {@code Z} and millisecond
 * precision (e.g. {@code 2026-07-05T00:00:00.000Z}). {@link LocalDateTime} cannot
 * parse an offset directly, so we first attempt {@link OffsetDateTime} (which
 * accepts {@code Z}) and fall back to plain local parsing.
 */
public final class DateUtils {

    private static final DateTimeFormatter OFFSET_DATE_TIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private DateUtils() {
    }

    /**
     * Parses a date-time query parameter, or returns {@code null} when blank.
     *
     * @throws InvalidQueryParameterException if the value is present but unparseable
     */
    public static LocalDateTime parseDateTime(String value, String paramName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value, OFFSET_DATE_TIME).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException e2) {
                throw new InvalidQueryParameterException(paramName, value,
                        "yyyy-MM-dd'T'HH:mm:ss[.SSS][Z]");
            }
        }
    }

    /**
     * Parses a date query parameter (e.g. {@code asOfDate}), or returns {@code null} when blank.
     *
     * @throws InvalidQueryParameterException if the value is present but unparseable
     */
    public static LocalDate parseDate(String value, String paramName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidQueryParameterException(paramName, value, "yyyy-MM-dd");
        }
    }
}
