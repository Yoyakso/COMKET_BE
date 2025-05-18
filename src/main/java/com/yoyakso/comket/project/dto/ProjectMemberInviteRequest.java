package com.yoyakso.comket.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberInviteRequest {
	private List<Long> workspaceMemberIdList;
	private String positionType;
}
