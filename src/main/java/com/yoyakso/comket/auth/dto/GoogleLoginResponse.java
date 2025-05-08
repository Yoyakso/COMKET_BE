package com.yoyakso.comket.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleLoginResponse {
	private String accessToken;
	// private String refreshToken;
	private String name;
	private String email;
}
