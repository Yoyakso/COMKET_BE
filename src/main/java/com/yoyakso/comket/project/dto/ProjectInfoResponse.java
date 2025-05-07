package com.yoyakso.comket.project.dto;

import java.time.LocalDateTime;

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
	private Boolean isPublic;
	private LocalDateTime createTime;
	private String profileFileUrl;
	// private List<String> projectTag; // TODO: 프로젝트 태그 개발 시 추가
	// private Long adminId; // TODO: 추가 기획 논의
	// private Long memberCount; // TODO: 추가 기획 논의
	// private Long OwnerId; // TODO: 추가 기획 논의
}
