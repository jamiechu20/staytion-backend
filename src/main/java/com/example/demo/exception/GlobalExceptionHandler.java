//package com.example.demo.exception;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//	@ExceptionHandler(HotelNotFoundException.class)
//	public ResponseEntity<?> handleHotelNotFound(HotelNotFoundException ex) {
//		Map<String, Object> error = new HashMap<>();
//		error.put("timestamp", LocalDateTime.now());
//		error.put("status", HttpStatus.NOT_FOUND.value());
//		error.put("error", "Not Found");
//		error.put("message", ex.getMessage());
//		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//	}
//
//	@ExceptionHandler(RoomTypeNotFoundException.class)
//	public ResponseEntity<?> handleRoomTypeNotFound(RoomTypeNotFoundException ex) {
//		Map<String, Object> error = new HashMap<>();
//		error.put("timestamp", LocalDateTime.now());
//		error.put("status", HttpStatus.NOT_FOUND.value());
//		error.put("error", "Not Found");
//		error.put("message", ex.getMessage());
//		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
//	}
//
////	@ExceptionHandler(JwtAuthException.class)
////	public ResponseEntity<String> handleJwtException(JwtAuthException e) {
////		return ResponseEntity.status(HttpStatus.FORBIDDEN).body("權限被拒：" + e.getMessage()); // 403
////	}
//}