package com.yas.product.model.enumeration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DimensionUnit} enum.
 *
 * <p>Verifies that each enum constant exposes the correct display name
 * and that standard enum operations (values, valueOf) work as expected.
 */
class DimensionUnitTest {

    @Test
    void dimensionUnit_cmHasCorrectName() {
        assertThat(DimensionUnit.CM.getName()).isEqualTo("cm");
    }

    @Test
    void dimensionUnit_inchHasCorrectName() {
        assertThat(DimensionUnit.INCH.getName()).isEqualTo("inch");
    }

    @Test
    void dimensionUnit_valuesContainsBothConstants() {
        DimensionUnit[] values = DimensionUnit.values();
        assertThat(values).containsExactlyInAnyOrder(DimensionUnit.CM, DimensionUnit.INCH);
    }

    @Test
    void dimensionUnit_valueOfCm_returnsCmConstant() {
        assertThat(DimensionUnit.valueOf("CM")).isEqualTo(DimensionUnit.CM);
    }

    @Test
    void dimensionUnit_valueOfInch_returnsInchConstant() {
        assertThat(DimensionUnit.valueOf("INCH")).isEqualTo(DimensionUnit.INCH);
    }
}
