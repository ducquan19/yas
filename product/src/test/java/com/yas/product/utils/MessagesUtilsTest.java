package com.yas.product.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MessagesUtils}.
 *
 * <p>Verifies message resolution from the resource bundle and graceful
 * fallback when a message key is not defined.
 */
class MessagesUtilsTest {

    @Test
    void getMessage_whenKeyExists_thenReturnFormattedMessage() {
        // "BRAND_NOT_FOUND" is defined in messages/messages.properties as:
        // "Brand {} is not found"
        String result = MessagesUtils.getMessage("BRAND_NOT_FOUND", 1L);
        assertThat(result).isNotNull().isNotEmpty();
        // Confirms the key resolved to an actual message (not returned as-is)
        assertThat(result).doesNotStartWith("BRAND_NOT_FOUND");
        assertThat(result).contains("Brand");
    }

    @Test
    void getMessage_whenKeyDoesNotExist_thenReturnKeyAsMessage() {
        // When a key is missing, MessagesUtils falls back to returning the key itself
        String unknownKey = "UNKNOWN_KEY_THAT_DOES_NOT_EXIST";
        String result = MessagesUtils.getMessage(unknownKey);
        assertThat(result).isEqualTo(unknownKey);
    }

    @Test
    void getMessage_whenKeyExistsWithNoArgs_thenReturnMessage() {
        // "MAKE_SURE_CATEGORY_DO_NOT_CONTAIN_CHILDREN" has no placeholders
        String result = MessagesUtils.getMessage("MAKE_SURE_CATEGORY_DO_NOT_CONTAIN_CHILDREN");
        assertThat(result).isNotNull().isNotEmpty();
        assertThat(result).doesNotStartWith("MAKE_SURE");
    }
}
