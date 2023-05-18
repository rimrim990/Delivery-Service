package com.project.deliveryservice.common.exception;

import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.utils.ApiUtils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.project.deliveryservice.utils.ApiUtils.fail;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtInvalidException.class)
    public ResponseEntity<ApiResponse> handleJwtInvalidException(JwtInvalidException e) {
        return ResponseEntity.
                status(HttpStatus.FORBIDDEN)
                .body(fail(e.getMessage()));
    }
}
