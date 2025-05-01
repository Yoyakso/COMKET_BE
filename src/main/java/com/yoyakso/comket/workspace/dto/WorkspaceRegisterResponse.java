package com.yoyakso.comket.workspace.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkspaceRegisterResponse {
	private Long id;
	private String name;
	private String description;
	// private String imageUrl;
	private String createdAt;
	private String updatedAt;
	private int memberCount;
	private boolean isOwner;
	private boolean isMember;

	// 추가된 필드
	private String imageUrl; // 워크스페이스 이미지 URL
}
