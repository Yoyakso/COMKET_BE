package com.yoyakso.comket.project.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectMemberInviteRequest {
	private List<Long> memberIdList;
	private String positionType;
}
