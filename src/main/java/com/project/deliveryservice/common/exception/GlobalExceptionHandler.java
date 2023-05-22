package com.project.deliveryservice.common.exception;

import com.project.deliveryservice.jwt.JwtInvalidException;
import com.project.deliveryservice.utils.ApiUtils.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedList;
import java.util.List;

import static com.project.deliveryservice.utils.ApiUtils.fail;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtInvalidException.class)
    public ResponseEntity<ApiResponse> handleJwtInvalidException(JwtInvalidException e) {
        return ResponseEntity.
                status(HttpStatus.FORBIDDEN)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(fail(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<String> msg = new LinkedList<>();
        for (FieldError err : e.getBindingResult().getFieldErrors()) {
            msg.add(err.getField() + " " + err.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(fail(msg));
    }
}
