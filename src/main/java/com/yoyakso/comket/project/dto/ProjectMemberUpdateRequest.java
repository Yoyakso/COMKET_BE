package com.yoyakso.comket.project.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberUpdateRequest {
	private Long projectMemberId;
	private String positionType;
}
