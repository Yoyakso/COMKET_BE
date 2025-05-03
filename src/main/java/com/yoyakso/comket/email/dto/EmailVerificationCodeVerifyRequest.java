package com.yoyakso.comket.email.dto;

import lombok.Data;

@Data
public class EmailVerificationCodeVerifyRequest {
	private String email;
	private String code;
}
