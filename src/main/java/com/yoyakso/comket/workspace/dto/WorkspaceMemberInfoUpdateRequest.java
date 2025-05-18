package com.yoyakso.comket.workspace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceMemberInfoUpdateRequest {
	@JsonProperty("workspace_member_email")
	private String workspaceMemberEmail;

	@JsonProperty("position_type")
	private String positionType;

	private WorkspaceMemberState state;
}
