package com.yoyakso.comket.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberUpdateRequest {
	private Long projectMemberId;
	private String positionType;
}
