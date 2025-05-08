package com.yoyakso.comket.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleTokenResponse {
	private String access_token;
	private String expires_in;
	private String refresh_token;
	private String scope;
	private String token_type;
	private String id_token;
}
