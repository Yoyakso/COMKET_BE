package com.yoyakso.comket.thread.event;

import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.thread.entity.ThreadMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ThreadMentionedEvent {
	private final ThreadMessage threadMessage;
	private final ProjectMember projectMember;
}