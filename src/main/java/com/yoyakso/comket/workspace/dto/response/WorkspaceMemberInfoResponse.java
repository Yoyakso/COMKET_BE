package com.yoyakso.comket.workspace.dto.response;

import com.yoyakso.comket.workspaceMember.enums.WorkspaceMemberState;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceMemberInfoResponse {
	private Long workspaceMemberid;
	private String name;
	private String email;
	private String profileFileUrl;
	private String department;
	private String responsibility;
	private String positionType;
	private WorkspaceMemberState state;
	private String createdAt;
	private String updatedAt;
}
