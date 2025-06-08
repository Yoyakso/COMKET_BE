package com.yoyakso.comket.file.enums;

import lombok.Getter;

@Getter
public enum FileCategory {
	// 멤버프로필, 워크스페이스프로필, 프로젝트 프로필
	MEMBER_PROFILE("profile/member"),
	WORKSPACE_PROFILE("profile/workspace"),
	PROJECT_PROFILE("profile/project"),
	// 프로젝트 파일
	PROJECT_FILE("project/file"),

	// 스레드 파일
	THREAD_FILE("thread/file");

	private final String directoryName;

	FileCategory(String directoryName) {
		this.directoryName = directoryName;
	}

}
