package com.yas.product.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private static class TestFallbackHandler extends AbstractCircuitBreakFallbackHandler {
        void callBodiless(Throwable t) throws Throwable {
            handleBodilessFallback(t);
        }

        Object callTyped(Throwable t) throws Throwable {
            return handleTypedFallback(t);
        }

        void callError(Throwable t) throws Throwable {
            handleError(t);
        }
    }

    private final TestFallbackHandler handler = new TestFallbackHandler();

    @Test
    void handleBodilessFallback_whenErrorPassed_thenRethrowSameThrowable() {
        RuntimeException ex = new RuntimeException("boom");

        assertThatThrownBy(() -> handler.callBodiless(ex))
            .isSameAs(ex);
    }

    @Test
    void handleTypedFallback_whenErrorPassed_thenRethrowSameThrowable() {
        IllegalStateException ex = new IllegalStateException("typed-fallback");

        assertThatThrownBy(() -> handler.callTyped(ex))
            .isSameAs(ex);
    }

    @Test
    void handleError_whenCalled_thenRethrowSameThrowable() {
        Throwable ex = new Exception("direct-error");

        assertThatThrownBy(() -> handler.callError(ex))
            .isSameAs(ex);
    }
}
