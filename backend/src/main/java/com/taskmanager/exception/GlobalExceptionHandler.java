package com.taskmanager.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        log.error("Global exception handler caught: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, WebRequest request) {
        log.error("Runtime exception: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        log.warn("Bad credentials: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Invalid email or password",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "Access denied",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        log.warn("Authentication exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Unauthorized",
            "Authentication failed",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // JWT-specific exception handlers
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwtException(ExpiredJwtException ex, WebRequest request) {
        log.warn("JWT token expired: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Token Expired",
            "JWT token has expired",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJwtException(MalformedJwtException ex, WebRequest request) {
        log.warn("Malformed JWT token: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid Token",
            "JWT token is malformed",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedJwtException(UnsupportedJwtException ex, WebRequest request) {
        log.warn("Unsupported JWT token: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.UNAUTHORIZED.value(),
            "Invalid Token",
            "JWT token is not supported",
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
            new Date(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    public static class ErrorResponse {
        private Date timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ErrorResponse(Date timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        // Getters and setters
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        
        public int getStatus() { return status; }
        public void setStatus(int status) { this.status = status; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}
