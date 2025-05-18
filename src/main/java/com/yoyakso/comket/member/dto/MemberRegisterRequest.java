package com.yoyakso.comket.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRegisterRequest {
	private String email;
	private String password;
	@JsonProperty("real_name")
	private String realName;
	@JsonProperty("profile_file_id")
	private Long profileFileId;
	private String department;
	private String role;
	private String responsibility;
}
