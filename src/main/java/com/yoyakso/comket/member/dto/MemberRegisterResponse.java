package com.yoyakso.comket.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberRegisterResponse {
	private Long memberId;
	private String email;
	private String realName;
	private String department;
	private String role;
	private String responsibility;
	private String accessToken;
	private String refreshToken;
	private String profileFileUrl;
}
