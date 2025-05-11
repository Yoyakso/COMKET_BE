package com.yoyakso.comket.project.dto;

import com.yoyakso.comket.projectMember.enums.ProjectMemberState;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberResponse {
	private Long projectMemberId;
	private String name;         // Member에서 가져옴
	private String email;        // Member에서 가져옴
	private String positionType; // ProjectMember에서 가져옴
	private ProjectMemberState state;    // ProjectMember에서 가져옴
}