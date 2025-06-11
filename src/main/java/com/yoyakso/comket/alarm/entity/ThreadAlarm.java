package com.yoyakso.comket.alarm.entity;

import com.yoyakso.comket.alarm.enums.ThreadAlarmType;
import com.yoyakso.comket.projectMember.entity.ProjectMember;
import com.yoyakso.comket.thread.entity.ThreadMessage;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThreadAlarm {
	private ThreadMessage threadMessage;

	private Long memberId;

	private ProjectMember mentionedProjectMember;

	private String alarmMessage = "";

	private ThreadAlarmType alarmType;
}