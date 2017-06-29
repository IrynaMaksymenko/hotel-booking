package com.exercises.hotelbooking.services;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static java.time.ZoneOffset.UTC;

@UtilityClass
public class Dates {

    public static final LocalTime CHECK_IN_TIME = LocalTime.NOON.plusHours(2);
    public static final LocalTime CHECK_OUT_TIME = LocalTime.NOON;

    public static long toMillis(LocalDate date, LocalTime time) {
        return LocalDateTime.of(date, time).toInstant(UTC).toEpochMilli();
    }

    public static LocalDateTime toDateTime(long millis) {
        return Instant.ofEpochMilli(millis).atZone(UTC).toLocalDateTime();
    }

}
