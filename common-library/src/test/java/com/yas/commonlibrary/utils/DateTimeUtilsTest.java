package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DateTimeUtilsTest {

    @Test
    void format_withDefaultPattern_formatsDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);

        String formatted = DateTimeUtils.format(dateTime);

        assertEquals("03-02-2024_04-05-06", formatted);
    }

    @Test
    void format_withCustomPattern_formatsDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2024, 2, 3, 4, 5, 6);

        String formatted = DateTimeUtils.format(dateTime, "yyyy/MM/dd HH:mm");

        assertEquals("2024/02/03 04:05", formatted);
    }
}
