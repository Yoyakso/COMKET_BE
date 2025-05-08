package com.yoyakso.comket.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
	private Long userId;
	private String name;
	private String email;
	private String accessToken;
}
