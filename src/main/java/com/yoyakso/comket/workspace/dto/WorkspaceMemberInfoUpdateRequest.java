package com.yoyakso.comket.workspace.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberInfoUpdateRequest {
	@JsonProperty("workspace_member_id")
	private Long workspaceMemberId;

	@JsonProperty("position_type")
	private String positionType;

	private WorkspaceMemberState state;
}
