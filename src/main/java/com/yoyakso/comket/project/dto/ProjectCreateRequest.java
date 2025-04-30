package com.yoyakso.comket.project.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectCreateRequest {
	private String name; // 프로젝트 이름
	private String purpose; // 프로젝트 목적
	private String description; // 프로젝트 설명
	private List<String> members;
	private Boolean isPublic;
}
