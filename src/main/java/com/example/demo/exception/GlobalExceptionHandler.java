package com.example.demo.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(HotelNotFoundException.class)
	public ResponseEntity<?> handleHotelNotFound(HotelNotFoundException ex) {
		return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Not Found");
	}

	@ExceptionHandler(RoomTypeNotFoundException.class)
	public ResponseEntity<?> handleRoomTypeNotFound(RoomTypeNotFoundException ex) {
		return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Not Found");
	}
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex) {
		return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Not Found");
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<?> handleInvalidCredentialsException(InvalidCredentialsException ex) {
		return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Unauthorized");
	}

	@ExceptionHandler(EmailNotVerifiedException.class)
	public ResponseEntity<?> handleEmailNotVerifiedException(EmailNotVerifiedException ex) {
		return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "Forbidden");
	}
	@ExceptionHandler(DuplicateEmailException.class)
	public ResponseEntity<?> handleDuplicateEmailException(DuplicateEmailException ex) {
		return buildErrorResponse(ex, HttpStatus.CONFLICT, "Conflict");
	}
	@ExceptionHandler(PasswordConfirmationMismatchException.class)
	public ResponseEntity<?> handlePasswordConfirmationMismatchException(PasswordConfirmationMismatchException ex) {
		return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Bad Request");
	}
	@ExceptionHandler(InvalidTokenException.class)
	public ResponseEntity<?> handleInvalidTokenException(InvalidTokenException ex) {
		return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Unauthorized");

	}
	@ExceptionHandler(InvalidGoogleTokenException.class)
	public ResponseEntity<?> handleInvalidGoogleTokenException(InvalidGoogleTokenException ex) {
		return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Unauthorized");

	}
	private ResponseEntity<?> buildErrorResponse(Exception ex, HttpStatus status, String errorType) {
		Map<String, Object> error = new HashMap<>();
		error.put("timestamp", LocalDateTime.now());
		error.put("status", status.value());
		error.put("error", errorType);
		error.put("message", ex.getMessage());
		return new ResponseEntity<>(error, status);
	}
//	@ExceptionHandler(JwtAuthException.class)
//	public ResponseEntity<String> handleJwtException(JwtAuthException e) {
//		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("權限被拒：" + e.getMessage()); // 403
//	}
}