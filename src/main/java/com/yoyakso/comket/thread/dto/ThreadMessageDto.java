package com.yoyakso.comket.thread.dto;

import java.time.LocalDateTime;

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
	private Long senderMemberId;
	private String senderName;
	private String content;
	private LocalDateTime sentAt;
}
