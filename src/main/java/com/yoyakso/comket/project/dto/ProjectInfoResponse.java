package com.yoyakso.comket.project.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectInfoResponse {
	private Long projectId;
	private String projectName;
	private String projectDescription;
	private List<String> projectTag;
	private Boolean isPublic;
	private Long adminId;
	private LocalDateTime createTime;
	private String profileFileUrl;
}
