package com.yoyakso.comket.thread.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yoyakso.comket.thread.enums.ThreadMessageState;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessageDto {
	private Long ticketId;

	@Nullable
	private Long threadId;

	private Long senderWorkspaceMemberId;

	@Nullable
	private Long parentThreadId;

	private String senderName;
	private String content;
	private Boolean isModified;
	private ThreadMessageState messageState;

	@Nullable
	private List<String> resources;

	@Nullable
	private List<Long> mentionedProjectMemberIds;

	@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime sentAt;
}
