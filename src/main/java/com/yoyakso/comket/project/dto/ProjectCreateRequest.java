package com.yoyakso.comket.project.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectCreateRequest {
	private String name; // 프로젝트 이름
	private String description; // 프로젝트 설명
	private Boolean isPublic;
	private List<String> tags;
	@JsonProperty("profile_file_id")
	private Long profileFileId; // 프로필 이미지 ID
}
