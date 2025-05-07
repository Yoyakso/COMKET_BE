package com.yoyakso.comket.workspace.dto;

import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberInfoResponse {
	private Long id;
	private String name;
	private String email;
	private String profileFileUrl;
	private String positionType;
	private WorkspaceMemberState state;
	private String createdAt;
	private String updatedAt;
}
