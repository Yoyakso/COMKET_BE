package com.yoyakso.comket.exception;

import java.util.HashMap;
import java.util.Map;

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
	public ResponseEntity<Map<String,String>> handleValidationException(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.put(error.getField(), error.getDefaultMessage());
		}
		return ResponseEntity.badRequest().body(errors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<Map<String,String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		Map<String, String> error = new HashMap<>();
		error.put("error", "Invalid request body format");
		error.put("message", ex.getLocalizedMessage());
		return ResponseEntity.badRequest().body(error);
	}
}
