package com.yas.product.viewmodel.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void constructorWithThreeArgs_shouldCreateEmptyFieldErrorsList() {
        ErrorVm vm = new ErrorVm("400", "Bad Request", "Invalid payload");

        assertThat(vm.statusCode()).isEqualTo("400");
        assertThat(vm.title()).isEqualTo("Bad Request");
        assertThat(vm.detail()).isEqualTo("Invalid payload");
        assertThat(vm.fieldErrors()).isNotNull().isEmpty();
    }

    @Test
    void canonicalConstructor_shouldKeepProvidedFieldErrors() {
        ErrorVm vm = new ErrorVm("422", "Validation Error", "Invalid field", List.of("name required"));

        assertThat(vm.statusCode()).isEqualTo("422");
        assertThat(vm.title()).isEqualTo("Validation Error");
        assertThat(vm.detail()).isEqualTo("Invalid field");
        assertThat(vm.fieldErrors()).containsExactly("name required");
    }
}
