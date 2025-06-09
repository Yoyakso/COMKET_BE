package com.yoyakso.comket.thread.dto;

import java.util.List;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessageEditRequestDto {
	private Long threadId;
	private Long senderWorkspaceMemberId;
	private Long workspaceId;
	private String content;

	@Nullable
	private List<String> resources;
}
