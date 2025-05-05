package com.yoyakso.comket.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MemberRegisterRequest {
	private String email;
	private String password;
	@JsonProperty("real_name")
	private String realName;
	// private int position;
	// private String profileFileId;
}
