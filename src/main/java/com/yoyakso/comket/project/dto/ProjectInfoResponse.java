package com.yoyakso.comket.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectInfoResponse {
	private Long projectId;
	private String projectName;
}
