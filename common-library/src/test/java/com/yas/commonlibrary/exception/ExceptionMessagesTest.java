package com.yas.commonlibrary.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ExceptionMessagesTest {

    @Test
    void notFoundException_usesMessageBundle() {
        NotFoundException ex = new NotFoundException("PRODUCT_NOT_FOUND", "p1");

        assertEquals("The product p1 is not found", ex.getMessage());
    }

    @Test
    void badRequestException_formatsMessage() {
        BadRequestException ex = new BadRequestException("WRONG_EMAIL_FORMAT", "user@example.com");

        assertEquals("Wrong email format for user@example.com", ex.getMessage());
    }

    @Test
    void duplicatedException_formatsMessage() {
        DuplicatedException ex = new DuplicatedException("SLUG_IS_DUPLICATED", "slug-1");

        assertEquals("Slug slug-1 is duplicated", ex.getMessage());
    }

    @Test
    void resourceExistedException_allowsMessageOverride() {
        ResourceExistedException ex = new ResourceExistedException("RESOURCE_ALREADY_EXISTED");
        ex.setMessage("Override");

        assertEquals("Override", ex.getMessage());
    }
}
