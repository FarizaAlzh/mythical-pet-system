package com.example.matchingservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalTokenInterceptorTest {

    @Test
    void shouldRejectRequestWithoutExpectedToken() throws Exception {
        InternalTokenInterceptor interceptor = new InternalTokenInterceptor("dev-internal-token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter writer = new StringWriter();

        when(request.getHeader("X-Internal-Token")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
    }

    @Test
    void shouldAllowRequestWithExpectedToken() throws Exception {
        InternalTokenInterceptor interceptor = new InternalTokenInterceptor("dev-internal-token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getHeader("X-Internal-Token")).thenReturn("dev-internal-token");

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }
}
