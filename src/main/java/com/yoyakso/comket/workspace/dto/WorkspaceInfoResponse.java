package com.yoyakso.comket.workspace.dto;

import com.yoyakso.comket.workspace.enums.WorkspaceState;

import lombok.Builder;
import lombok.Data;

// 워크스페이스 단건 조회
@Data
@Builder
public class WorkspaceInfoResponse {
	private Long id;
	private String name;
	private String description;
	private WorkspaceState state;
	private Boolean isPublic;
	private String profileFileUrl;
	private String inviteCode;
	private String createdAt;
	private String updatedAt;
	private int memberCount;
}
