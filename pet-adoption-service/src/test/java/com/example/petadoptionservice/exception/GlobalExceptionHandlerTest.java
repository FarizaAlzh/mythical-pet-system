package com.example.petadoptionservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnFriendlyMessageForBadCredentials() {
        ResponseEntity<Map<String, String>> response =
                handler.handleBadCredentials(new BadCredentialsException("Bad credentials"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().get("error"));
    }

    @Test
    void shouldReturnFriendlyMessageForInvalidHomeType() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "bad request",
                new RuntimeException("Cannot deserialize value of field homeType from String CASTLE"),
                (HttpInputMessage) null
        );

        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("homeType must be HOUSE, APARTMENT or FARM", response.getBody().get("error"));
    }

    @Test
    void shouldRejectUnknownRoleField() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "bad request",
                new RuntimeException("Unrecognized field role"),
                (HttpInputMessage) null
        );

        ResponseEntity<Map<String, String>> response = handler.handleBadRequest(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unknown field: role", response.getBody().get("error"));
    }
}
