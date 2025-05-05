package com.yoyakso.comket.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
	// Custom exception handling methods can be added here
	// For example, you can handle specific exceptions and return custom responses

	// Example:
	// @ExceptionHandler(CustomException.class)
	// public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
	//     ErrorResponse errorResponse = new ErrorResponse(ex.getCode(), ex.getMessage());
	//     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	// }

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
		Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
		Map<String, String> errors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}
		logger.error("Validation failed: {}", errors, ex);

		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String, String>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex) {
		Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
		logger.error("HttpMessageNotReadableException occurred: {}", ex.getMessage(), ex);

		Map<String, String> error = new HashMap<>();
		error.put("error", "Invalid request body format");
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
		Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
		logger.error("IllegalArgumentException occurred: {}", ex.getMessage(), ex);

		Map<String, String> errorResponse = new HashMap<>();
		errorResponse.put("message", ex.getMessage() != null ? ex.getMessage() : "잘못된 형식의 요청입니다.");
		errorResponse.put("code", "INVALID_REQUEST");

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<Map<String, String>> handleCustomException(CustomException e) {
		Map<String, String> errorResponse = Map.of(
			"code", e.getCode(),
			"message", e.getMessage()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
}
