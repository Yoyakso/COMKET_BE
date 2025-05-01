package com.yoyakso.comket.workspace.dto;

import com.yoyakso.comket.workspace.enums.Visibility;

import lombok.Builder;
import lombok.Data;

// 워크스페이스 단건 조회
@Data
@Builder
public class WorkspaceInfoResponse {
	private Long id;
	private String name;
	private String description;
	// private String imageUrl;
	private Visibility visibility;
	private String createdAt;
	private String updatedAt;
	private int memberCount;
	private boolean isOwner;
	private boolean isMember;
}
