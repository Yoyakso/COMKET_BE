package com.yoyakso.comket.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MemberUpdateRequest {
	private String email;

	@JsonProperty("real_name")
	private String realName;
}
