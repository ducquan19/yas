package com.yas.product.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link ProductConverter}.
 *
 * <p>Verifies slug generation logic: trimming, lowercasing,
 * replacing special characters with hyphens, collapsing consecutive hyphens,
 * and stripping a leading hyphen.
 */
class ProductConverterTest {

    // ==================== toSlug ====================

    @Test
    void toSlug_whenSimpleInput_thenReturnLowercaseHyphenated() {
        assertThat(ProductConverter.toSlug("Hello World")).isEqualTo("hello-world");
    }

    @Test
    void toSlug_whenAlreadyValidSlug_thenReturnUnchanged() {
        assertThat(ProductConverter.toSlug("already-a-slug")).isEqualTo("already-a-slug");
    }

    @Test
    void toSlug_whenInputHasLeadingAndTrailingSpaces_thenTrimmed() {
        assertThat(ProductConverter.toSlug("  trimmed  ")).isEqualTo("trimmed");
    }

    @Test
    void toSlug_whenInputHasUpperCase_thenConvertedToLowerCase() {
        assertThat(ProductConverter.toSlug("UPPERCASE")).isEqualTo("uppercase");
    }

    @Test
    void toSlug_whenInputHasSpecialCharacters_thenReplacedWithHyphen() {
        assertThat(ProductConverter.toSlug("product@name!")).isEqualTo("product-name-");
    }

    /**
     * Verifies that consecutive spaces are collapsed to a single hyphen.
     *
     * <p>Internally, two spaces become two hyphens ({@code "--"}), which are then
     * collapsed to one by the {@code replaceAll("-{2,}", "-")} step.
     */
    @Test
    void toSlug_whenInputHasConsecutiveSpecialChars_thenCollapsedToSingleHyphen() {
        // "product  name" -> "product--name" -> collapsed -> "product-name"
        assertThat(ProductConverter.toSlug("product  name")).isEqualTo("product-name");
    }

    @Test
    void toSlug_whenInputStartsWithSpecialChar_thenLeadingHyphenStripped() {
        // Leading non-alphanumeric → replaced with '-', then stripped by the method
        assertThat(ProductConverter.toSlug("@product")).isEqualTo("product");
    }

    @Test
    void toSlug_whenInputHasNumbers_thenNumbersPreserved() {
        assertThat(ProductConverter.toSlug("Product 123")).isEqualTo("product-123");
    }

    @ParameterizedTest(name = "toSlug(\"{0}\") == \"{1}\"")
    @CsvSource({
        "Hello World,       hello-world",
        // 'é' is replaced with '-', no consecutive hyphens -> stays as single '-'
        "Caf\u00e9 au lait,      caf-au-lait",
        // existing hyphens 'a---b': 3 consecutive hyphens -> collapsed to 'a-b'
        "a---b,             a-b",
        "abc,               abc"
    })
    void toSlug_parameterized(String input, String expected) {
        assertThat(ProductConverter.toSlug(input.strip())).isEqualTo(expected.strip());
    }
}
