package com.yoyakso.comket.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateRequest {
	private String email;

	@JsonProperty("real_name")
	private String realName;

	private String department;

	private String role;

	private String responsibility;

	@JsonProperty("profile_file_id")
	private Long profileFileId;
}
