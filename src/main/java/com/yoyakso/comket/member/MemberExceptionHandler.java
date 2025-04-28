package com.yoyakso.comket.member;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.yoyakso.comket.exception.CustomException;

@RestControllerAdvice
public class MemberExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<Map<String,String>> handleCustomException(CustomException e) {
		Map<String, String> errorResponse = Map.of(
			"code", e.getCode(),
			"message", e.getMessage()
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}
}
