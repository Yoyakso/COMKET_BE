package com.yoyakso.comket.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenReissueRequest {
	@NotBlank
	private String refreshToken;
}
