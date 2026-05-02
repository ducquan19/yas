package com.yas.commonlibrary.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.viewmodel.error.ErrorVm;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    void handleNotFoundException_buildsErrorResponseWithPath() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/products/1");
        request.setServletPath("/api/products/1");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorVm> response = handler.handleNotFoundException(
            new NotFoundException("PRODUCT_NOT_FOUND", "p1"),
            webRequest
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("404 NOT_FOUND", response.getBody().statusCode());
        assertEquals("The product p1 is not found", response.getBody().detail());
        assertEquals(null, response.getBody().fieldErrors());
    }

    @Test
    void handleMethodArgumentNotValid_buildsFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new TestRequest(), "testRequest");
        bindingResult.addError(new FieldError("testRequest", "name", "must not be blank"));

        Method method = TestController.class.getDeclaredMethod("create", TestRequest.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorVm> response = handler.handleMethodArgumentNotValid(
            ex,
            new ServletWebRequest(new MockHttpServletRequest())
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request information is not valid", response.getBody().detail());
        assertEquals(List.of("name must not be blank"), response.getBody().fieldErrors());
    }

    @Test
    void handleConstraintViolation_buildsFieldErrors() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<TestRequest> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");
        when(violation.getRootBeanClass()).thenReturn(TestRequest.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("invalid");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorVm> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().fieldErrors().isEmpty());
        assertTrue(response.getBody().fieldErrors().get(0).contains("email: invalid"));
    }

    @Test
    void handleAccessDeniedException_buildsForbiddenResponse() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        request.setServletPath("/admin");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        ResponseEntity<ErrorVm> response = handler.handleAccessDeniedException(
            new AccessDeniedException("no access"),
            webRequest
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("no access", response.getBody().detail());
    }

    @Test
    void handleInternalServerErrorException_buildsErrorVm() {
        ResponseEntity<ErrorVm> response = handler.handleInternalServerErrorException(
            new InternalServerErrorException("PAYMENT_FAIL_MESSAGE")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Payment failed", response.getBody().detail());
    }

    static class TestRequest {
    }

    static class TestController {
        void create(TestRequest request) {
        }
    }
}
