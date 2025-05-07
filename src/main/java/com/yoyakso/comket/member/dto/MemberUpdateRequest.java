package com.yoyakso.comket.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MemberUpdateRequest {
	private String email;

	@JsonProperty("real_name")
	private String realName;

	@JsonProperty("department")
	private String department;

	@JsonProperty("role")
	private String role;

	@JsonProperty("responsibility")
	private String responsibility;

	@JsonProperty("profile_file_id")
	private Long profileFileId;
}
