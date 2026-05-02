package com.yas.commonlibrary.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_whenKeyExists_formatsMessage() {
        String message = MessagesUtils.getMessage("PRODUCT_NOT_FOUND", "p1");

        assertEquals("The product p1 is not found", message);
    }

    @Test
    void getMessage_whenKeyMissing_returnsKey() {
        String message = MessagesUtils.getMessage("UNKNOWN_KEY");

        assertEquals("UNKNOWN_KEY", message);
    }
}
