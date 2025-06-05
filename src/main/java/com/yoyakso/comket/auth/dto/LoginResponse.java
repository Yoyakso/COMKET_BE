package com.yoyakso.comket.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {
	private Long memberId;
	private String name;
	private String email;
	private String accessToken;
	private String loginPlatformInfo;
}
