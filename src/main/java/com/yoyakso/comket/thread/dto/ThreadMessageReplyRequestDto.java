package com.yoyakso.comket.thread.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessageReplyRequestDto {
	private Long ticketId;
	private Long parentThreadId;
	private Long senderWorkspaceMemberId;
	private Long workspaceId;
	private String senderName;
	private String reply;
	private List<Long> mentionedProjectMemberIds;

	@Nullable
	private List<String> resources;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime sentAt;
}
