package com.yoyakso.comket.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MemberInfoResponse {
	private String email;
	private String realName;
	private String department;
	private String role;
	private String responsibility;
	private String profileFileUrl;
}
