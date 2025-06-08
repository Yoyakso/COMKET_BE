package com.yoyakso.comket.thread.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThreadMessageEditDto {
	private Long threadId;
	private Long senderMemberId;
	private String content;
}
