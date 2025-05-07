package com.yoyakso.comket.project.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberResponse {
	private Long memberId;
	private String name;         // Member에서 가져옴
	private String email;        // Member에서 가져옴
	private String positionType; // ProjectMember에서 가져옴
	private boolean isActive;    // ProjectMember에서 가져옴
}