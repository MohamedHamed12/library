package io.pillopl.library.lending.patronprofile.web.error;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.INTERNAL_ERROR;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.INVALID_PATH_PARAMETER;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.MALFORMED_REQUEST;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.VALIDATION_FAILED;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception, HttpServletRequest request) {
        return response(
                exception.getStatus(),
                exception.getCode(),
                exception.getMessage(),
                request,
                Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationFailure(MethodArgumentNotValidException exception,HttpServletRequest request) {
        List<ApiErrorDetail> details = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toDetail)
                .sorted((first, second) -> first.getField().compareTo(second.getField()))
                .collect(Collectors.toList());

        return response(
                HttpStatus.BAD_REQUEST,
                VALIDATION_FAILED,
                "The request contains invalid fields.",
                request,
                details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMalformedRequest(HttpMessageNotReadableException exception,HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                MALFORMED_REQUEST,
                "The request body is malformed.",
                request,
                Collections.emptyList());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,HttpServletRequest request) {
        ApiErrorDetail detail = new ApiErrorDetail(
                exception.getName(),
                "The supplied value has an invalid format.");

        return response(
                HttpStatus.BAD_REQUEST,
                INVALID_PATH_PARAMETER,
                "A path or request parameter has an invalid format.",
                request,
                Collections.singletonList(detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception,HttpServletRequest request) {
        log.error(
                "Unexpected exception while processing {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception);

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_ERROR,
                "An unexpected error occurred.",
                request,
                Collections.emptyList());
    }

    private ApiErrorDetail toDetail(FieldError error) {
        String message = error.getDefaultMessage();

        if (message == null || message.trim().isEmpty()) {
            message = "Invalid value.";
        }

        return new ApiErrorDetail(
                error.getField(),
                message);
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status,ApiErrorCode code,String message,HttpServletRequest request,List<ApiErrorDetail> details) {
        ApiErrorResponse body = new ApiErrorResponse(
                code,
                message,
                request.getRequestURI(),
                Instant.now(),
                details);

        return ResponseEntity
                .status(status)
                .body(body);
    }
}