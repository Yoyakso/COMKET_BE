package com.yoyakso.comket.project.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectInfoResponse {
	private Long projectId;
	private String projectName;
}
