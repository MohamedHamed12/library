package io.pillopl.library.lending.patronprofile.web.error;

import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.BOOK_NOT_FOUND;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.EMAIL_ADDRESS_ALREADY_REGISTERED;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.HOLD_NOT_FOUND;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.INTERNAL_ERROR;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.INVALID_EMAIL_ADDRESS;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.INVALID_PATH_PARAMETER;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.MALFORMED_REQUEST;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.PATRON_NOT_FOUND;
import static io.pillopl.library.lending.patronprofile.web.error.ApiErrorCode.VALIDATION_FAILED;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.pillopl.library.lending.patron.application.hold.BookNotFoundException;
import io.pillopl.library.lending.patron.application.hold.HoldNotFoundException;
import io.pillopl.library.lending.patron.application.hold.PatronNotFoundException;
import io.pillopl.library.lending.patron.model.EmailAddressAlreadyRegistered;
import io.pillopl.library.lending.patron.model.InvalidEmailAddress;

@RestControllerAdvice
public class RestExceptionHandler {

  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(RestExceptionHandler.class);

  private final Clock clock;

  public RestExceptionHandler(Clock clock) {
    this.clock = clock;
  }

  @ExceptionHandler(InvalidEmailAddress.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidEmailAddress(
      InvalidEmailAddress exception, HttpServletRequest request) {
    ApiErrorDetail detail = new ApiErrorDetail("email", exception.getMessage());

    return response(
        HttpStatus.BAD_REQUEST,
        INVALID_EMAIL_ADDRESS,
        "The supplied email address is invalid.",
        request,
        List.of(detail));
  }

  @ExceptionHandler(EmailAddressAlreadyRegistered.class)
  public ResponseEntity<ApiErrorResponse> handleEmailAddressAlreadyRegistered(
      EmailAddressAlreadyRegistered exception, HttpServletRequest request) {
    ApiErrorDetail detail = new ApiErrorDetail("email", "Email address is already registered.");

    return response(
        HttpStatus.CONFLICT,
        EMAIL_ADDRESS_ALREADY_REGISTERED,
        "A patron with this email address already exists.",
        request,
        List.of(detail));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiErrorResponse> handleApiException(
      ApiException exception, HttpServletRequest request) {
    return response(
        exception.getStatus(), exception.getCode(), exception.getMessage(), request, List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationFailure(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<ApiErrorDetail> details =
        exception.getBindingResult().getFieldErrors().stream()
            .map(this::toDetail)
            .sorted((first, second) -> first.getField().compareTo(second.getField()))
            .toList();

    return response(
        HttpStatus.BAD_REQUEST,
        VALIDATION_FAILED,
        "The request contains invalid fields.",
        request,
        details);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiErrorResponse> handleMalformedRequest(
      HttpMessageNotReadableException exception, HttpServletRequest request) {
    return response(
        HttpStatus.BAD_REQUEST,
        MALFORMED_REQUEST,
        "The request body is malformed.",
        request,
        List.of());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
    ApiErrorDetail detail =
        new ApiErrorDetail(exception.getName(), "The supplied value has an invalid format.");

    return response(
        HttpStatus.BAD_REQUEST,
        INVALID_PATH_PARAMETER,
        "A path or request parameter has an invalid format.",
        request,
        List.of(detail));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpectedException(
      Exception exception, HttpServletRequest request) {
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
        List.of());
  }

  private ApiErrorDetail toDetail(FieldError error) {
    String message = error.getDefaultMessage();

    if (message == null || message.trim().isEmpty()) {
      message = "Invalid value.";
    }

    return new ApiErrorDetail(error.getField(), message);
  }

  private ResponseEntity<ApiErrorResponse> response(
      HttpStatus status,
      ApiErrorCode code,
      String message,
      HttpServletRequest request,
      List<ApiErrorDetail> details) {
    Instant timestamp = clock.instant();
    ApiErrorResponse body =
        new ApiErrorResponse(code, message, request.getRequestURI(), timestamp, details);

    return ResponseEntity.status(status).body(body);
  }

  @ExceptionHandler(PatronNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handlePatronNotFound(
      PatronNotFoundException exception, HttpServletRequest request) {
    return response(
        HttpStatus.NOT_FOUND,
        PATRON_NOT_FOUND,
        "The requested patron was not found.",
        request,
        List.of());
  }

  @ExceptionHandler(BookNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleBookNotFound(
      BookNotFoundException exception, HttpServletRequest request) {
    return response(
        HttpStatus.NOT_FOUND,
        BOOK_NOT_FOUND,
        "The requested book was not found.",
        request,
        List.of());
  }

  @ExceptionHandler(HoldNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleHoldNotFound(
      HoldNotFoundException exception, HttpServletRequest request) {
    return response(
        HttpStatus.NOT_FOUND,
        HOLD_NOT_FOUND,
        "The requested hold was not found.",
        request,
        List.of());
  }
}
