package com.yoyakso.comket.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberInfoResponse {
	private String email;
	private String realName;

}
