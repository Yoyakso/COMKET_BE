package com.yoyakso.comket.workspace.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberInfoUpdateRequest {
	// 닉네임
	private String nickname;
	
	// 소속
	private String department;

	// 직무
	private String responsibility;

	// 프로필
	@JsonProperty("profile_file_id")
	private Long profileFileId;
}
